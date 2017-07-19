precision mediump float;

uniform sampler2D u_texture;
in vec2 coord;
uniform float cutout;
out vec4 out_color;

void main() {
	vec4 color;
	color = texture(u_texture, coord);
	
	if(color.r < cutout){
		out_color = vec4(0,0,0,color.a);
	}else{
		out_color = vec4(0,0,0,0);
	}
	if(color.a < 1.0)
		out_color = vec4(0,0,0,0);
}