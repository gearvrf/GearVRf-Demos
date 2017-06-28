#extension GL_OES_EGL_image_external : enable
#extension GL_OES_EGL_image_external_essl3 : enable
precision highp float;
uniform float u_resolution;
uniform samplerExternalOES u_texture;


varying vec2 vTextureCoord;

vec4 vertBlur(vec2 texCoord, vec2 resolution)
{
    float offset[3], weight[3];
    offset[0] = 0.0;
    offset[1] = 1.3846153846;
    offset[2] = 3.2307692308;
    weight[0] = 0.2270270270;
    weight[1] = 0.3162162162;
    weight[2] = 0.0702702703;
    vec3 tc = texture2D(u_texture, texCoord).rgb * weight[0];
    for (int i = 1; i < 3; i++)
    {
        tc += texture2D(u_texture, texCoord + vec2(0.0, offset[i]) / resolution.y).rgb * weight[i];
        tc += texture2D(u_texture, texCoord - vec2(0.0, offset[i]) / resolution.y).rgb * weight[i];
    }
    return vec4(tc, 1.0);
}

void main()
{
    gl_FragColor = vertBlur(vTextureCoord, vec2(u_resolution, u_resolution));
}