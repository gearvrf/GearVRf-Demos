precision mediump float;
in vec4 a_position;
in vec3 a_normal;
in vec2 a_texcoord;

@MATRIX_UNIFORMS
out vec2 coord;

void main() {

	vec4 pos = u_mvp * a_position;
	coord = a_texcoord;
    gl_Position = pos;
    
}