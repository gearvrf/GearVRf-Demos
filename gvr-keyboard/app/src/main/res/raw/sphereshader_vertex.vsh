
precision mediump float;

in vec4 a_position;
in vec3 a_normal;
in vec2 a_texcoord;

out vec3 normal;
out vec3 view;
out vec3 light;
out vec2 coord;
out vec3  n;
out vec3  v;
out vec3  l;
out vec3  p;

layout (std140) uniform Material_ubo{
    vec3 u_eye;
    vec3 u_light;
    vec3 trans_color;
    float animTexture;
    float blur;
    float u_radius;
};

layout (std140) uniform Transform_ubo{
 #ifdef HAS_MULTIVIEW
     mat4 u_view_[2];
     mat4 u_mvp_[2];
     mat4 u_mv_[2];
     mat4 u_mv_it_[2];
 #else
     mat4 u_view;
     mat4 u_mvp;
     mat4 u_mv;
     mat4 u_mv_it;
 #endif
     mat4 u_model;
     mat4 u_view_i;
     vec4 u_right;
};
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