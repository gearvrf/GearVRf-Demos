package org.gearvrf.ply;

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.utility.TextFile;
import org.gearvrf.x3ddemo.R;

public class VertexColorShader extends GVRShader
{
    private static String fragTemplate = null;
    private static String vtxTemplate = null;

    public VertexColorShader(GVRContext gvrcontext)
    {
        super("", "", "float3 a_position float2 a_texcoord float4 a_color", GLSLESVersion.VULKAN);
        Context context = gvrcontext.getContext();
        fragTemplate = TextFile.readTextFile(context, R.raw.fragmentshader);
        vtxTemplate = TextFile.readTextFile(context, R.raw.vertexshader);
        setSegment("FragmentTemplate", fragTemplate);
        setSegment("VertexTemplate", vtxTemplate);
    }
}
