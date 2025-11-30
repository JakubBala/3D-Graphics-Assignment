#version 330 core

in vec2 aTexCoord;

out vec4 fragColor;

struct Material {
    vec3 color;              // Base color (used when no albedo map)
    
    sampler2D albedoMap;
    sampler2D emissionMap;
    
    int hasAlbedoMap;
    int hasEmissionMap;
};

uniform Material material;

void main() {
    // Base color
    vec3 color = material.color;
    
    // Override with albedo texture if present
    if (material.hasAlbedoMap == 1) {
        vec4 texColor = texture(material.albedoMap, aTexCoord);
        color = texColor.rgb;
    }
    
    // Add emission (glowing parts)
    if (material.hasEmissionMap == 1) {
        color += texture(material.emissionMap, aTexCoord).rgb;
    }
    
    fragColor = vec4(color, 1.0);
}