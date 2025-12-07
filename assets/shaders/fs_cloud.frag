#version 330 core

in vec2 aTexCoord;

out vec4 fragColor;

struct Material {
    vec3 tint; // tint color (RGB)
    float opacity; // global opacity multiplier (0-1)

    sampler2D albedoMap;
    int hasAlbedoMap;
};

uniform Material material;

void main() {
    vec3 baseColor = material.tint;
    float alpha = material.opacity;

    if (material.hasAlbedoMap == 1) {
        vec4 tex = texture(material.albedoMap, aTexCoord);
        
        // Multiply texture color by tint
        baseColor *= tex.rgb;

        // Multiply alpha channel by global opacity
        alpha *= tex.a;
    }

    // discard very transparent edges
    if (alpha < 0.01)
        discard;

    fragColor = vec4(baseColor, alpha);
}