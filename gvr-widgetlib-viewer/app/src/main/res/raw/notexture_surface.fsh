uniform vec4 ambient_color;
uniform vec4 diffuse_color;
uniform vec4 specular_color;
uniform vec4 emissive_color;
uniform float specular_exponent;

struct Surface
{
   vec3 viewspaceNormal;
   vec4 ambient;
   vec4 diffuse;
   vec4 specular;
   vec4 emission;
};

Surface @ShaderName()
{
    vec4 diffuse = diffuse_color;
    diffuse.xyz *= diffuse.w;
    return Surface(viewspace_normal, ambient_color, diffuse, specular_color, vec4(0.0, 0.0, 0.0, 1.0));
}
