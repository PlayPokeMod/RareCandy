#version 150 core
#define ambientLight 0.6f
in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

vec4 process(vec4 inColor) {
    float grayscale = 0.2126 * inColor.r + 0.7152 * inColor.g + 0.0722 * inColor.b;

    return vec4(vec3(grayscale), inColor.a);
}

void main() {
    vec4 color = texture(diffuse, texCoord0);

    outColor = process(color);
}
