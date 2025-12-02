#version 330 core

in vec3 TexCoords;
out vec4 FragColor;

uniform vec3 skyColorTop;
uniform vec3 skyColorHorizon;
uniform vec3 skyColorBottom;

void main() {
    // Normalize the direction vector and use Y component for vertical gradient
    float t = normalize(TexCoords).y;
    
    vec3 color;
    if (t > 0.0) {
        // Upper hemisphere: interpolate from horizon to top
        color = mix(skyColorHorizon, skyColorTop, t);
    } else {
        // Lower hemisphere: interpolate from horizon to bottom
        color = mix(skyColorHorizon, skyColorBottom, -t);
    }
    
    FragColor = vec4(color, 1.0);
}