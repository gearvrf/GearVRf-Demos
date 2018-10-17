precision mediump float;

in vec4 v_color;
in vec2 v_texcoord;
uniform sampler2D u_texture;
out vec4 fragColor;

void main() {
  fragColor = v_color;
}
