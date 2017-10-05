#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
precision mediump float;

layout(location = 0) in vec4 a_position;
layout(location = 2) in vec3 a_normal;
layout(location = 1) in vec2 a_texcoord;

layout(location = 0) out vec3 normal;
layout(location = 1) out vec3 view;
layout(location = 2) out vec3 light;
layout(location = 3) out vec2 coord;
layout(location = 4) out vec3  n;
layout(location = 5) out vec3  v;
layout(location = 6) out vec3  l;
layout(location = 7) out vec3  p;
@MATRIX_UNIFORMS
@MATERIAL_UNIFORMS
void main() {

	vec4 pos = u_mvp * a_position;
    normal = a_normal;
	view  = u_eye.xyz - pos.xyz;
	light = u_light.xyz;
	coord = a_texcoord;
	n = normalize(normal);
	v = normalize(view);
    l = normalize(light);
    p = pos.xyz;
    gl_Position = pos;

}