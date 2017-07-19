precision mediump float;

in vec2 coord;
uniform sampler2D state1;
uniform sampler2D state2;
uniform float textureSwitch;
uniform float opacity;
out vec4 out_color;

void main() {
    if(textureSwitch == 0.0) {
        out_color = texture(state1, coord);
    } else {
        out_color = texture(state2, coord);
    }
}