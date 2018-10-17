precision highp float;
uniform sampler2D u_texture;

@MATERIAL_UNIFORMS

layout ( location = 0 ) in vec2 diffuse_coord;
layout ( location = 1 ) in vec2 mask_coord;

out vec4 outColor;

void main()
{
    vec4 color = texture(u_texture, diffuse_coord);
    //vec4 mask_color = texture(u_texture, mask_coord);
    //color.a = min(color.a, mask_color.a);
    outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
