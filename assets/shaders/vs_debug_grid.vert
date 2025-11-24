#version 330 core

layout(location = 0) in vec3 aPos;

out vec2 vUv;
out mat4 vView;
out mat4 vProjection;

uniform mat4 view;
uniform mat4 projection;

void main() {
    // Pass UV coordinates
    vUv = aPos.xy;
    
    // Pass matrices to fragment shader (this prevents them from being optimized out)
    vView = view;
    vProjection = projection;
    
    // Fullscreen quad in clip space
    gl_Position = vec4(aPos.xy, 0.0, 1.0);
}