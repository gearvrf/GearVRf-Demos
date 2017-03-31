
in vec3 a_position;
in vec2 a_texcoord;

out vec2 vTextureCoord;

void main()
{
  vTextureCoord = a_texcoord.xy;
  gl_Position = vec4(a_position, 1.0);
}
