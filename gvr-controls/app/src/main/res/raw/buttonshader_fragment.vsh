#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
precision mediump float;
layout ( location = 0 ) in vec2  coord;

layout(set = 0, binding = 4) uniform sampler2D state1Text;
layout(set = 0, binding = 5) uniform sampler2D state2Text;
layout(set = 0, binding = 6) uniform sampler2D state3Text;
layout(set = 0, binding = 7) uniform sampler2D state1Background;
layout(set = 0, binding = 8) uniform sampler2D state2Background;
layout(set = 0, binding = 9) uniform sampler2D state3Background;

@MATERIAL_UNIFORMS
layout(location = 0) out vec4 outColor;
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