package org.gearvrf.blurfilter;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.utility.TextFile;

public class HorzBlurShader  extends GVRShader
{
    private static String fragTemplate;
    private static String vtxTemplate;

    public  HorzBlurShader(GVRContext context)
    {
        super("float u_resolution", "samplerExternalOES u_texture", "float3 a_position float2 a_texcoord", GLSLESVersion.VULKAN);
        fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.gaussianblurhorz);
        vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.pos_tex);

        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentTemplate", fragTemplate);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setFloat("u_resolution", 1);
    }
}