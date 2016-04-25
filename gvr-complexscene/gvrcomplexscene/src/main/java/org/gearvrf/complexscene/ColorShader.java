package org.gearvrf.complexscene;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;

public class ColorShader extends GVRShaderTemplate {

    private static final String VERTEX_SHADER = "in vec3 a_position;\n"
            + "uniform mat4 u_mvp;\n" //
            + "void main() {\n" //
            + "  gl_Position = u_mvp * vec4(a_position, 1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "uniform vec4 u_color;\n"
            + "out vec4 gl_FragColor;\n"
            + "void main() {\n" //
            + "  gl_FragColor = u_color;\n" //
            + "}\n";


    public ColorShader(GVRContext gvrcontext)
    {
        super("float4 u_color");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
