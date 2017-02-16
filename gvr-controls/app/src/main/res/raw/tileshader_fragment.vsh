precision mediump float;
in vec2  coord;
uniform sampler2D texture;
layout (std140) uniform Material_ubo
{
 float tile
};
;
out vec4 outColor;
void main() {

	vec4 color;
    color = texture(texture, coord);
		
	outColor = color;
}