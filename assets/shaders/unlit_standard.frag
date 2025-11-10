#version 330 core

in vec3 aPos;
in vec3 aNormal;
in vec2 aTexCoord;

out vec4 fragColor;

struct Material {
    vec3 color;                // base tint
    sampler2D albedoMap;       // optional texture
    int hasAlbedoMap;          // 1 if using texture, 0 if not
};

uniform Material material;

void main() {
    vec3 baseColor = material.color;
    
    baseColor *= material.hasAlbedoMap * texture(material.albedoMap, aTexCoord).rgb;

    fragColor = vec4(baseColor, 1.0);
}