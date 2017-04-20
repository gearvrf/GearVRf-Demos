
attribute vec3 a_position;
attribute vec2 a_texcoord;

varying vec2 vTextureCoord;

void main()
{
  vTextureCoord = a_texcoord.xy;
  gl_Position = vec4(a_position, 1.0);
}
