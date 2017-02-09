/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.gearvrf.gvreyepicking;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

import android.content.Context;

public class ColorShader extends GVRShader
{
    private static final String VERTEX_SHADER = "in vec4 a_position;\n"
            + "layout (std140) uniform Transform_ubo{\n"
            +  "mat4 u_view;\n"
            +   "mat4 u_mvp;\n"
            +   "mat4 u_mv;\n"
            +  "mat4 u_mv_it;\n"
            +   "mat4 u_model;\n"
            +   "mat4 u_view_i;\n"
            + "vec4 u_right;};"

            + "void main() {\n"
            + "  gl_Position = u_mvp * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "layout (std140) uniform Material_ubo{ \n"
            + "vec4 u_color; };\n"
            + "out vec4 fragColor;\n"
            + "void main() {\n"
            + "  fragColor = u_color;\n"
            + "}\n";

    public ColorShader(GVRContext gvrContext)
    {
        super("float4 u_color", "", "float3 a_position", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec4("u_color", 0.5f, 0.5f, 0.5f, 1);
    }
}
