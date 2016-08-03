package com.gearvrf.fasteater;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

import android.content.Context;

import org.gearvrf.GVRCustomMaterialShaderId;


/**
 * Copied from gvr-eyepicking demo app
 */
public class ColorShader extends GVRShaderTemplate {

    private static final String VERTEX_SHADER = "in vec4 a_position;\n"
            + "uniform mat4 u_mvp;\n"
            + "void main() {\n"
            + "  gl_Position = u_mvp * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform vec4 u_color;\n"
            + "out vec4 fragColor;\n"
            + "void main() {\n"
            + "  fragColor = u_color;\n"
            + "}\n";

    public ColorShader(GVRContext gvrContext)
    {
        super("float4 u_color");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }


}
