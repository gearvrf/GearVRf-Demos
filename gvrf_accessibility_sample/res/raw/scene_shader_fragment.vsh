precision mediump float;
varying vec2  coord;
uniform sampler2D texture;
uniform float blur;
void main() {
	
	float division = 13.0;
	
	vec4 color = texture2D(texture, coord) / division;
	vec3 color2 = texture2D(texture, coord ).rgb;
	
	
	color += texture2D(texture, (coord + vec2(0.001, 0.0))) / division;
	color += texture2D(texture, (coord + vec2(0.001, 0.001))) / division;
	color += texture2D(texture, (coord + vec2(0.0, 0.001))) / division;
	
	color += texture2D(texture, (coord + vec2(-0.001, 0.0))) / division;
	color += texture2D(texture, (coord + vec2(-0.001, -0.001))) / division;
	color += texture2D(texture, (coord + vec2(0.0, -0.001))) / division;
	
	
	color += texture2D(texture, (coord + vec2(0.002, 0.0))) / division;
	color += texture2D(texture, (coord + vec2(0.002, 0.002))) / division;
	color += texture2D(texture, (coord + vec2(0.0, 0.002))) / division;
	
	color += texture2D(texture, (coord + vec2(-0.002, 0.0))) / division;
	color += texture2D(texture, (coord + vec2(-0.002, -0.002))) / division;
	color += texture2D(texture, (coord + vec2(0.0, -0.002))) / division;
	
	vec3 finalColor = (color.rgb * blur) + (color2 * (1.0-blur));
	
	gl_FragColor = vec4( finalColor * (1.0 - blur/2.0), 1.0 );
	gl_FragColor.a = 1.0 - min(0.9,blur);
}