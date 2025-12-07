#version 330 core

layout (location = 0) in vec3 position;
layout (location = 2) in vec2 texCoord;

out vec2 aTexCoord;

uniform mat4 model;
uniform mat4 mvpMatrix;

uniform vec2 uvScale = vec2(1.0, 1.0);
uniform vec2 uvOffset = vec2(0.0, 0.0);
uniform vec2 tiling;

void main() {
    gl_Position = mvpMatrix * vec4(position, 1.0);

    // Apply UV scale/offset exactly like standard shader
    aTexCoord = (texCoord * tiling) * uvScale + uvOffset;
}