precision mediump float;

@MATRIX_UNIFORMS

in vec3 a_position;
in vec2 a_texcoord;
in vec4 a_color;
out vec4 v_color;
out vec2 v_texcoord;

void main() {
  gl_Position = u_mvp * vec4(a_position, 1.0);
  v_color = a_color;
  v_texcoord = a_texcoord;
}
