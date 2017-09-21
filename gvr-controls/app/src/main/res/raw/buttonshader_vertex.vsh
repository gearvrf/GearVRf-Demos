in vec3 a_position;
in vec3 a_normal;
in vec2 a_texcoord;
@MATRIX_UNIFORMS
out vec2 coord;

void main() {

	gl_Position = u_mvp * vec4(a_position,1.0);
	coord = a_texcoord;
}
