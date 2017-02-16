in vec3 a_position;
in vec3 a_normal;
in vec2 a_texcoord;
uniform mat4 u_mvp;
out vec2 coord;

void main() {

	vec4 pos = u_mvp * a_position;
	coord = a_texcoord;
    gl_Position = vec4(pos,1.0);
    
}
