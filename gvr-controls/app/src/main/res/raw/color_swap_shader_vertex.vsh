in vec3 a_position;
in vec2 a_texcoord;

@MATRIX_UNIFORMS

out vec2 coord;

void main() {

	vec4 pos = u_mvp * vec4(a_position,1.0);
	coord = a_texcoord;
    gl_Position =pos;
    
}
