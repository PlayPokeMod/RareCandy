#version 150 core

in vec2 texCoord0;
out vec4 outColor;

uniform sampler2D diffuse;

vec4 getColor() {
    return texture(diffuse, texCoord0);
}

void main() {
    outColor = getColor();
}
