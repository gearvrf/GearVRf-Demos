#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#extension GL_OES_EGL_image_external : enable
#extension GL_OES_EGL_image_external_essl3 : enable
precision highp float;
@MATERIAL_UNIFORMS
uniform samplerExternalOES u_texture;

layout (location = 0) in vec2 vTextureCoord;
layout (location = 0) out vec4 outColor;

vec4 horzBlur(vec2 texCoord, vec2 resolution)
{
    float offset[3], weight[3];
    offset[0] = 0.0;
    offset[1] = 1.3846153846;
    offset[2] = 3.2307692308;
    weight[0] = 0.2270270270;
    weight[1] = 0.3162162162;
    weight[2] = 0.0702702703;
    vec3 tc = texture(u_texture, texCoord).rgb * weight[0];
    for (int i = 1; i < 3; i++)
    {
      tc += texture(u_texture, texCoord + vec2(offset[i] / resolution.x, 0.0)).rgb * weight[i];
      tc += texture(u_texture, texCoord - vec2(offset[i] / resolution.x, 0.0)).rgb * weight[i];
    }
    return vec4(tc, 1.0);
}

void main()
{
    outColor = horzBlur(vTextureCoord, vec2(u_resolution, u_resolution));
}