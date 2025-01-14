package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.LoggerUtil;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.AnimationInstance;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.rendering.FrameBuffer;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.renderer.rendering.ScreenRenderer;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.tools.TextureLoader;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.lwjgl.util.nfd.NativeFileDialog;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;


public class RareCandyCanvas extends AWTGLCanvas {
    private static CycleVariants runnable;
    public static Matrix4f projectionMatrix;
    public static float radius = 2.0f;

    private final ModelLoader loader = new ModelLoader();

    public static float lightLevel = 1;
    private static double time;
    public static FrameBuffer framebuffer;

    public final Matrix4f viewMatrix = new Matrix4f();
    public final List<AnimatedObjectInstance> instances = new ArrayList<>();
    public float scaleModifier = 0;
    private final PokeUtilsGui handler;
    public double startTime = System.currentTimeMillis();
    public String currentAnimation = null;
    public double originalScaleModifer;
    private RareCandy renderer;
    private MultiRenderObject<MeshObject> plane;
    private ObjectInstance planeInstance;

    public ToggleableMultiRenderObject loadedModel;
    public AnimatedObjectInstance loadedModelInstance;
    private static float previousLightLevel;
    private String fileName;
    public static boolean cycling;
    private ScreenRenderer screenRenderer;
    public static boolean animate = true;
    public static boolean renderingFrame;
    private MultiRenderObject<MeshObject> cube;
    private ObjectInstance[] cubeInstances;

    public static void setLightLevel(float lightLevel) {
        previousLightLevel = RareCandyCanvas.lightLevel;
        RareCandyCanvas.lightLevel = lightLevel;
    }

     static float getLightLevel() {
        return lightLevel;
    }

    public RareCandyCanvas(PokeUtilsGui handler) {
        super(defaultData());
        this.handler = handler;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
            }
        });

        NativeFileDialog.NFD_Init();
//        root = DialogueUtils.chooseFolder();
//        if(root == null) root = Path.of("pack");
    }

    private static GLData defaultData() {
        var data = new GLData();
        data.profile = GLData.Profile.CORE;
        data.forwardCompatible = true;
        data.api = GLData.API.GL;
        data.majorVersion = 3;
        data.minorVersion = 2;
        return data;
    }

    public static double getTime() {
        return time;
    }

    public static void setup(RareCandyCanvas canvas) {

        ITextureLoader.setInstance(new TextureLoader());

        var renderLoop = new Runnable() {
            @Override
            public void run() {
                if (canvas.isValid()) canvas.render();
                SwingUtilities.invokeLater(this);
            }
        };

        SwingUtilities.invokeLater(renderLoop);
    }

    public void openFile(PixelAsset pkFile, String name) throws IOException {
        openFile(pkFile, name, () -> {}, true);
    }

    public void openFile(PixelAsset pkFile, String name, Runnable runnable, boolean resetAnimation) throws IOException {
        currentAnimation = null;
        renderer.objectManager.clearObjects();
        renderer.objectManager.add(plane, planeInstance);

//        for (ObjectInstance instance : cubeInstances) {
//            renderer.objectManager.add(cube, instance);
//        }

        if(loadedModel != null) loadedModel.close();

        this.fileName = name;

        if(pkFile == null) return;

        loadPokemonModel(pkFile, model -> {
            var i = 0;

            loadedModel = (ToggleableMultiRenderObject) model;

            scaleModifier = loadedModel.scale;
            originalScaleModifer = loadedModel.scale;

            handler.scale.reset();

            var variants = model.availableVariants();

            var variant = !variants.isEmpty() ? variants.iterator().next() : null;
            var instance = new AnimatedObjectInstance(new Matrix4f(), viewMatrix, variant);

            loadedModelInstance = renderer.objectManager.add(model, instance);
            model.updateDimensions();
            runnable.run();

            if(resetAnimation) setAnimation("idle");

        });
    }

    @Override
    public void initGL() {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize();
        this.renderer = new RareCandy();

        GL11C.glClearColor(0, 0, 0, 0);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        framebuffer = new FrameBuffer(1024, 1024);

//        screenRenderer = new ScreenRenderer(framebuffer);


        loadPlane(100, 100, model -> {
            plane = model;
            planeInstance = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0f, -0.001f, 0f), viewMatrix, null));
        });

//        loadCube(1, 1, 1, model -> {
//            cube = model;
//            cubeInstances = new ObjectInstance[4];
//            cubeInstances[0] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, -0.5f, 0), viewMatrix, null));
//            cubeInstances[1] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, 0.5f, -1), viewMatrix, null));
//            cubeInstances[2] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, 1.5f, -1), viewMatrix, null));
//            cubeInstances[3] = renderer.objectManager.add(model, new ObjectInstance(new Matrix4f().translation(0, 02.5f, -1), viewMatrix, null));
//        });
    }

    private MultiRenderObject<MeshObject> loadPlane(int width, int length, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        return loader.generatePlane(width, length, onFinish);
    }

    private MultiRenderObject<MeshObject> loadCube(int width, int length, int height, Consumer<MultiRenderObject<MeshObject>> onFinish) {
        return loader.generateCube(width, length, height, "smooth_stone", onFinish);
    }


    private final Vector3f size = new Vector3f();

    private final double fraciton = 1/16f;
    @Override
    public void paintGL() {
        if (loadedModelInstance != null) {
            loadedModelInstance.transformationMatrix().identity().scale(scaleModifier);

            size.set(loadedModel.dimensions).mul(scaleModifier);
        }

        if(animate) time = (System.currentTimeMillis() - startTime) / 1000f;

        if (runnable != null) runnable.pre();

        renderToFramebuffer();
        renderToScreen();

        if (runnable != null) runnable.post();
        swapBuffers();

        if (instances.size() > 1) {
            ((MultiRenderObject<AnimatedMeshObject>) instances.get(0).object()).onUpdate(a -> {
                for (var instance : instances) {
                    if(a.animations != null) {
                        var newAnimation = a.animations.get(currentAnimation);

                        if(newAnimation != null) {
                            instance.changeAnimation(createInstance(newAnimation));
                        }
                    }
                }
            });
        }
    }

    private final int[] originalViewport = new int[4]; // Array to store x, y, width, height

    private void renderToFramebuffer() {
        renderingFrame = true;
        framebuffer.bindFramebuffer();

        glGetIntegerv(GL_VIEWPORT, originalViewport);

        GL11C.glViewport(0, 0, 1024, 1024);


        GL11C.glClearColor(0.3f, 0.3f, 0.5f, 0.0f); // Ensure alpha is set to 0 for transparency
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

        renderer.render(false, time);

        framebuffer.unbindFramebuffer();

        glViewport(originalViewport[0], originalViewport[1], originalViewport[2], originalViewport[3]);
        renderingFrame = false;
    }

    private void renderToScreen() {

//        BlendType.Regular.enable();

        GL11C.glClearColor(0,0,0, 1f); // Ensure alpha is set to 0 for transparency
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);

//        ObjectManager.render(plane, planeInstance);
        renderer.render(false, time);

//        BlendType.Regular.disable();
    }

    public AnimationInstance createInstance(Animation animation) {
        return new AnimationInstance(animation);
    }

    protected void loadPokemonModel(PixelAsset is, Consumer<MultiRenderObject<AnimatedMeshObject>> onFinish) {
        loader.createObject(
                ToggleableMultiRenderObject::new,
                () -> is,
                (gltfModel, animResources, images, config, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    ModelLoader.processModel(object, gltfModel, animResources, images, config, glCalls, AnimatedMeshObject::new, GLModel::new);
                    return glCalls;
                }, onFinish);
    }

    public void setAnimation(@NotNull String animation) {
        AnimatedMeshObject object = loadedModel.objects.get(0);

//        if (object.animations != null)
//            LoggerUtil.print(animation);
        if (Objects.requireNonNull(object.animations).containsKey(animation)) {
            loadedModelInstance.changeAnimation(createInstance(object.animations.get(animation)));
        }
    }

    public void updateLoadedModel(Consumer<AnimatedMeshObject> consumer) {
        loadedModel.onUpdate(consumer);
    }

    public void setVariant(String variant) {
        loadedModelInstance.setVariant(variant);
    }

    public void attachArcBall(GuiHandler.ArcballOrbit arcballOrbit) {
        this.addMouseMotionListener(arcballOrbit);
        this.addMouseWheelListener(arcballOrbit);
        this.addMouseListener(arcballOrbit);
    }

    public void toggleObject(boolean add, String object) {
        if(add) loadedModel.overrides.add(object);
        else loadedModel.overrides.remove(object);
    }


    public static final Path images = Path.of("assets", "generations_core", "textures", "pokemon");

    private Path root = Path.of("images");

    public void takeScreenshot(boolean isPortrait) throws IOException {
        var path = root.resolve(fileName);
        if(Files.notExists(path)) Files.createDirectories(path);

        var temp = Path.of((path + "\\" + (isPortrait ? "portrait" : "profile") + "-" + (loadedModelInstance.variant() != null ? loadedModelInstance.variant() : "default") + ".png").replace("\\", "/"));
        if(framebuffer.captureScreenshot(temp, isPortrait)) {
            LoggerUtil.print("Screenshot saved to " + temp);
        } else {
            LoggerUtil.print("Failed to save screenshot to " + temp);
        }
    }

    public static class CycleVariants  {
        private final RareCandyCanvas canvas;
        private final boolean isPortrait;
        private List<String> list;
        private int index;

        public CycleVariants(RareCandyCanvas canvas, boolean isPortrait) {
            this.canvas = canvas;
            this.isPortrait = isPortrait;
            if(canvas.loadedModel == null) return;
            RareCandyCanvas.cycling = true;
            list = List.copyOf(canvas.loadedModel.availableVariants());

            if(list.size() == 0) return;

            index = 0;
            RareCandyCanvas.runnable = this;
        }
        public void pre() {
            if(index >= list.size()) {
                RareCandyCanvas.runnable = null;
                cycling = false;
            } else {
                canvas.loadedModelInstance.setVariant(list.get(index));
            }
        }

        public void post() {
            try {
                canvas.takeScreenshot(isPortrait);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            index += 1;
        }
    }

    public static class ToggleableMultiRenderObject extends MultiRenderObject<AnimatedMeshObject> {
        public List<String> overrides = new ArrayList<>();

        @Override
        public <V extends RenderObject> void render(List<ObjectInstance> instances) {
            for (var object : this.objects) {
                if (object != null && !overrides.contains(object.name) && object.isReady()) {
                    object.render(instances);
                }
            }
        }

        @Override
        public <V extends RenderObject> void render(ObjectInstance instance) {
            for (var object : this.objects) {
                if (object != null && !overrides.contains(object.name) && object.isReady()) {
                    object.render(instance);
                }
            }
        }
    }
}

