package org.gearvrf.multilight;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;

public class DirectLight extends GVRLight
{
	private static final String LIGHT_SHADER_CODE = 
        "Radiance @LightType(Surface s, in Data@LightType data) {\n" +
        "   vec3 L = normalize(data.world_direction.xyz);\n" +
        "   float nDotL = max(dot(s.normal.xyz, L), 0.0);\n" +
        "   Radiance r = Radiance(nDotL * data.color, L);\n" +
        "   return r; } \n";
        
	public DirectLight(GVRContext gvrContext) {
		super(gvrContext);
		setShaderSource(LIGHT_SHADER_CODE);
		uniformDescriptor = "float3 world_position, float3 world_direction, float3 color";
	}	
}
