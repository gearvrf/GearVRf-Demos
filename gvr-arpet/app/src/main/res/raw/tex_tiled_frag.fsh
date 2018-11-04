precision highp float;
uniform sampler2D u_texture;

@MATERIAL_UNIFORMS

layout ( location = 0 ) in vec2 diffuse_coord;

out vec4 outColor;

void main()
{
    vec4 color = texture(u_texture, diffuse_coord);
    outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
