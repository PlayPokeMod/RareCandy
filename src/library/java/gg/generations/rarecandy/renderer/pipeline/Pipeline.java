package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public record Pipeline(Map<String, Consumer<UniformUploadContext>> uniformSuppliers, Map<String, Uniform> uniforms,
                       Consumer<Material> preDrawBatch, Consumer<Material> postDrawBatch, int program) {

    public void bind(Material material) {
        GL20C.glUseProgram(program);
        preDrawBatch.accept(material);
    }

    public void unbind(Material material) {
        postDrawBatch.accept(material);
    }

    public void updateOtherUniforms(ObjectInstance instance, RenderObject renderObject) {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if (!uniformSuppliers.containsKey(name))
                RareCandy.fatal("No handler for uniform with name \"" + name + "\"");
            if (uniform.type != GL20C.GL_SAMPLER_2D)
                uniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, instance, uniform));
        }
    }

    public void updateTexUniforms(ObjectInstance instance, RenderObject renderObject) {
        for (var name : uniforms.keySet()) {
            var uniform = uniforms.get(name);
            if (!uniformSuppliers.containsKey(name))
                RareCandy.fatal("No handler for uniform with name \"" + name + "\"");
            if (uniform.type == GL20C.GL_SAMPLER_2D)
                uniformSuppliers.get(name).accept(new UniformUploadContext(renderObject, instance, uniform));
        }
    }

    public static class Builder {

        public Map<String, Uniform> uniforms = new HashMap<>();
        public Consumer<Material> preDrawBatch = material -> {
        };
        public Consumer<Material> postDrawRunBatch = material -> {
        };
        private Map<String, Consumer<UniformUploadContext>> uniformSuppliers = new HashMap<>();
        private int program;

        public Builder() {
        }

        public Builder(Builder base) {
            this.uniformSuppliers = new HashMap<>(base.uniformSuppliers);
            this.program = base.program;
            this.uniforms = new HashMap<>(base.uniforms);
            this.preDrawBatch = base.preDrawBatch;
            this.postDrawRunBatch = base.postDrawRunBatch;
        }

        private void addShader(String text, int type, int programId) {
            var shader = GL20C.glCreateShader(type);
            if (shader == 0) RareCandy.fatal("an error occurred creating the shader object. We don't know what it is.");
            GL20C.glShaderSource(shader, text);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetShaderInfoLog(shader, 1024));
            GL20C.glAttachShader(programId, shader);
        }

        private void compileShader(int programId) {
            GL20C.glLinkProgram(programId);
            if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == 0)
                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));

//I'm suspecting that macs validate differently than windows. So Naiviely disable this check for now. - Waterpicker.
//            GL20C.glValidateProgram(programId);
//            if (GL20C.glGetProgrami(programId, GL20C.GL_VALIDATE_STATUS) == 0)
//                RareCandy.fatal(GL20C.glGetProgramInfoLog(programId, 1024));
        }

        public Builder prePostDraw(Consumer<Material> preDrawBatch, Consumer<Material> postDrawRunBatch) {
            this.preDrawBatch = preDrawBatch;
            this.postDrawRunBatch = postDrawRunBatch;
            return this;
        }

        public Builder supplyUniform(String name, Consumer<UniformUploadContext> provider) {
            uniformSuppliers.put(name, provider);
            return this;
        }

        public Builder shader(@NotNull String vs, @NotNull String fs) {
            program = GL20C.glCreateProgram();
            addShader(vs, GL20C.GL_VERTEX_SHADER, program);
            addShader(fs, GL20C.GL_FRAGMENT_SHADER, program);
            compileShader(program);

            try (var stack = MemoryStack.stackPush()) {
                var pUniformCount = stack.ints(1);
                GL20C.glGetProgramiv(program, GL20C.GL_ACTIVE_UNIFORMS, pUniformCount);
                var uniformCount = pUniformCount.get(0);

                var pMaxNameLength = stack.ints(1);
                GL20C.glGetProgramiv(program, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH, pMaxNameLength);

                for (int i = 0; i < uniformCount; i++) {
                    var pSize = stack.ints(1);
                    var pType = stack.ints(1);
                    var name = GL20C.glGetActiveUniform(program, i, pMaxNameLength.get(0), pSize, pType);

                    if (name.contains("[")) {
                        name = name.substring(0, name.indexOf('['));
                    }

                    this.uniforms.put(name, new Uniform(program, name, pType.get(0), pSize.get(0)));
                }
            }
            return this;
        }

        public Pipeline build() {
            if (this.program == 0) throw new RuntimeException("Shader not created");

            return new Pipeline(uniformSuppliers, uniforms, preDrawBatch, postDrawRunBatch, program);
        }

        public Builder configure(Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }
    }
}
