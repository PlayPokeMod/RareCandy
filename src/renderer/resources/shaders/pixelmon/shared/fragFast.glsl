#version 330 core
#pragma optionNV(strict on)

in vec2 outTexCoords;
in vec3 inNormal;
in vec3 toLightVector;
in vec3 toCameraVector;

out vec4 outColor;

uniform int intColor;
uniform float shineDamper;
uniform float reflectivity;
uniform float diffuseColorMix;

uniform sampler2D diffuse;

const float ambientLight = 0.0f;

vec3 intToColor() {
    return vec3((intColor >> 16 & 255) / 255.0, (intColor >> 8 & 255) / 255.0, (intColor & 255) / 255.0);
}

void main() {
    vec4 color = texture2D(diffuse, texCoord0);

    vec3 lightColor = intToColor();
    vec3 pixelmonColor = mix(lightColor, vec3(1.0, 1.0, 1.0), diffuseColorMix);
    vec3 unitNormal = normalize(inNormal);
    vec3 unitLightVector = normalize(toLightVector);
    vec3 lightDir = -unitLightVector;
    vec3 unitToCameraVector = normalize(toCameraVector);

    // Diffuse Lighting
    float rawDiffuse = dot(unitNormal, unitLightVector);
    float diffuse = max(rawDiffuse, ambientLight);
    vec3 coloredDiffuse = diffuse * pixelmonColor;

    // Specular Lighting
    vec3 reflectedLightDir = reflect(lightDir, unitNormal);
    float rawSpecularFactor = dot(reflectedLightDir, unitToCameraVector);
    float specularFactor = max(rawSpecularFactor, 0.0f);
    float dampedFactor = pow(specularFactor, shineDamper);
    vec3 finalSpecular = dampedFactor * reflectivity * lightColor;

    outColor = vec4(coloredDiffuse, 1.0f) * color + vec4(finalSpecular, 1.0f);
}
