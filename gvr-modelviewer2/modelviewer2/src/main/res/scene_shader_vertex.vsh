attribute vec4 a_position;
attribute vec2 a_texcoord;
uniform mat4 u_mvp;
varying vec2 coord;

void main() {

	vec4 pos = u_mvp * a_position;
	coord = a_texcoord;
    gl_Position = pos;
    
}
