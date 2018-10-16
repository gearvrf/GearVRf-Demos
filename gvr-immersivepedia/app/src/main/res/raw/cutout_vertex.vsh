#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
layout(location = 0) in vec4 a_position;
layout(location = 1) in vec2 a_texcoord;

@MATRIX_UNIFORMS
layout(location = 0) out vec2 coord;

void main() {
#ifdef HAS_MULTIVIEW
    vec4 pos = u_mvp_[gl_ViewID_OVR] * a_position;
#else
    vec4 pos = u_mvp * a_position;
#endif
    coord = a_texcoord;

    gl_Position = pos;
}
