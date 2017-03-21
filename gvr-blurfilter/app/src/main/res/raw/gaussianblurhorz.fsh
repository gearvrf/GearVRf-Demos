#extension GL_OES_EGL_image_external_essl3 : require
//#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform float u_resolution;
uniform samplerExternalOES main_texture;

in vec2 vTextureCoord;
out vec4 FragmentColor;

float offset[3] = float[]( 0.0, 1.3846153846, 3.2307692308 );
float weight[3] = float[]( 0.2270270270, 0.3162162162, 0.0702702703 );


vec4 horzBlur(vec2 texCoord, vec2 resolution)
{
    vec3 tc = texture(main_texture, texCoord).rgb * weight[0];
    for (int i = 1; i < 3; i++)
    {
      tc += texture(main_texture, texCoord + vec2(offset[i]) / resolution.x, 0.0).rgb * weight[i];
      tc += texture(main_texture, texCoord - vec2(offset[i]) / resolution.x, 0.0).rgb * weight[i];
    }
    return vec4(tc, 1.0);
}

void main()
{
    FragmentColor = horzBlur(vTextureCoord, vec2(u_resolution, u_resolution));
}