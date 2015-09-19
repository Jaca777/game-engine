#version 430
precision mediump float;

uniform samplerCube cube;
in vec3 vTexCoord;

out vec4 fragColor;

void main(void) {
    fragColor = texture(cube, vTexCoord) * vec4(1.1,1.1,1.5,1);
}