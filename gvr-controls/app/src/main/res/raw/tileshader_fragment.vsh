precision mediump float;
in vec2  coord;
uniform sampler2D u_texture;
out vec4 outColor;
void main() {

	vec4 color;
    color = texture(u_texture, coord);
		
	outColor = color;
}