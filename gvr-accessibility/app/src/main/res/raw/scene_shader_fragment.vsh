precision mediump float;
in vec2 diffuse_coord;
uniform sampler2D u_texture;
out vec4 outColor;
layout (std140) uniform Material_ubo
{
  float blur;
};


void main() {
	
	float division = 13.0;
	
	vec4 color = texture(u_texture, diffuse_coord) / division;
	vec3 color2 = texture(u_texture, diffuse_coord ).rgb;
	
	
	color += texture(u_texture, (diffuse_coord + vec2(0.001, 0.0))) / division;
	color += texture(u_texture, (diffuse_coord + vec2(0.001, 0.001))) / division;
	color += texture(u_texture, (diffuse_coord + vec2(0.0, 0.001))) / division;
	
	color += texture(u_texture, (diffuse_coord + vec2(-0.001, 0.0))) / division;
	color += texture(u_texture, (diffuse_coord + vec2(-0.001, -0.001))) / division;
	color += texture(u_texture, (diffuse_coord + vec2(0.0, -0.001))) / division;
	
	vec3 finalColor = (color.rgb * blur) + (color2 * (1.0-blur));
	
	outColor = vec4( finalColor * (1.0 - blur/2.0), 1.0 );
	outColor.a = 1.0 - min(0.9,blur);
}