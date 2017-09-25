#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable


precision mediump float;
layout ( location = 0 ) in vec2  coord;
layout(set = 0, binding = 4) uniform sampler2D u_texture;
layout(location = 0) out vec4 outColor;
void main() {

	vec4 color;
    color = texture(u_texture, coord);
		
	outColor = color;
}