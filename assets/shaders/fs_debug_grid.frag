#version 330 core

in vec2 vUv;
in mat4 vView;
in mat4 vProjection;

out vec4 fragColor;

// COLORS
const vec3 GRID_COLOR   = vec3(0.25, 0.25, 0.25);  // Darker grey
const vec3 AXIS_COLOR_X = vec3(1.0, 0.2, 0.2);
const vec3 AXIS_COLOR_Z = vec3(0.2, 0.5, 1.0);

// AXIS THICKNESS (in pixels)
const float AXIS_WIDTH_PIXELS = 1.0;  // Adjust this for thicker/thinner axes

// FADE DISTANCE
const float FADE_START = 40.0;
const float FADE_END   = 80.0;

void main() {
    // Ray from camera through this screen pixel
    mat4 invVP = inverse(vProjection * vView);
    vec4 nearPoint = invVP * vec4(vUv, 0.0, 1.0);
    vec4 farPoint  = invVP * vec4(vUv, 1.0, 1.0);
    nearPoint /= nearPoint.w;
    farPoint /= farPoint.w;
    
    // Ray direction
    vec3 rayDir = normalize(farPoint.xyz - nearPoint.xyz);
    
    // Intersect with ground plane (y = 0)
    float t = -nearPoint.y / rayDir.y;
    
    // If ray doesn't hit ground plane or hits behind camera, discard
    if (t < 0.0) {
        discard;
    }
    
    vec3 worldPos = nearPoint.xyz + rayDir * t;
    float x = worldPos.x;
    float z = worldPos.z;
    
    // Calculate proper depth for this world position
    vec4 clipPos = vProjection * vView * vec4(worldPos, 1.0);
    float depth = (clipPos.z / clipPos.w) * 0.5 + 0.5;
    gl_FragDepth = depth;

    // ANTI-ALIASED GRID using derivatives
    // Calculate how fast x and z change across the screen
    vec2 coord = vec2(x, z);
    vec2 derivative = fwidth(coord);  // How much coord changes per pixel
    
    // Grid lines at integer boundaries (this centers grid on axes)
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
    float line = min(grid.x, grid.y);
    
    // Smooth grid line with anti-aliasing
    float gridStrength = 1.0 - min(line, 1.0);
    
    vec3 color = GRID_COLOR * gridStrength;

    // Axes with CONSTANT SCREEN-SPACE WIDTH
    // derivative tells us world units per pixel, so multiply by desired pixel width
    float axisWidthX = AXIS_WIDTH_PIXELS * derivative.x;
    float axisWidthZ = AXIS_WIDTH_PIXELS * derivative.y;
    
    float axisX = 1.0 - smoothstep(axisWidthX - derivative.x, axisWidthX + derivative.x, abs(x));
    float axisZ = 1.0 - smoothstep(axisWidthZ - derivative.y, axisWidthZ + derivative.y, abs(z));
    
    // Axes override grid color
    if (axisX > 0.01) color = mix(color, AXIS_COLOR_X, axisX);
    if (axisZ > 0.01) color = mix(color, AXIS_COLOR_Z, axisZ);

    // Fade out with distance
    float dist = length(vec2(x, z));
    float fade = 1.0 - smoothstep(FADE_START, FADE_END, dist);
    
    // If completely transparent, discard the fragment
    if (fade < 0.01 && gridStrength < 0.01) discard;

    // Combine grid and fade
    float alpha = max(gridStrength, max(axisX, axisZ)) * fade;
    
    fragColor = vec4(color, alpha * 0.6);  // 0.6 for subtle transparency
}