precision mediump float;
in vec2  coord;

uniform sampler2D state1Background;
uniform sampler2D state1Text;
uniform sampler2D state2Background;
uniform sampler2D state2Text;
uniform sampler2D state3Background;
uniform sampler2D state3Text;


layout (std140) uniform Material_ubo
{
 float textureSwitch;
 float u_opacity;
};
out vec4 outColor;
void main() {
	vec4 background;
	vec4 text;
	if(textureSwitch == 0.0) {
		background = texture(state1Background, coord);
		text = texture(state1Text, coord);
	} else if (textureSwitch == 1.0) {
		background = texture(state2Background, coord);
		text = texture(state2Text, coord);
	} else if (textureSwitch == 2.0) {
		background = texture(state3Background, coord);
		text = texture(state3Text, coord);
	} else {
		background = vec4(0.0, 1.0, 0.0, 1.0);
		text = vec4(0.0, 0.0, 1.0, 1.0);
	}
		
	outColor = background + text;
	outColor.a = outColor.a * u_opacity;
}