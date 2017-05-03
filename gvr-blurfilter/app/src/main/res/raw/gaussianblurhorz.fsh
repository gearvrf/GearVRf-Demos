#extension GL_OES_EGL_image_external : require
precision highp float;
uniform float u_resolution;
uniform samplerExternalOES main_texture;

varying vec2 vTextureCoord;
vec4 horzBlur(vec2 texCoord, vec2 resolution)
{
    float offset[3], weight[3];
    offset[0] = 0.0;
    offset[1] = 1.3846153846;
    offset[2] = 3.2307692308;
    weight[0] = 0.2270270270;
    weight[1] = 0.3162162162;
    weight[2] = 0.0702702703;
    vec3 tc = texture2D(main_texture, texCoord).rgb * weight[0];
    for (int i = 1; i < 3; i++)
    {
      tc += texture2D(main_texture, texCoord + vec2(offset[i] / resolution.x, 0.0)).rgb * weight[i];
      tc += texture2D(main_texture, texCoord - vec2(offset[i] / resolution.x, 0.0)).rgb * weight[i];
    }
    return vec4(tc, 1.0);
}

void main()
{
    gl_FragColor = horzBlur(vTextureCoord, vec2(u_resolution, u_resolution));
}