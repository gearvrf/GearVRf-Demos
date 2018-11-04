#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

uniform vec2 u_scale;

layout ( location = 0 )in vec3 a_position;

@MATRIX_UNIFORMS

layout ( location = 0 ) out vec2 diffuse_coord;

void main()
{
    vec4 pos = u_model * vec4(a_position, 1);

    diffuse_coord = pos.sp * u_scale;

    gl_Position = u_mvp * vec4(a_position, 1);
}