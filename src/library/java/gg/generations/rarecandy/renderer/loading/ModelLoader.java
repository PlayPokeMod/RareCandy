package gg.generations.rarecandy.renderer.loading;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import gg.generations.rarecandy.pokeutils.*;
import gg.generations.rarecandy.pokeutils.gfbanm.AnimationT;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.pokeutils.tracm.TRACM;
import gg.generations.rarecandy.pokeutils.tranm.TRANMT;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.animation.*;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.GlCallSupplier;
import gg.generations.rarecandy.renderer.model.MeshDrawCommand;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModelLoader {
    private final GltfModelReader reader = new GltfModelReader();
    private final ExecutorService modelLoadingPool;

    public ModelLoader() {
        this(4);
    }

    public ModelLoader(int numThreads) {
        this.modelLoadingPool = Executors.newFixedThreadPool(numThreads);
    }

    public interface NodeProvider {
        Animation.AnimationNode[] getNode(Animation animation, Skeleton skeleton);
    }

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, Pair<byte[], byte[]>> trFilesMap, Map<String, String> images, ModelConfig config, List<Runnable> glCalls, Supplier<T> supplier) {
        create2(objects, gltfModel, smdFileMap, gfbFileMap, trFilesMap, images, config, glCalls, supplier, Animation.GLB_SPEED);
    }

    public static <T extends MeshObject> void create2(MultiRenderObject<T> objects, GltfModel gltfModel, Map<String, SMDFile> smdFileMap, Map<String, byte[]> gfbFileMap, Map<String, Pair<byte[], byte[]>> trFilesMap, Map<String, String> images, ModelConfig config, List<Runnable> glCalls, Supplier<T> supplier, int animationSpeed) {
        if (config == null) throw new RuntimeException("Error no config.json found.");

        checkForRootTransformation(objects, gltfModel);
        if (gltfModel.getSceneModels().size() > 1) throw new RuntimeException("Cannot handle more than one scene");

        Map<String, Animation> animations = null;

        Skeleton skeleton = null;

        if (!gltfModel.getSkinModels().isEmpty()) {
            skeleton = new Skeleton(gltfModel.getNodeModels(), gltfModel.getSkinModels().get(0));
//            animations = gltfModel.getAnimationModels().stream().map(animationModel -> new GlbAnimation(animationModel, skeleton, config != null && config.animationFpsOverride != null ? config.animationFpsOverride.getOrDefault(animationModel.getName(), 30) : animationSpeed)).collect(Collectors.toMap(animation -> animation.name, animation -> animation));

            Map<String, NodeProvider> animationNodeMap = new HashMap<>();
            Map<String, Integer> fpsMap = new HashMap<>();
            Map<String, Map<String, Animation.Offset>> offsetsMap = new HashMap<>();

            Set<String> animationNames = new HashSet<>();


            for (var entry : trFilesMap.entrySet()) {
                var name = entry.getKey();
                var tranm = entry.getValue().a() != null ? TRANMT.deserializeFromBinary(entry.getValue().a()) : null;
                var tracm = entry.getValue().b() != null ? TRACM.getRootAsTRACM(ByteBuffer.wrap(entry.getValue().b())) : null;

                if (tranm != null || tracm != null) {
                    animationNames.add(name);
                    if (tranm != null) {
                        animationNodeMap.putIfAbsent(name, (animation, skeleton1) -> TranmUtils.getNodes(animation, skeleton1, tranm));
                        fpsMap.putIfAbsent(name, (int) tranm.getInfo().getAnimationRate());
                    }

                    if (tracm != null) {
                        offsetsMap.putIfAbsent(name, TracmUtils.getOffsets(tracm));
                        fpsMap.putIfAbsent(name, (int) tracm.config().framerate());
                    }
                }
            }

            for (var entry : gfbFileMap.entrySet()) {
                var name = entry.getKey();

                try {
                    var gfbAnim = AnimationT.deserializeFromBinary(entry.getValue());

                    if (gfbAnim.getMaterial() != null || gfbAnim.getSkeleton() != null) {
                        animationNames.add(name);
                        fpsMap.put(name, (int) gfbAnim.getInfo().getFrameRate());

                        if (gfbAnim.getSkeleton() != null) {
                            animationNodeMap.put(name, (animation, skeleton13) -> GfbanmUtils.getNodes(animation, skeleton13, gfbAnim));
                        }

                        if (gfbAnim.getMaterial() != null) {
                            offsetsMap.put(name, GfbanmUtils.getOffsets(gfbAnim));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Failed to load animation %s due to the following exception: %s".formatted(name, e.getMessage()));
                    e.printStackTrace();
                }

            }

            for (var entry : smdFileMap.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                if (value != null) {
                    animationNames.add(key);
                    animationNodeMap.putIfAbsent(key, (NodeProvider) (animation, skeleton12) -> SmdUtils.getNode(animation, skeleton12, value));
                    fpsMap.putIfAbsent(key, 30);
                }
            }

            animations = new HashMap<>();

            for (var name : animationNames) {
                var fps = fpsMap.get(name);
                fps = config.animationFpsOverride != null && config.animationFpsOverride.containsKey(name) ? config.animationFpsOverride.get(name) : fps;

                var offsets = offsetsMap.getOrDefault(name, new HashMap<>());
                offsets.forEach((trackName, offset) -> config.getMaterialsForAnimation(trackName).forEach(a -> offsets.put(a, offset)));

                var nodes = animationNodeMap.getOrDefault(name, (animation, skeleton14) -> null);
                var ignoreScaling = config.ignoreScaleInAnimation != null && (config.ignoreScaleInAnimation.contains(name) || config.ignoreScaleInAnimation.contains("all"));

                animations.put(name, new Animation(name, fps, skeleton, nodes, offsets, ignoreScaling));
            }
        }

        Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimation = new HashMap<>();

        var materials = new HashMap<String, Material>();

        config.materials.forEach((k, v) -> {
            var material = MaterialReference.process(k, config.materials, images);

            materials.put(k, material);
        });

        var defaultVariant = new HashMap<String, Variant>();
        config.defaultVariant.forEach((k, v) -> {
            var variant = new Variant(materials.get(v.material()), v.hide(), v.offset());
            defaultVariant.put(k, variant);
        });

        var variantMaterialMap = new HashMap<String, Map<String, Material>>();
        var variantHideMap = new HashMap<String, List<String>>();
        var variantOffsetMap = new HashMap<String, Map<String, Vector2f>>();

        if (config.hideDuringAnimation != null) {
            hideDuringAnimation = config.hideDuringAnimation;
        }

        if (config.variants != null) {
            for (Map.Entry<String, VariantParent> entry : config.variants.entrySet()) {
                String variantKey = entry.getKey();
                VariantParent variantParent = entry.getValue();

                VariantParent child = config.variants.get(variantParent.inherits());

                var map = variantParent.details();

                while (child != null) {
                    var details = child.details();

                    applyVariantDetails(details, map);

                    child = config.variants.get(child.inherits());
                }

                applyVariantDetails(config.defaultVariant, map);

                var matMap = variantMaterialMap.computeIfAbsent(variantKey, s3 -> new HashMap<>());
                var hideMap = variantHideMap.computeIfAbsent(variantKey, s3 -> new ArrayList<>());
                var offsetMap = variantOffsetMap.computeIfAbsent(variantKey, s3 -> new HashMap<>());

                applyVariant(materials, matMap, hideMap, map);
            }
        } else {
            var matMap = variantMaterialMap.computeIfAbsent("regular", s3 -> new HashMap<>());
            var hideMap = variantHideMap.computeIfAbsent("regular", s3 -> new ArrayList<>());
            var offsetMap = variantOffsetMap.computeIfAbsent("regular", s3 -> new HashMap<>());

            defaultVariant.forEach((s1, variant) -> {
                matMap.put(s1, variant.material());
                if (variant.hide()) hideMap.add(s1);
                if (variant.offset() != null) offsetMap.put(s1, variant.offset());
            });
        }

        var matMap = reverseMap(variantMaterialMap);
        var hidMap = reverseListMap(variantHideMap);
        var offsetMap = reverseMap(variantOffsetMap);

        objects.dimensions.set(calculateDimensions(gltfModel));

        for (var node : gltfModel.getSceneModels().get(0).getNodeModels()) {
            var transform = new Matrix4f();

            traverseTree(transform, node, objects, supplier, matMap, hidMap, offsetMap, glCalls, skeleton, animations, hideDuringAnimation);
        }
//        } else {
//
//            //Original model loading code
//
//            var textures = gltfModel.getTextureModels().stream().collect(Collectors.toMap(textureModel -> textureModel.getImageModel().getName(), raw -> {
//                var id = gltfModel.getNodeModels().get(0).getName() + "-" + raw.getImageModel().getName();
//
//                var image = ImageUtils.readAsBufferedImage(raw.getImageModel().getImageData());
//
//                ITextureLoader.instance().register(id, new TextureReference(image, raw.getImageModel().getName()));
//
//                return id;
//            }));
//
//            var materials = gltfModel.getMaterialModels().stream().map(MaterialModelV2.class::cast).map(raw -> {
//                var textureName = raw.getBaseColorTexture().getImageModel().getName();
//                return SolidReferenceMaterial.process(raw.getName(), textureName, textures);
//            }).toList();
//            var variants = getVariants(gltfModel);
//
//            objects.dimensions.set(calculateDimensions(gltfModel));
//            objects.scale = 1f / objects.scale;
//
//            // gltfModel.getSceneModels().get(0).getNodeModels().get(0).getScale()
//            for (var node : gltfModel.getSceneModels().get(0).getNodeModels()) {
//                var transform = new Matrix4f();
//                applyTransforms(transform, node);
//
//                if (node.getChildren().isEmpty()) {
//                    // Model Loading Method #1
//                    objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));
//
//                    for (var meshModel : node.getMeshModels()) {
//                        var showList = collectShownVariants(meshModel, variants);
//
//                        processPrimitiveModels(objects, supplier, meshModel, materials, variants, showList, glCalls, skeleton, animations);
//                    }
//                } else {
//                    // Model Loading Method #2
//                    for (var child : node.getChildren()) {
//                        applyTransforms(transform, child);
//                        objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));
//
//                        for (var meshModel : child.getMeshModels()) {
//                            var showList = collectShownVariants(meshModel, variants);
//                            processPrimitiveModels(objects, supplier, meshModel, materials, variants, showList, glCalls, skeleton, animations);
//                        }
//                    }
//                }
//            }
//        }
    }

    private static <T extends MeshObject> void traverseTree(Matrix4f transform, NodeModel node, MultiRenderObject<T> objects, Supplier<T> supplier, Map<String, Map<String, Material>> matMap, Map<String, List<String>> hidMap, Map<String, Map<String, Vector2f>> offsetMap, List<Runnable> glCalls, Skeleton skeleton, Map<String, Animation> animations, Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimation) {
        applyTransforms(transform, node);

        objects.setRootTransformation(objects.getRootTransformation().add(transform, new Matrix4f()));

        for (var meshModel : node.getMeshModels()) {
            processPrimitiveModels(objects, supplier, meshModel, matMap, hidMap, offsetMap, glCalls, skeleton, animations, hideDuringAnimation);
        }

        for (var child : node.getChildren()) {
            traverseTree(transform, child, objects, supplier, matMap, hidMap, offsetMap, glCalls, skeleton, animations, hideDuringAnimation);
        }
    }

    private static void applyVariantDetails(Map<String, VariantDetails> applied, Map<String, VariantDetails> appliee) {
        for (Map.Entry<String, VariantDetails> entry : applied.entrySet()) {
            String k = entry.getKey();
            VariantDetails v = entry.getValue();
            appliee.compute(k, (s, variantDetails) -> {
                if (variantDetails == null) return v;
                return variantDetails.fillIn(v);
            });
        }
    }

    private static void applyVariant(Map<String, Material> materials, Map<String, Material> matMap, List<String> hideMap, Map<String, VariantDetails> variantMap) {
        variantMap.forEach((k, v) -> {
            matMap.put(k, materials.get(v.material()));
            if (v.hide() != null && v.hide()) hideMap.add(k);
        });
    }

    public static Vector3f calculateDimensions(GltfModel model) {
        var vec = new Vector3f();
        var pos = new Vector3f();

        for (var mesh : model.getMeshModels()) {
            for (var primitive : mesh.getMeshPrimitiveModels()) {

                var buf = primitive.getAttributes().get("POSITION").getBufferViewModel().getBufferViewData();

                var smallestVertexX = 0f;
                var smallestVertexY = 0f;
                var smallestVertexZ = 0f;
                var largestVertexX = 0f;
                var largestVertexY = 0f;
                var largestVertexZ = 0f;
                for (int i = 0; i < buf.capacity(); i += 12) { // Start at the y entry of every vertex and increment by 12 because there are 12 bytes per vertex
                    var xPoint = buf.getFloat(i);
                    var yPoint = buf.getFloat(i + 4);
                    var zPoint = buf.getFloat(i + 8);
                    smallestVertexX = Math.min(smallestVertexX, xPoint);
                    smallestVertexY = Math.min(smallestVertexY, yPoint);
                    smallestVertexZ = Math.min(smallestVertexZ, zPoint);
                    largestVertexX = Math.max(largestVertexX, xPoint);
                    largestVertexY = Math.max(largestVertexY, yPoint);
                    largestVertexZ = Math.max(largestVertexZ, zPoint);
                }

                vec.set(largestVertexX - smallestVertexX, largestVertexY - smallestVertexY, largestVertexZ - smallestVertexZ);

                pos.max(vec);
            }
        }

        return pos;
    }

    private static void applyTransforms(Matrix4f transform, NodeModel node) {
        if (node.getScale() != null) transform.scale(new Vector3f(node.getScale()));
        if (node.getRotation() != null)
            transform.rotate(new Quaternionf(node.getRotation()[0], node.getRotation()[1], node.getRotation()[2], node.getRotation()[3]));
        if (node.getTranslation() != null) {
            if (node.getTranslation().length == 3)
                transform.add(new Matrix4f().setTranslation(node.getTranslation()[0], node.getTranslation()[1], node.getTranslation()[2]));
            else
                transform.add(new Matrix4f().set(node.getTranslation()));
        }
    }

    public static <T extends MeshObject> void checkForRootTransformation(MultiRenderObject<T> objects, GltfModel gltfModel) {
        if (gltfModel.getSkinModels().isEmpty()) {
            var node = gltfModel.getNodeModels().get(0);
            while (node.getParent() != null) node = node.getParent();
            var rootTransformation = new Matrix4f().set(node.createGlobalTransformSupplier().get());
            objects.setRootTransformation(rootTransformation);
        }
    }

    private static <T extends MeshObject> void processPrimitiveModels(MultiRenderObject<T> objects, Supplier<T> objSupplier, MeshModel model, Map<String, Map<String, Material>> materialMap, Map<String, List<String>> hiddenMap, Map<String, Map<String, Vector2f>> offsetMap, List<Runnable> glCalls, @Nullable Skeleton skeleton, @Nullable Map<String, Animation> animations, Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimations) {
        var name = model.getName();

        var map = materialMap.get(name);
        var list = hiddenMap.get(name);
        var map1 = offsetMap.get(name);

        for (var primitiveModel : model.getMeshPrimitiveModels()) {
            var glModel = processPrimitiveModel(primitiveModel, glCalls);
            var renderObject = objSupplier.get();


            if (animations != null && renderObject instanceof AnimatedMeshObject animatedMeshObject) {
                animatedMeshObject.setup(map, list, map1, glModel, name, skeleton, animations, hideDuringAnimations.getOrDefault(name, ModelConfig.HideDuringAnimation.NONE));
            } else {
                renderObject.setup(map, list, map1, glModel, name);
            }

            objects.add(renderObject);
        }
    }

    private static GLModel processPrimitiveModel(MeshPrimitiveModel primitiveModel, List<Runnable> glCalls) {
        var model = new GLModel();
        var attributes = primitiveModel.getAttributes();

        glCalls.add(() -> {
            model.vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(model.vao);

//            // FIXME: this is for old models which dont get proper scaling for
//            var buf = position.getBufferViewModel().getBufferViewData();
//            var smallestVertexX = 0f;
//            var smallestVertexY = 0f;
//            var smallestVertexZ = 0f;
//            var largestVertexX = 0f;
//            var largestVertexY = 0f;
//            var largestVertexZ = 0f;
//            for (int i = 0; i < buf.capacity(); i += 12) { // Start at the y entry of every vertex and increment by 12 because there are 12 bytes per vertex
//                var xPoint = buf.getFloat(i);
//                var yPoint = buf.getFloat(i + 4);
//                var zPoint = buf.getFloat(i + 8);
//                smallestVertexX = Math.min(smallestVertexX, xPoint);
//                smallestVertexY = Math.min(smallestVertexY, yPoint);
//                smallestVertexZ = Math.min(smallestVertexZ, zPoint);
//                largestVertexX = Math.max(largestVertexX, xPoint);
//                largestVertexY = Math.max(largestVertexY, yPoint);
//                largestVertexZ = Math.max(largestVertexZ, zPoint);
//            }
//            model.dimensions = new Vector3f(largestVertexX - smallestVertexX, largestVertexY - smallestVertexY, largestVertexZ - smallestVertexZ);

            model.vbos = new int[attributes.containsKey("JOINTS_0") ? 5 : 3];

            bindAccessorModel(model, attributes, "POSITION", 0);

            var position = attributes.get("POSITION");
            DataUtils.bindArrayBuffer(position.getBufferViewModel());
            vertexAttribPointer(position, 0);

            var uvs = attributes.get("TEXCOORD_0");
            DataUtils.bindArrayBuffer(uvs.getBufferViewModel());
            vertexAttribPointer(uvs, 1);

            var normal = attributes.get("NORMAL");
            DataUtils.bindArrayBuffer(normal.getBufferViewModel());
            vertexAttribPointer(normal, 2);

            var joints = attributes.get("JOINTS_0");

            if (joints != null) {

                DataUtils.bindArrayBuffer(joints.getBufferViewModel());
                vertexAttribPointer(joints, 3);

                var weights = attributes.get("WEIGHTS_0");
                DataUtils.bindArrayBuffer(weights.getBufferViewModel());
                vertexAttribPointer(weights, 4);
            }

            model.ebo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, model.ebo);
            GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, DataUtils.makeDirect(primitiveModel.getIndices().getBufferViewModel().getBufferViewData()), GL15.GL_STATIC_DRAW);

            var mode = primitiveModel.getMode();
            model.meshDrawCommands.add(new MeshDrawCommand(model.vao, mode, primitiveModel.getIndices().getComponentType(), model.ebo, primitiveModel.getIndices().getCount()));
        });
        return model;
    }

    private static void bindAccessorModel(GLModel model, Map<String, AccessorModel> attributes, String name, int binding) {
        var position = attributes.get(name);
        model.vbos[binding] = DataUtils.bindArrayBuffer(position.getBufferViewModel());
        vertexAttribPointer(position, binding);
    }

    private static void vertexAttribPointer(AccessorModel data, int binding) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                data.getElementType().getNumComponents(),
                data.getComponentType(),
                false,
                data.getByteStride(),
                data.getByteOffset());
    }

    public static List<String> getVariants(GltfModel model) {
        try {
            if (model.getExtensions() == null || model.getExtensions().isEmpty() || !model.getExtensions().containsKey("KHR_materials_variants"))
                return null;

            var variantMap = (Map<String, Object>) model.getExtensions().get("KHR_materials_variants");
            var variantList = (List<Map<String, String>>) variantMap.get("variants");
            var variantNames = new ArrayList<String>();

            for (Map<String, String> a : variantList) {
                var name = a.get("name");
                variantNames.add(name);
            }
            return variantNames;
        } catch (Exception e) {
            throw new RuntimeException("Malformed Variant List in GLTF model.", e);
        }
    }

    public static <T> Map<String, T> createMeshVariantMap(MeshPrimitiveModel primitiveModel, List<T> materials, List<String> variantsList) {
        if (variantsList == null) {
            var materialId = primitiveModel.getMaterialModel().getName();
            return Collections.singletonMap("default", materials.stream().filter(a -> a.toString().equals(materialId)).findAny().get());
        } else {
            var map = (Map<String, Object>) primitiveModel.getExtensions().get("KHR_materials_variants");
            var mappings = (List<Map<String, Object>>) map.get("mappings");
            var variantMap = new HashMap<String, T>();

            for (var mapping : mappings) {
                if (!mapping.containsKey("material")) continue;
                var material = materials.get((Integer) mapping.get("material"));
                var variants = (List<Integer>) mapping.get("variants");

                for (var i : variants) {
                    var variant = variantsList.get(i);
                    variantMap.put(variant, material);
                }
            }

            return variantMap;
        }
    }

    public <T extends RenderObject> MultiRenderObject<T> createObject(@NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        var obj = new MultiRenderObject<T>();
        var task = threadedCreateObject(obj, is, objectCreator, onFinish);
        if (RareCandy.DEBUG_THREADS) task.run();
        else modelLoadingPool.submit(task);
        return obj;
    }

    private <T extends RenderObject> Runnable threadedCreateObject(MultiRenderObject<T> obj, @NotNull Supplier<PixelAsset> is, GlCallSupplier<T> objectCreator, Consumer<MultiRenderObject<T>> onFinish) {
        return ThreadSafety.wrapException(() -> {
            var asset = is.get();
            var config = asset.getConfig();

            var images = readImages(asset);

            if (asset.getModelFile() == null) return;

            if (config != null) obj.scale = config.scale;
            var model = read(asset);
            var smdAnims = readSmdAnimations(asset);
            var gfbAnims = readGfbAnimations(asset);
            var trAnims = readtrAnimations(asset);
            var glCalls = objectCreator.getCalls(model, smdAnims, gfbAnims, trAnims, images, config, obj);
            ThreadSafety.runOnContextThread(() -> {
                glCalls.forEach(Runnable::run);
                obj.updateDimensions();
                if (onFinish != null) onFinish.accept(obj);
            });
        });
    }

    private Map<String, String> readImages(PixelAsset asset) {
        var images = asset.getImageFiles();
        var map = new HashMap<String, String>();
        for (var entry : images) {
            var key = entry.getKey();

//            try {
                var id = asset.name + "-" + key;
                ITextureLoader.instance().register(id, key, entry.getValue());

                map.put(key, id);
//            } catch (IOException e) {
//                System.out.print("Error couldn't load: " + key); //TODO: Logger solution.
//            }
        }

        return map;
    }

    private Map<String, byte[]> readGfbAnimations(PixelAsset asset) {
        return asset.files.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(".pkx") || entry.getKey().endsWith(".gfbanm"))
                .collect(Collectors.toMap(this::cleanAnimName, Map.Entry::getValue));
    }

    private HashMap<String, Pair<byte[], byte[]>> readtrAnimations(PixelAsset asset) {
        var map = new HashMap<String, Pair<byte[], byte[]>>();

        var list = asset.files.keySet().stream().filter(a -> a.endsWith("tranm") || a.endsWith("tracm")).collect(Collectors.toCollection(ArrayList::new));

        while (!list.isEmpty()) {
            var a = list.remove(0);

            if (a.endsWith(".tranm")) {
                var index = list.indexOf(a.replace(".tranm", ".tracm"));

                if (index != -1) {
                    var b = list.remove(index);

                    map.put(a.replace(".tranm", ""), new Pair<>(asset.files.get(a), asset.files.get(b)));
                } else {
                    map.put(a.replace(".tranm", ""), new Pair<>(asset.files.get(a), null));
                }
            } else {
                if (a.endsWith(".tracm")) {
                    var index = list.indexOf(a.replace(".tracm", ".tranm"));

                    if (index != -1) {
                        var b = list.remove(index);

                        map.put(a.replace(".tracm", ""), new Pair<>(asset.files.get(b), asset.files.get(a)));
                    } else {
                        map.put(a.replace(".tracm", ""), new Pair<>(null, asset.files.get(a)));
                    }
                }
            }

        }

        return map;
    }


    public String cleanAnimName(Map.Entry<String, byte[]> entry) {
        var str = entry.getKey();
        return cleanAnimName(str);
    }

    public String cleanAnimName(String str) {
        var substringEnd = str.lastIndexOf(".") == -1 ? str.length() : str.lastIndexOf(".");
        var substringStart = str.lastIndexOf("/") == -1 ? 0 : str.lastIndexOf("/");
        return str.substring(substringStart, substringEnd);
    }

    private Map<String, SMDFile> readSmdAnimations(PixelAsset pixelAsset) {
        var files = pixelAsset.getAnimationFiles();
        var map = new HashMap<String, SMDFile>();
        var reader = new SMDTextReader();

        for (var entry : files) {
            var smdFile = reader.read(new String(entry.getValue()));
            map.put(cleanAnimName(entry.getKey().replace(".smd", "")), smdFile);
        }

        return map;
    }

    public void close() {
        modelLoadingPool.shutdown();
    }

    public GltfModel read(PixelAsset asset) {
        try {
            return reader.readWithoutReferences(new ByteArrayInputStream(asset.getModelFile()));
        } catch (IOException e) {
            throw new RuntimeException("Issue reading GLTF Model", e);
        }
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

}