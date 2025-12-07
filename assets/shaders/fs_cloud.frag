#version 330 core

in vec2 aTexCoord;

out vec4 fragColor;

struct Material {
    vec3 tint; // tint color (RGB)
    float opacity; // global opacity multiplier (0-1)

    sampler2D albedoMap;
    int hasAlbedoMap;
};

uniform float timeOfDay; // 0.0 = night, 0.25 = dawn, 0.5 = day, 0.75 = dusk

uniform vec3 nightTint;
uniform vec3 dawnTint;
uniform vec3 dayTint;
uniform vec3 duskTint;

uniform Material material;

// Smooth interpolation function (smoother than linear)
float smoothBlend(float t) {
    return t * t * (3.0 - 2.0 * t); // Smoothstep
}

void main() {
    vec3 baseColor = material.tint;
    float alpha = material.opacity;

    // Wrap timeOfDay to [0, 1] range just in case
    float time = fract(timeOfDay);

    vec3 tintColor;

    // Determine which phase we're in and blend between them
    if (time < 0.25) {
        // NIGHT -> DAWN (0.0 to 0.25)
        float phase = time / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        tintColor = mix(nightTint, dawnTint, blend);
        
    } else if (time < 0.5) {
        // DAWN -> DAY (0.25 to 0.5)
        float phase = (time - 0.25) / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        tintColor = mix(dawnTint, dayTint, blend);
        
    } else if (time < 0.75) {
        // DAY -> DUSK (0.5 to 0.75)
        float phase = (time - 0.5) / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        tintColor = mix(dayTint, duskTint, blend);
        
    } else {
        // DUSK -> NIGHT (0.75 to 1.0)
        float phase = (time - 0.75) / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        tintColor = mix(duskTint, nightTint, blend);
    }

    baseColor *= tintColor;

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