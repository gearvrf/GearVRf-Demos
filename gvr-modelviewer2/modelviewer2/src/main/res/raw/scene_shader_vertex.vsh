attribute vec4 a_position;
uniform mat4 u_mvp;

void main() {

	vec4 pos = u_mvp * a_position;
    gl_Position = pos;
    
}
