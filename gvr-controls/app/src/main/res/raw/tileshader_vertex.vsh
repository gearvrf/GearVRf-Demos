in vec3 a_position;
in vec3 a_normal;
in vec2 a_texcoord;
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
     float u_right;
};
out vec2 coord;

void main() {

	vec4 pos = u_mvp * vec4(a_position,1.0);
	coord = a_texcoord;
    gl_Position = pos;
    
}
