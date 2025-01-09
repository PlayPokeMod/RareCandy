package gg.generations.rarecandy.tools.gui.imgui;

import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import org.joml.Vector3f;

import java.io.IOException;

public class GuiPipelines {
    private static final Pipeline.Builder ROOT = new Pipeline.Builder();

    private static final Pipeline.Builder BASE = new Pipeline.Builder()
            .configure(GuiPipelines::addDiffuse);

    private static void addDiffuse(Pipeline.Builder builder) {
        builder.supplyUniform("diffuse", ctx -> {
            var texture = ctx.getMaterial().getDiffuseTexture();

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

    public static final Pipeline.Builder LAYERED_BASE = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("layered.fs.glsl"))
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
    public static final Pipeline VINTAGE = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("vintage.fs.glsl"))
            .build();
    public static final Pipeline SKETCH = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("sketch.fs.glsl"))
            .build();
    public static final Pipeline CARTOON = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("cartoon.fs.glsl"))
            .build();
    public static final Pipeline PASTEL = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("pastel.fs.glsl"))
            .build();
    public static final Pipeline SHADOW = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("shadow.fs.glsl"))
            .build();
    public static final Pipeline.Builder GALAXY_BASE = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("galaxy.fs.glsl"))
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
    public static final Pipeline GALAXY = new Pipeline.Builder(GALAXY_BASE)
            .supplyUniform("frame", ctx -> ctx.uniform().uploadInt(-1))
            .build();


    public static final Pipeline SOLID = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("solid.fs.glsl"))
            .build();
    public static final Pipeline MASKED = new Pipeline.Builder(BASE)
            .shader(builtin("animated.vs.glsl"), builtin("masked.fs.glsl"))
            .supplyUniform("diffuse", ctx -> {
                var texture = ctx.getMaterial().getDiffuseTexture();

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

    private static String builtin(String name) {
        try (var is = Pipeline.class.getResourceAsStream("/shaders/process/" + name)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read built in shader", e);
        }
    }

    public static void init() {

        PipelineRegistry.setFunction(s-> switch(s) {
            case "masked" -> GuiPipelines.MASKED;
            case "layered" -> GuiPipelines.LAYERED;
            case "paradox" -> GuiPipelines.LAYERED;
            case "galaxy" -> GuiPipelines.GALAXY;
            case "cartoon" -> GuiPipelines.CARTOON;
            case "pastel" -> GuiPipelines.PASTEL;
            case "shadow" -> GuiPipelines.SHADOW;
            case "vintage" -> GuiPipelines.VINTAGE;
            case "sketch" -> GuiPipelines.SKETCH;
            default -> GuiPipelines.SOLID;
        });
    }
}