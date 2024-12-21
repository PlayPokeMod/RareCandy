package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.assimp.*;
import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.*;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.opengl.GL30C.*;

public class ModelLoader {
    private final ExecutorService modelLoadingPool;

    private static Vector3f temp = new Vector3f();

    public ModelLoader() {
        this(4);
    }

    public ModelLoader(int numThreads) {
        this.modelLoadingPool = Executors.newFixedThreadPool(numThreads);
    }

    public interface NodeProvider {
        Animation.AnimationNode[] getNode(Animation animation, Skeleton skeleton);
    }


    public static List<Attribute> ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );

    public static <T extends MeshObject, V extends MultiRenderObject<T>> void processModel(V objects, PixelAsset asset, Map<String, AnimResource> animResources, Map<String, String> images, ModelConfig config, List<Runnable> glCalls, Supplier<T> supplier, RenderModel.Provider renderModelSuppler) {
        if (config == null) throw new RuntimeException("config.json can't be null.");

        var scene = ModelLoader.read(asset);

        var rootNode = ModelNode.create(scene.mRootNode());

        var meshes = IntStream.range(0, scene.mNumMeshes()).mapToObj(i -> AIMesh.create(scene.mMeshes().get(i))).toArray(AIMesh[]::new);
        
        Skeleton skeleton = new Skeleton(rootNode, meshes, config.excludeMeshNamesFromSkeleton);
        
        var animations = processAnimations(scene, skeleton, animResources, config);

        var variants = processVariants(config, images);

        for (var mesh : meshes) {
            processPrimitiveModels(renderModelSuppler, objects, supplier, mesh, variants, config.meshesToRenderFirst != null ? config.meshesToRenderFirst : Collections.emptyList(), glCalls, skeleton, animations, config.hideDuringAnimation, config.modelOptions != null ? config.modelOptions : Collections.<String, MeshOptions>emptyMap());
        }

        var transform = new Matrix4f();

        traverseTree(transform, rootNode, objects);

        Assimp.aiReleaseImport(scene);

    }

    private static Map<String, Animation> processAnimations(AIScene scene, Skeleton skeleton, Map<String, AnimResource> animResources, ModelConfig config) {
        extractAssimpAnimations(scene, skeleton, animResources);

        Map<String, Animation> animations = new HashMap<>();

        var offSetsToInsert = new HashMap<String, Animation.Offset>();

        animResources.forEach((name, animResource) -> {
            var fps = animResource.fps();
            fps = config.animationFpsOverride != null && config.animationFpsOverride.containsKey(name) ? config.animationFpsOverride.get(name) : fps;

            var offsets = animResource.getOffsets();
            offsets.forEach((trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach(a -> offSetsToInsert.put(a, offset)));
            offsets.putAll(offSetsToInsert);
            offSetsToInsert.clear();

            var nodes = animResource.getNodes(skeleton);
            var ignoreScaling = config.ignoreScaleInAnimation != null && (config.ignoreScaleInAnimation.contains(name) || config.ignoreScaleInAnimation.contains("all"));

            animations.put(name, new Animation(name, (int) fps, skeleton, nodes, offsets, ignoreScaling, config.offsets.getOrDefault(name, new SkeletalTransform()).scale(config.scale)));
        });

        return animations;
    }

    private static void extractAssimpAnimations(AIScene scene, Skeleton skeleton, Map<String, AnimResource> animResources) {
        for (int i = 0; i < scene.mNumAnimations(); i++) {
            AIAnimation aiAnimation = AIAnimation.create(scene.mAnimations().get(i));
            var animName = aiAnimation.mName().dataString();

            var fps = aiAnimation.mTicksPerSecond();

            var animationNodes = new Animation.AnimationNode[skeleton.jointMap.size()];

            for (int channelIndex = 0; channelIndex < aiAnimation.mNumChannels(); channelIndex++) {
                var channel = AINodeAnim.create(aiAnimation.mChannels().get(channelIndex));

                var boneName = channel.mNodeName().dataString();

                if(!skeleton.boneIdMap.containsKey(boneName)) continue;

                var node = animationNodes[skeleton.boneIdMap.get(boneName)] = new Animation.AnimationNode();


                for (int posIndex = 0; posIndex < channel.mNumPositionKeys(); posIndex++) {
                    var posKey = channel.mPositionKeys().get(posIndex);

                    var time = posKey.mTime();
                    var pos = new Vector3f(posKey.mValue().x(), posKey.mValue().y(), posKey.mValue().z());

                    node.positionKeys.add(time, pos);
                }

                for (int rotIndex = 0; rotIndex < channel.mNumRotationKeys(); rotIndex++) {
                    var rotKey = channel.mRotationKeys().get(rotIndex);

                    var time = rotKey.mTime();
                    var rot = new Quaternionf(rotKey.mValue().x(), rotKey.mValue().y(), rotKey.mValue().z(), rotKey.mValue().w());

                    node.rotationKeys.add(time, rot);
                }

                for (int scaleIndex = 0; scaleIndex < channel.mNumScalingKeys(); scaleIndex++) {
                    var scaleKey = channel.mScalingKeys().get(scaleIndex);

                    var time = scaleKey.mTime();
                    var scale = new Vector3f(scaleKey.mValue().x(), scaleKey.mValue().y(), scaleKey.mValue().z());

                    node.scaleKeys.add(time, scale);
                }
            }

            for (int nodeIndex = 0; nodeIndex < animationNodes.length; nodeIndex++) {

                if(animationNodes[nodeIndex] == null) {
                    var node = new Animation.AnimationNode();
                    var joint = skeleton.jointMap.get(skeleton.bones[nodeIndex].name);

                    node.rotationKeys.add(0, joint.poseRotation);
                    node.rotationKeys.add(0, joint.poseRotation);
                    node.scaleKeys.add(0, joint.poseScale);
                }
            }

            animResources.putIfAbsent(animName, new GenericAnimResource((long) fps, animationNodes));
        }
    }

    private static Map<String, Map<String, Variant>> processVariants(ModelConfig config, Map<String, String> images) {
        var materials = config.prepMaterials(images);

        var defaultVariant = new HashMap<String, Variant>();
        
        Map<String, List<String>> aliases = config.aliases != null ? config.aliases : Collections.emptyMap();

        
        config.defaultVariant.forEach((k, v) -> {
            var variant = new Variant(materials.get(v.material()), v.hide(), v.offset());

            if(!aliases.isEmpty() && aliases.containsKey(k)) {
                for (String s : aliases.get(k)) {
                    defaultVariant.put(s, variant);
                }
            }
            else defaultVariant.put(k, variant);

        });

        var variants = new HashMap<String, Map<String, Variant>>();

        if(config.variants != null) {
            config.variants.forEach((variantKey, variantParent) -> {

                VariantParent child = config.variants.get(variantParent.inherits());

                var map = variantParent.details();

                while (child != null) {
                    var details = child.details();

                    applyVariantDetails(details, map);

                    child = config.variants.get(child.inherits());
                }

                applyVariantDetails(config.defaultVariant, map);

                applyVariant(variantKey, variants, materials, map, aliases);
            });
        } else {
            defaultVariant.forEach((s1, variant) -> {
                defaultVariant.forEach((s, variant1) -> variants.computeIfAbsent(s, a -> new HashMap<>()).put("regular", variant1));
            });
        }
        
        return variants;
    }

    private static <T extends MeshObject> void traverseTree(Matrix4f transform, ModelNode node, MultiRenderObject<T> objects) {
        applyTransforms(transform, node);

        objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

        for (var child : node.children) {
            traverseTree(transform, child, objects);
        }
    }

    private static void applyVariantDetails(Map<String, VariantDetails> applied, Map<String, VariantDetails> appliee) {
        for (Map.Entry<String, VariantDetails> entry : applied.entrySet()) {
            String k = entry.getKey();
            VariantDetails v = entry.getValue();
            appliee.compute(k, (s, variantDetails) -> {
                if(variantDetails == null) return v;
                return variantDetails.fillIn(v);
            });
        }
    }

    private static void applyVariant(
            String variantKey,
            Map<String, Map<String, Variant>> variants,
            Map<String, Material> materials,
            Map<String, VariantDetails> variantMap,
            Map<String, List<String>> aliases) {
        variantMap.forEach((k, v) -> {
            Material mat = materials.get(v.material());
            boolean hide = v.hide() != null && v.hide();
            var offset = v.offset() != null ? v.offset() : null;

            var variant = new Variant(mat, hide, offset);

            if(!aliases.isEmpty() && aliases.containsKey(k)) aliases.get(k).forEach(s -> variants.computeIfAbsent(s, s1 -> new HashMap<>()).put(variantKey, variant));
            else {
                variants.computeIfAbsent(k, s1 -> new HashMap<>()).put(variantKey, variant);
            }
        });
    }

    private static void applyTransforms(Matrix4f transform, ModelNode node) {
        transform.set(node.transform);
    }

    private static <T extends MeshObject> void processPrimitiveModels(RenderModel.Provider provider, MultiRenderObject<T> objects, Supplier<T> objSupplier, AIMesh mesh, Map<String, Map<String, Variant>> variants, List<String> meshesToRenderFirst, List<Runnable> glCalls, @Nullable Skeleton skeleton, @Nullable Map<String, Animation> animations, Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimations, Map<String, MeshOptions> meshOptions) {
        var name = mesh.mName().dataString();

        var renderObject = objSupplier.get();
        var glModel = processPrimitiveModel(skeleton, mesh, meshOptions, glCalls, objects.dimensions, provider);

        var variant = variants.get(name);

        if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
            animatedMeshObject.setup(variant, glModel, name, animations, hideDuringAnimations.getOrDefault(name, ModelConfig.HideDuringAnimation.NONE));
        } else {
            renderObject.setup(variant, glModel, name);
        }

        objects.add(renderObject, meshesToRenderFirst.contains(name));
    }

    private static RenderModel processPrimitiveModel(Skeleton skeleton, AIMesh mesh, Map<String, MeshOptions> options, List<Runnable> glCalls, Vector3f dimensions, RenderModel.Provider renderModelSupplier) {
        var name = mesh.mName().dataString();

        var invertFace = options.containsKey(name) && options.get(name).invert();

        var length = calculateVertexSize(ATTRIBUTES);
        var amount = mesh.mNumVertices();

        var vertexBuffer = MemoryUtil.memAlloc(length * amount);

        var aiFaces = mesh.mFaces();
        var indexBuffer = MemoryUtil.memAlloc(mesh.mNumFaces() * Integer.BYTES * 3);

        for (int j = 0; j < mesh.mNumFaces(); j++) {
            var aiFace = aiFaces.get(j).mIndices();
            indexBuffer.putInt(aiFace.get(invertFace ? 2 : 0)).putInt(aiFace.get(1)).putInt(aiFace.get(invertFace ? 0 : 2));
        }
        indexBuffer.flip();

        var aiVert = mesh.mVertices();
        var aiUV = mesh.mTextureCoords(0);

        if (aiUV == null) {
            throw new RuntimeException("Error UV coordinates not found!");
        }

        var aiNormals = mesh.mNormals();

        if (aiNormals == null) {
            throw new RuntimeException("Error Normals not found!");
        }

        byte[] ids = new byte[amount * 4];
        float[] weights = new float[amount * 4];

        if (mesh.mBones() != null) {
            var aiBones = requireNonNull(mesh.mBones());

            for (int boneIndex = 0; boneIndex < aiBones.capacity(); boneIndex++) {
                var aiBone = AIBone.create(aiBones.get(boneIndex));

                var weight = aiBone.mWeights();

                var index = skeleton.getId(aiBone.mName().dataString());

                for (int weightId = 0; weightId < weight.capacity(); weightId++) {
                    var aiWeight = weight.get(weightId);
                    var vertexId = aiWeight.mVertexId();

                    if(aiWeight.mWeight() > 0f) {
                        addBoneData(ids, weights, vertexId, (byte) index, aiWeight.mWeight());
                    }
                }
            }
        }

        var isEmpty = IntStream.range(0, ids.length).allMatch(a -> ids[a] == 0);

        for (int i = 0; i < amount; i++) {

            var position = aiVert.get(i);
            var uv = aiUV.get(i);
            var normal = aiNormals.get(i);

            vertexBuffer.putFloat(position.x());
            vertexBuffer.putFloat(position.y());
            vertexBuffer.putFloat(position.z());
            vertexBuffer.putFloat(uv.x());
            vertexBuffer.putFloat(1 - uv.y());
            vertexBuffer.putFloat(normal.x());
            vertexBuffer.putFloat(normal.y());
            vertexBuffer.putFloat(normal.z());

            if(isEmpty) {
                vertexBuffer.put((byte) 1);
                vertexBuffer.put((byte) 0);
                vertexBuffer.put((byte) 0);
                vertexBuffer.put((byte) 0);

                vertexBuffer.putFloat(1);
                vertexBuffer.putFloat(0);
                vertexBuffer.putFloat(0);
                vertexBuffer.putFloat(0);
            } else {
                vertexBuffer.put(ids[i * 4 + 0]);
                vertexBuffer.put(ids[i * 4 + 1]);
                vertexBuffer.put(ids[i * 4 + 2]);
                vertexBuffer.put(ids[i * 4 + 3]);

                vertexBuffer.putFloat(weights[i * 4 + 0]);
                vertexBuffer.putFloat(weights[i * 4 + 1]);
                vertexBuffer.putFloat(weights[i * 4 + 2]);
                vertexBuffer.putFloat(weights[i * 4 + 3]);
            }

            dimensions.max(temp.set(position.x(), position.y(), position.z()));
        }

        vertexBuffer.flip();


        var indexSize = mesh.mNumFaces() * 3;

        return renderModelSupplier.create(vertexBuffer, indexBuffer, glCalls, indexSize, GL11.GL_UNSIGNED_INT, ATTRIBUTES);
    }

    public static void addBoneData(byte[] ids, float[] weights, int vertexId, byte boneId, float weight) {
        var length = vertexId * 4;
        for (var i = 0 ; i < 4; i++) {
            var blep = length + i;

            if (weights[blep] == 0.0) {
                ids[blep] = boneId;
                weights[blep] = weight;

                return;
            }
        }
    }

    public static int calculateVertexSize(List<Attribute> layout) {
        var size = 0;
        for (var attrib : layout) size += calculateAttributeSize(attrib);
        return size;
    }

    public static int calculateAttributeSize(Attribute attrib) {
        return switch (attrib.glType()) {
            case GL_FLOAT, GL_UNSIGNED_INT, GL_INT -> 4;
            case GL_BYTE, GL_UNSIGNED_BYTE -> 1;
            case GL_SHORT, GL_UNSIGNED_SHORT, GL_HALF_FLOAT -> 2;
            default -> throw new IllegalStateException("Unexpected OpenGL Attribute type: " + attrib.glType() + ". If this is wrong, please contact hydos");
        } * attrib.amount();
    }

    private static List<Attribute> DEFAULT_ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );


    public static void generateVao(GLModel model, ByteBuffer vertexBuffer, List<Attribute> layout) {
        model.vao = glGenVertexArrays();

        glBindVertexArray(model.vao);
        var stride = calculateVertexSize(layout);
        var attribPtr = 0;

        // I hate openGL. why cant I keep the vertex data and vertex layout separate :(
        model.vbo = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, model.vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < layout.size(); i++) {
            var attrib = layout.get(i);
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(
                    i,
                    attrib.amount(),
                    attrib.glType(),
                    false,
                    stride,
                    attribPtr
            );
            attribPtr += calculateAttributeSize(attrib);
        }

        glBindVertexArray(0);
    }

    private static void vertexAttribPointer(Attribute data, int binding) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                data.amount(),
                data.glType(),
                false,
                0,
                0);
    }

    public <T extends RenderObject> MultiRenderObject<T> createObject(@NotNull Supplier<PixelAsset> is, GlCallSupplier<T, MultiRenderObject<T>> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        return createObject(MultiRenderObject::new, is, objectCreator, onFinish);
    }

    public <T extends RenderObject, V extends MultiRenderObject<T>> V createObject(Supplier<V> supplier, @NotNull Supplier<PixelAsset> is, GlCallSupplier<T, V> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        V obj = supplier.get();
        var task = threadedCreateObject(obj, is, objectCreator, onFinish);
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);
        return obj;
    }

    public MultiRenderObject<MeshObject> generatePlane(float width, float length, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        var pair = PlaneGenerator.generatePlane(width, length);

        var task = ThreadSafety.wrapException(() -> {
            ThreadSafety.runOnContextThread(() -> {
                pair.a().forEach(Runnable::run);
                pair.b().updateDimensions();
                if (onFinish != null) onFinish.accept(pair.b());
            });
        });
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);

        return pair.b();
    }

    public MultiRenderObject<MeshObject> generateCube(float width, float length, float height, String image, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        var pair = PlaneGenerator.generateCube(width, length, height, image);

        var task = ThreadSafety.wrapException(() -> {
            ThreadSafety.runOnContextThread(() -> {
                pair.a().forEach(Runnable::run);
                pair.b().updateDimensions();
                if (onFinish != null) onFinish.accept(pair.b());
            });
        });
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);

        return pair.b();
    }

    private <T extends RenderObject, V extends MultiRenderObject<T>> Runnable threadedCreateObject(V obj, @NotNull Supplier<PixelAsset> is, GlCallSupplier<T, V> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        return ThreadSafety.wrapException(() -> {
            var asset = is.get();
            var config = asset.getConfig();

            var images = readImages(asset);

            if(asset.getModelFile() == null) return;

            if (config != null) obj.scale = config.scale;

            var aninResouces = new HashMap<String, AnimResource>();

            SmdResource.read(asset, aninResouces);
            GfbanmResource.read(asset, aninResouces);
            TrAnimationResource.read(asset, aninResouces);
            var glCalls = objectCreator.getCalls(asset, aninResouces, images, config, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });
        });
    }


    public static Map<String, String> readImages(PixelAsset asset) {
        var images = asset.getImageFiles();
        var map = new HashMap<String, String>();
        for (var entry : images) {
            var key = entry.getKey();

            var id = asset.name + "-" + key;
            ITextureLoader.instance().register(id, key, entry.getValue());

            map.put(key, id);
        }

        return map;
    }

    public void close() {
        modelLoadingPool.shutdown();
    }

    public static AIScene read(PixelAsset asset) {
        var name = asset.modelName;

        var fileIo = AIFileIO.create()
                .OpenProc((pFileIO, pFileName, openMode) -> {
                    var fileName = MemoryUtil.memUTF8(pFileName);
                    var bytes = asset.get(fileName);
                    var data = BufferUtils.createByteBuffer(bytes.length);
                    data.put(bytes);
                    data.flip();

                    return AIFile.create()
                            .ReadProc((pFile, pBuffer, size, count) -> {
                                var max = Math.min(data.remaining() / size, count);
                                MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, max * size);
                                data.position((int) (data.position() + max * size));
                                return max;
                            })
                            .SeekProc((pFile, offset, origin) -> {
                                switch (origin) {
                                    case Assimp.aiOrigin_CUR -> data.position(data.position() + (int) offset);
                                    case Assimp.aiOrigin_SET -> data.position((int) offset);
                                    case Assimp.aiOrigin_END -> data.position(data.limit() + (int) offset);
                                }

                                return 0;
                            })
                            .FileSizeProc(pFile -> data.limit())
                            .address();
                })
                .CloseProc((pFileIO, pFile) -> {
                    var aiFile = AIFile.create(pFile);
                    aiFile.ReadProc().free();
                    aiFile.SeekProc().free();
                    aiFile.FileSizeProc().free();
                });

        var scene = Assimp.aiImportFileEx(name, Assimp.aiProcess_Triangulate | Assimp.aiProcess_ImproveCacheLocality, fileIo);

        if (scene == null) throw new RuntimeException(Assimp.aiGetErrorString());

        return scene;
    }

    public static <T> Map<String, Map<String, T>> reverseMap(Map<String, Map<String, T>> inputMap) {
        Map<String, Map<String, T>> reversedMap = new HashMap<>();

        for (Map.Entry<String, Map<String, T>> outerEntry : inputMap.entrySet()) {
            String outerKey = outerEntry.getKey();
            Map<String, T> innerMap = outerEntry.getValue();

            for (Map.Entry<String, T> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                T value = innerEntry.getValue();

                reversedMap.computeIfAbsent(innerKey, k -> new HashMap<>()).put(outerKey, value);
            }
        }

        return reversedMap;
    }

    public static Map<String, List<String>> reverseListMap(Map<String, List<String>> inputMap) {
        Map<String, List<String>> reversedMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
            String outerKey = entry.getKey();
            List<String> innerList = entry.getValue();

            for (String innerKey : innerList) {
                reversedMap.computeIfAbsent(innerKey, k -> new ArrayList<>()).add(outerKey);
            }
        }

        return reversedMap;
    }

    public static Matrix4f from(Matrix4f transform, AIMatrix4x4 aiMat4) {
        return transform.set(aiMat4.a1(), aiMat4.a2(), aiMat4.a3(), aiMat4.a4(),
                aiMat4.b1(), aiMat4.b2(), aiMat4.b3(), aiMat4.b4(),
                aiMat4.c1(), aiMat4.c2(), aiMat4.c3(), aiMat4.c4(),
                aiMat4.d1(), aiMat4.d2(), aiMat4.d3(), aiMat4.d4());
    }
}