#version 330 core

in vec3 TexCoords;
out vec4 FragColor;

uniform float timeOfDay; // 0.0 to 1.0

// Night (midnight)
uniform vec3 nightColorTop;
uniform vec3 nightColorHorizon;
uniform vec3 nightColorBottom;

// Dawn
uniform vec3 dawnColorTop;
uniform vec3 dawnColorHorizon;
uniform vec3 dawnColorBottom;

// Day (midday)
uniform vec3 dayColorTop;
uniform vec3 dayColorHorizon;
uniform vec3 dayColorBottom;

// Dusk
uniform vec3 duskColorTop;
uniform vec3 duskColorHorizon;
uniform vec3 duskColorBottom;

// Stars
uniform float starDensity;
uniform float starBrightness;
uniform float starSize;

// Improved hash function for better star distribution
float hash13(vec3 p3) {
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3, p3.zyx + 31.32);
    return fract((p3.x + p3.y) * p3.z);
}

// Smooth interpolation function (smoother than linear)
float smoothBlend(float t) {
    return t * t * (3.0 - 2.0 * t); // Smoothstep
}

float skyFalloff(float y) {
    float densityTuning = 4.0;
    return 1.0 - exp(-densityTuning * max(y, 0.0)); // 4.0 = density tuning
}

float skyCurve(float y) {
    y = clamp(y, -1.0, 1.0);
    float curveStrength = 1.5;
    return 1.0 - pow(1.0 - max(y, 0.0), curveStrength);  // 1.5 controls curve strength
}

vec3 getSkyGradient(vec3 colorTop, vec3 colorHorizon, vec3 colorBottom, float verticalPos) {
    float k;

    if (verticalPos > 0.0) {
        // Upper hemisphere - strong softening near horizon
        k = skyCurve(verticalPos);
        return mix(colorHorizon, colorTop, k);
    }
    else {
        // Lower hemisphere - subtle atmospheric falloff
        k = 1.0 - pow(1.0 - (-verticalPos), 2.0);
        return mix(colorHorizon, colorBottom, k);
    }
}

// Calculate star visibility based on time of day
float getStarVisibility(float time) {
    // Stars fade in from 0.85 (late dusk) to 0.95 (full night)
    // Stay visible until 0.20 (early dawn), then fade out by 0.30
    
    if (time >= 0.75 && time < 0.95) {
        // Fade in: 0.75 -> 0.95 (from 0 to 1)
        return smoothBlend((time - 0.75) / 0.20);
    } 
    else if (time >= 0.95 || time < 0.20) {
        // Fully visible: 0.95 -> 1.0 and 0.0 -> 0.20
        return 1.0;
    }
    else if (time >= 0.20 && time < 0.30) {
        // Fade out: 0.20 -> 0.30 (from 1 to 0)
        return 1.0 - smoothBlend((time - 0.20) / 0.10);
    }
    
    // No stars during day
    return 0.0;
}

void main() {
    vec3 dir = normalize(TexCoords);
    float t = dir.y; // Vertical position (-1 to 1)
    
    // Wrap timeOfDay to [0, 1] range just in case
    float time = fract(timeOfDay);

    vec3 skyColor;

    // Determine which phase we're in and blend between them
    if (time < 0.25) {
        // NIGHT -> DAWN (0.0 to 0.25)
        float phase = time / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        
        vec3 nightSky = getSkyGradient(nightColorTop, nightColorHorizon, nightColorBottom, t);
        vec3 dawnSky = getSkyGradient(dawnColorTop, dawnColorHorizon, dawnColorBottom, t);
        
        skyColor = mix(nightSky, dawnSky, blend);
        
    } else if (time < 0.5) {
        // DAWN -> DAY (0.25 to 0.5)
        float phase = (time - 0.25) / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        
        vec3 dawnSky = getSkyGradient(dawnColorTop, dawnColorHorizon, dawnColorBottom, t);
        vec3 daySky = getSkyGradient(dayColorTop, dayColorHorizon, dayColorBottom, t);
        
        skyColor = mix(dawnSky, daySky, blend);
        
    } else if (time < 0.75) {
        // DAY -> DUSK (0.5 to 0.75)
        float phase = (time - 0.5) / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        
        vec3 daySky = getSkyGradient(dayColorTop, dayColorHorizon, dayColorBottom, t);
        vec3 duskSky = getSkyGradient(duskColorTop, duskColorHorizon, duskColorBottom, t);
        
        skyColor = mix(daySky, duskSky, blend);
        
    } else {
        // DUSK -> NIGHT (0.75 to 1.0)
        float phase = (time - 0.75) / 0.25; // 0.0 to 1.0 within this phase
        float blend = smoothBlend(phase);
        
        vec3 duskSky = getSkyGradient(duskColorTop, duskColorHorizon, duskColorBottom, t);
        vec3 nightSky = getSkyGradient(nightColorTop, nightColorHorizon, nightColorBottom, t);
        
        skyColor = mix(duskSky, nightSky, blend);
    }
    
    // Calculate star visibility
    float starVisibility = getStarVisibility(time);
    
    // Only draw stars in upper hemisphere
    if (starVisibility > 0.0 && t > 0.0) {

        // 1. Convert direction to stable spherical coordinates (no flicker)
        float azimuth  = atan(dir.z, dir.x);       // -PI .. PI
        float altitude = acos(dir.y);              // 0 .. PI

        // Normalize to 0..1 UV
        vec2 uv = vec2(
            azimuth / (2.0 * 3.14159265) + 0.5,
            altitude / 3.14159265
        );

        // 2. Increase grid resolution (star density control)
        float densityScale = 300.0;
        vec2 scaledUV = uv * densityScale;

        vec2 cell = floor(scaledUV);
        vec2 localPos = fract(scaledUV);

        // Random per-cell value
        float rnd = hash13(vec3(cell, 0.0));

        // Only generate stars in a fraction of cells
        if (rnd > (1.0 - starDensity)) {

            // 3. Star center position (random inside cell)
            vec2 starCenter = vec2(
                hash13(vec3(cell, 12.34)),
                hash13(vec3(cell, 56.78))
            );

            // Distance from fragment to star center
            float dist = distance(localPos, starCenter);

            // Base brightness (0..1, squared for more dim stars)
            float brightness = hash13(vec3(cell, 98.76));
            brightness = pow(brightness, 3.0);

            // Random size multiplier between 0.5 and 1.0
            float sizeRand = mix(0.5, 1.0, hash13(vec3(cell, 22.22)));

            // Bigger stars should be brighter, scale size by brightness
            // brightness^0.5 makes the increase smoother and more natural
            float sizeBoost = mix(0.7, 1.3, sqrt(brightness));

            // Final per-star size
            float finalSize = starSize * sizeRand * sizeBoost;

            // Final star intensity from falloff
            float starIntensity = 1.0 - smoothstep(0.0, finalSize, dist);

            if (starIntensity > 0.0) {
                // 5. Brightness variation
                float brightness = hash13(vec3(cell, 98.76));
                brightness = pow(brightness, 3.0);  // Many dim stars, few bright ones

                // 6. Subtle star color variation
                float colorVar = hash13(vec3(cell, 45.67));
                vec3 starColor =
                    (colorVar > 0.70) ? vec3(1.0, 0.95, 0.8) :   // warm white
                    (colorVar > 0.30) ? vec3(1.0) :             // pure white
                                        vec3(0.8, 0.9, 1.0);     // cool blue

                // 7. Add to final sky color (with visibility + user brightness)
                skyColor += starColor * starIntensity * brightness *
                            starBrightness * starVisibility;
            }
        }
    }
    
    FragColor = vec4(skyColor, 1.0);
}