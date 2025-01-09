package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.animation.AnimationController;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.projectionMatrix;
import static java.lang.Math.floor;

public class GuiPipelines {
    private static final Pipeline.Builder ROOT = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("boneTransforms", ctx -> {
                var mats = ctx.instance() instanceof AnimatedObjectInstance instance ? instance.getTransforms() != null ? instance.getTransforms() : AnimationController.NO_ANIMATION : AnimationController.NO_ANIMATION;
                ctx.uniform().uploadMat4fs(mats);
            })
            .supplyUniform("offset", ctx -> {
                Transform transform = ctx.object().getTransform(ctx.instance().variant());

                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());

                    if (t != null) {
                        transform = t;
                    }
                }

                ctx.uniform().uploadVec2f(transform.offset());
            })
            .supplyUniform("scale", ctx -> {
                Transform transform = ctx.object().getTransform(ctx.instance().variant());

                if (ctx.instance() instanceof AnimatedObjectInstance instance) {
                    var t = instance.getTransform(ctx.getMaterial().getMaterialName());

                    if (t != null) {
                        transform = t;
                    }
                }

                ctx.uniform().uploadVec2f(transform.scale());
            })
            .prePostDraw(material -> {
                if(material.getBoolean("disableDepth")) {
                    GL11.glDisable(GL11.GL_DEPTH_TEST);
                }

                material.cullType().enable();
                material.blendType().enable();
            }, material -> {
                if(material.getBoolean("disableDepth")) {
                    GL11.glEnable(GL11.GL_DEPTH_TEST);
                }

                material.cullType().disable();
                material.blendType().disable();
            });

    public static final Pipeline PLANE = new Pipeline.Builder()
            .supplyUniform("viewMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().viewMatrix()))
            .supplyUniform("modelMatrix", ctx -> ctx.uniform().uploadMat4f(ctx.instance().transformationMatrix()))
            .supplyUniform("projectionMatrix", (ctx) -> ctx.uniform().uploadMat4f(projectionMatrix))
            .supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()))
            .supplyUniform("radius", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.radius))
            .supplyUniform("render", ctx -> ctx.uniform().uploadBoolean(RareCandyCanvas.renderingFrame))
            .prePostDraw(material -> BlendType.Regular.enable(), material -> BlendType.Regular.disable())
            .shader(builtin("animated/plane.vs.glsl"), builtin("animated/plane.fs.glsl")).build();

    public static final Pipeline SCREEN_QUAD = new Pipeline.Builder()
            .supplyUniform("screenTexture", ctx -> {
                RareCandyCanvas.framebuffer.bind(0);
                ctx.uniform().uploadInt(0);
            })
            .shader(builtin("screen/screen_quad.vs.glsl"),
                    builtin("screen/screen_quad.fs.glsl")).build();

    private static final Pipeline.Builder BASE = new Pipeline.Builder(ROOT)
            .configure(GuiPipelines::addDiffuse)
            .configure(GuiPipelines::addLight);

    private static void addDiffuse(Pipeline.Builder builder) {
        builder.supplyUniform("diffuse", ctx -> {
            var texture = ctx.object().getMaterial(ctx.instance().variant()).getDiffuseTexture();

            if(texture == null) {
                texture = ITextureLoader.instance().getNuetralFallback();
            }

            texture.bind(0);
            ctx.uniform().uploadInt(0);
        });
    }

    private static void baseColors(Pipeline.Builder builder) {
        builder.supplyUniform("baseColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("baseColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("baseColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE));
    }

    private static void addLight(Pipeline.Builder builder) {
        builder.supplyUniform("lightLevel", ctx -> ctx.uniform().uploadFloat(RareCandyCanvas.getLightLevel()))
                .supplyUniform("emission", ctx -> {
                    var texture = ctx.object().getMaterial(ctx.instance().variant()).getTexture("emission");

                    if(texture == null) {
                        texture = ITextureLoader.instance().getDarkFallback();
                    }

                    texture.bind(1);
                    ctx.uniform().uploadInt(1);
                })
                .supplyUniform("useLight", ctx -> ctx.uniform().uploadBoolean(ctx.getValue("useLight") instanceof Boolean bool ? bool : true));
    }

    public static final Pipeline.Builder LAYERED_BASE = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/layered.fs.glsl"))
            .configure(GuiPipelines::baseColors)
            .configure(GuiPipelines::emissionColors)
            .supplyUniform("layer", ctx -> {
                var texture = ctx.getTexture("layer");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();


                texture.bind(2);
                ctx.uniform().uploadInt(2);
            }).supplyUniform("mask", ctx -> {
                var texture = ctx.getTexture("mask");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();

                texture.bind(3);
                ctx.uniform().uploadInt(3);
            });

    public static final Pipeline LAYERED = new Pipeline.Builder(LAYERED_BASE)
            .supplyUniform("frame", ctx -> ctx.uniform().uploadInt(-1))
            .build();

    public static final Pipeline SOLID = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/solid.fs.glsl"))
            .build();
    public static final Pipeline CARTOON = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("process/cartoon.fs.glsl"))
            .build();
    public static final Pipeline PASTEL = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("process/pastel.fs.glsl"))
            .build();
    public static final Pipeline SHADOW = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("process/shadow.fs.glsl"))
            .build();

    public static final Pipeline VINTAGE = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("process/vintage.fs.glsl"))
            .build();
    public static final Pipeline SKETCH = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("process/sketch.fs.glsl"))
            .build();
    public static final Pipeline MASKED = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/masked.fs.glsl"))
            .supplyUniform("diffuse", ctx -> {
                var texture = ctx.object().getMaterial(ctx.instance().variant()).getDiffuseTexture();

                if(texture == null) {
                    texture = ITextureLoader.instance().getBrightFallback();
                }

                texture.bind(0);
                ctx.uniform().uploadInt(0);
            })
            .supplyUniform("mask", ctx -> {

                var texture = ctx.getTexture("mask");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();

                texture.bind(2);
                ctx.uniform().uploadInt(2);
            })
            .supplyUniform("color", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("color") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
            .build();

    public static final Pipeline PARADOX = new Pipeline.Builder(LAYERED_BASE)
            .supplyUniform("frame", ctx -> {
                var i = (int) pingpong(RareCandyCanvas.getTime() % 1d);

                ctx.uniform().uploadInt(i);
            }).build();

    public static final Pipeline.Builder GALAXY_BASE = new Pipeline.Builder(BASE)
            .shader(builtin("animated/animated.vs.glsl"), builtin("animated/galaxy.fs.glsl"))
            .configure(GuiPipelines::baseColors)
            .configure(GuiPipelines::emissionColors)
            .supplyUniform("frame", ctx -> {
                var i = (int) pingpong((RareCandyCanvas.getTime()) % 1d); // Scale time
                ctx.uniform().uploadInt(i);

            }).supplyUniform("layer", ctx -> {
                var texture = ctx.getTexture("layer");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();


                texture.bind(2);
                ctx.uniform().uploadInt(2);
            }).supplyUniform("mask", ctx -> {
                var texture = ctx.getTexture("mask");

                if(texture == null) texture = ITextureLoader.instance().getDarkFallback();

                texture.bind(3);
                ctx.uniform().uploadInt(3);
            });
    public static final Pipeline GALAXY = new Pipeline.Builder(GALAXY_BASE)
            .supplyUniform("frame", ctx -> {
                double slowdownFactor = 2;
                var i = (int) pingpong((RareCandyCanvas.getTime() / slowdownFactor) % 1d); // Scale time
                ctx.uniform().uploadInt(i);

            }).build();


    public static double fract(double a) {
        return a - floor(a);
    }

    public static double pingpong(double time) {
        return (int) (Math.sin(time * Math.PI * 2) * 7 + 7);
    }
    private static void emissionColors(Pipeline.Builder builder) {
        builder.supplyUniform("emiColor1", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor1") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor2", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor2") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor3", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor3") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor4", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor4") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiColor5", ctx -> ctx.uniform().uploadVec3f(ctx.getValue("emiColor5") instanceof Vector3f vec ? vec : GuiPipelines.ONE))
                .supplyUniform("emiIntensity1", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity1") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity2", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity2") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity3", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity3") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity4", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity4") instanceof Float vec ? vec : 0.0f))
                .supplyUniform("emiIntensity5", ctx -> ctx.uniform().uploadFloat(ctx.getValue("emiIntensity5") instanceof Float vec ? vec : 1.0f));
    }
    private static final Vector3f ONE = new Vector3f(1,1, 1);



    public static void onInitialize() {
    }

    private static String builtin(String name) {
        try (var is = Pipeline.class.getResourceAsStream("/shaders/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }
}