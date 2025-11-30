#version 330 core

in vec2 vUv;
in mat4 vView;
in mat4 vProjection;

out vec4 fragColor;

// COLORS
const vec3 GRID_COLOR   = vec3(0.25, 0.25, 0.25);  // Darker grey
const vec3 AXIS_COLOR_X = vec3(1.0, 0.2, 0.2);     // Red for X-axis
const vec3 AXIS_COLOR_Z = vec3(0.2, 0.5, 1.0);     // Blue for Z-axis

// AXIS THICKNESS (in pixels)
const float AXIS_WIDTH_PIXELS = 1.0;

// DASH PARAMETERS (in world units)
const float DASH_LENGTH = 1.0;      // Length of each dash
const float DASH_GAP = 0.5;         // Gap between dashes
const float DASH_TOTAL = DASH_LENGTH + DASH_GAP;

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
    vec2 coord = vec2(x, z);
    vec2 derivative = fwidth(coord);
    
    // Grid lines at integer boundaries
    vec2 grid = abs(fract(coord - 0.5) - 0.5) / derivative;
    float line = min(grid.x, grid.y);
    
    // Smooth grid line with anti-aliasing
    float gridStrength = 1.0 - min(line, 1.0);
    
    vec3 color = GRID_COLOR * gridStrength;

    // AXES with CONSTANT SCREEN-SPACE WIDTH
    // FIXED: Naming now matches what the axes actually represent
    float axisWidthZ = AXIS_WIDTH_PIXELS * derivative.x;  // Z-axis width (runs along Z when x=0)
    float axisWidthX = AXIS_WIDTH_PIXELS * derivative.y;  // X-axis width (runs along X when z=0)
    
    // Z-axis appears when x coordinate = 0 (line parallel to Z)
    float zAxisLine = 1.0 - smoothstep(axisWidthZ - derivative.x, axisWidthZ + derivative.x, abs(x));
    // X-axis appears when z coordinate = 0 (line parallel to X)
    float xAxisLine = 1.0 - smoothstep(axisWidthX - derivative.y, axisWidthX + derivative.y, abs(z));
    
    // DASHED PATTERN for negative axes
    float dashPatternZ = 1.0;
    float dashPatternX = 1.0;
    
    // Z-axis: dashed when z < 0 (negative Z side)
    if (z < 0.0 && zAxisLine > 0.01) {
        float dashPos = mod(abs(z), DASH_TOTAL);
        dashPatternZ = smoothstep(DASH_LENGTH - derivative.x, DASH_LENGTH + derivative.x, dashPos);
        dashPatternZ = 1.0 - dashPatternZ;  // Invert so dashes are visible
    }
    
    // X-axis: dashed when x < 0 (negative X side)
    if (x < 0.0 && xAxisLine > 0.01) {
        float dashPos = mod(abs(x), DASH_TOTAL);
        dashPatternX = smoothstep(DASH_LENGTH - derivative.y, DASH_LENGTH + derivative.y, dashPos);
        dashPatternX = 1.0 - dashPatternX;  // Invert so dashes are visible
    }
    
    // Apply dash pattern to axis strength
    zAxisLine *= dashPatternZ;
    xAxisLine *= dashPatternX;
    
    // FIXED: Now colors match the correct axes!
    if (zAxisLine > 0.01) color = mix(color, AXIS_COLOR_Z, zAxisLine);  // Blue Z-axis
    if (xAxisLine > 0.01) color = mix(color, AXIS_COLOR_X, xAxisLine);  // Red X-axis

    // Fade out with distance
    float dist = length(vec2(x, z));
    float fade = 1.0 - smoothstep(FADE_START, FADE_END, dist);
    
    // If completely transparent, discard the fragment
    if (fade < 0.01 && gridStrength < 0.01) discard;

    // Combine grid and fade
    float alpha = max(gridStrength, max(zAxisLine, xAxisLine)) * fade;
    
    fragColor = vec4(color, alpha * 0.6);
}