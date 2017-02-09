precision mediump float;
in vec4 a_position;
in vec3 a_normal;
in vec2 a_texcoord;

layout (std140) uniform Transform_ubo{
     mat4 u_view;
     mat4 u_mvp;
     mat4 u_mv;
     mat4 u_mv_it;
     mat4 u_model;
     mat4 u_view_i;
     vec4 u_right;
};
out vec2 coord;

void main() {

	vec4 pos = u_mvp * a_position;
	coord = a_texcoord;
    gl_Position = pos;
    
}