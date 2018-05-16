#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
precision mediump float;

layout(location = 0) in vec2  coord;
layout(set = 0, binding = 5) uniform sampler2D u_texture;
@MATERIAL_UNIFORMS

layout (location = 0) out vec4 outColor;

void main() {
	
	float division = 13.0;
	
	vec4 color = texture(u_texture, coord) / division;
	vec3 color2 = texture(u_texture, coord ).rgb;

	color += texture(u_texture, (coord + vec2(0.001, 0.0))) / division;
	color += texture(u_texture, (coord + vec2(0.001, 0.001))) / division;
	color += texture(u_texture, (coord + vec2(0.0, 0.001))) / division;
	
	color += texture(u_texture, (coord + vec2(-0.001, 0.0))) / division;
	color += texture(u_texture, (coord + vec2(-0.001, -0.001))) / division;
	color += texture(u_texture, (coord + vec2(0.0, -0.001))) / division;
	
	vec3 finalColor = (color.rgb * blur) + (color2 * (1.0-blur));
	
	outColor = vec4( finalColor * (1.0 - blur/2.0), 1.0 );
	outColor.a = 1.0 - min(0.9,blur);
}