#version 150 core

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D layer;
uniform sampler2D mask;

//base
uniform vec3 baseColor1;
uniform vec3 baseColor2;
uniform vec3 baseColor3;
uniform vec3 baseColor4;
uniform vec3 baseColor5;

//emi
uniform vec3 emiColor1;
uniform vec3 emiColor2;
uniform vec3 emiColor3;
uniform vec3 emiColor4;
uniform vec3 emiColor5;
uniform float emiIntensity1;
uniform float emiIntensity2;
uniform float emiIntensity3;
uniform float emiIntensity4;
uniform float emiIntensity5;

uniform sampler2D paradoxMask;
uniform int frame;

float getParadoxIntensity() {
    vec2 effectTexCoord = vec2(texCoord0);

    if(frame >= 0) {
        effectTexCoord *= 4.0;
        effectTexCoord = fract(effectTexCoord);

        effectTexCoord *= (0.25);
        effectTexCoord.x += (frame % 4)/4.0;
        effectTexCoord.y +=  (frame/4)/4.0;
    }

    return texture(paradoxMask, effectTexCoord).r;
}

vec4 process(vec4 color) {
    return mix(color, vec4(1.0), getParadoxIntensity());
}

float adjustScalar(float color) {
    return clamp(color * 2, 0.0, 1.0);
}

vec4 adjust(vec4 color) {
    return clamp(color * 2, 0, 1);
}

vec3 applyEmission(vec3 base, vec3 emissionColor, float intensity) {
    return base + (emissionColor - base) * intensity;
}

float getMaskIntensity() {
    return clamp(texture(mask, texCoord0).r * 2, 0.0, 1.0);
}

vec4 getColor() {
    vec2 texCoord = texCoord0;
    vec4 color = texture(diffuse, texCoord);
    vec4 layerMasks = clamp(texture(layer, texCoord) * 2, 0, 1);
    float maskColor = getMaskIntensity();

    vec3 base = mix(color.rgb, color.rgb * baseColor1, layerMasks.r);
    base = mix(base, color.rgb * baseColor2, layerMasks.g);
    base = mix(base, color.rgb * baseColor3, layerMasks.b);
    base = mix(base, color.rgb * baseColor4, layerMasks.a);
    base = mix(base, color.rgb * baseColor5, maskColor);

    base = mix(base, applyEmission(base, emiColor1, emiIntensity1), layerMasks.r);
    base = mix(base, applyEmission(base, emiColor2, emiIntensity2), layerMasks.g);
    base = mix(base, applyEmission(base, emiColor3, emiIntensity3), layerMasks.b);
    base = mix(base, applyEmission(base, emiColor4, emiIntensity4), layerMasks.a);
    base = mix(base, applyEmission(vec3(0), emiColor5, emiIntensity5), maskColor);

    return vec4(base, color.a);
}

void main() {
    outColor = getColor();

    if(outColor.a < 0.004) discard;
}
