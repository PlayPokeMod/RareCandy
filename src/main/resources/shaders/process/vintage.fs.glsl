#version 150 core
#define ambientLight 0.6f
in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    vec4 color = texture(diffuse, texCoord0);

    float grayscale = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;

    outColor = vec4(vec3(grayscale), color.a);
}
