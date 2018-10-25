#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
precision mediump float;

layout ( location = 0 ) in vec2  coord;
layout(set = 0, binding = 4) uniform sampler2D state1;
layout(set = 0, binding = 5) uniform sampler2D state2;

@MATERIAL_UNIFORMS

layout ( location = 0 ) out vec4 outColor;

void main() {
	vec4 textureColor;
	if(textureSwitch == 0.0) {
		textureColor = texture(state1, coord);
	} else {
		textureColor = texture(state2, coord);
	}

	outColor = textureColor;
	outColor.a = outColor.a * u_opacity;
}