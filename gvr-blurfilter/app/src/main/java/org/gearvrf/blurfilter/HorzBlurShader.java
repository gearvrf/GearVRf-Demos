package org.gearvrf.blurfilter;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.utility.TextFile;

public class HorzBlurShader  extends GVRShaderTemplate
{
    private static String fragTemplate;
    private static String vtxTemplate;

    public  HorzBlurShader(GVRContext context)
    {
        super("float u_resolution", 300);
        fragTemplate = TextFile.readTextFile(context.getContext(), R.raw.gaussianblurhorz);
        vtxTemplate = TextFile.readTextFile(context.getContext(), R.raw.pos_tex);

        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentTemplate", fragTemplate);
    }
}