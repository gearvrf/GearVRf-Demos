#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision mediump float;
layout ( location = 0 ) in vec2  coord;
layout(set = 0, binding = 4) uniform sampler2D grayScaleTexture;
layout(set = 0, binding = 5) uniform sampler2D detailsTexture;

@MATERIAL_UNIFORMS

layout(location = 0) out vec4 outColor;
void main() {
	
	vec4 colorGrayScale;
	vec4 colorDetails;
	
    colorGrayScale = texture(grayScaleTexture, coord);
    colorDetails = texture(detailsTexture, coord);
    
    vec4 colorResult = colorGrayScale * u_color;

	if(colorDetails.a != 0.0){
	
		colorResult = colorResult * (1.0-colorDetails.a) + colorDetails * (colorDetails.a);
        
	}
    		
	outColor = colorResult * u_opacity;
}