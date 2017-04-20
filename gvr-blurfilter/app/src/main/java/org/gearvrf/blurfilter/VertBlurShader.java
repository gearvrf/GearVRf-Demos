package org.gearvrf.blurfilter;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.utility.TextFile;

public class VertBlurShader extends GVRShaderTemplate
{
    private static String fragTemplate;
    private static String vtxTemplate;

    public VertBlurShader(GVRContext context)
    {
        super("float u_resolution");
        fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.gaussianblurvert);
        vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.pos_tex);

        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentTemplate", fragTemplate);
    }
}