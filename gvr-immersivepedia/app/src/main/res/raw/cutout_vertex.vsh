#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

in vec4 a_position;
in vec3 a_normal;
in vec2 a_texcoord;

#ifdef HAS_MULTIVIEW
uniform mat4 u_mvp_[2];
#else
uniform mat4 u_mvp;
#endif

out vec2 coord;

void main() {
#ifdef HAS_MULTIVIEW
    vec4 pos = u_mvp_[gl_ViewID_OVR] * a_position;
#else
    vec4 pos = u_mvp * a_position;
#endif
    coord = a_texcoord;
    gl_Position = pos;
}
