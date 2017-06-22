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


package org.gearvrf.gvroutline;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;
//import org.gearvrf.GVRCustomMaterialShaderId;

public class OutlineShader extends GVRShader {
    public static final String COLOR_KEY = "u_color";
    public static final String THICKNESS_KEY = "u_thickness";
    private static final String VERTEX_SHADER =
            "layout(location = 0) in  vec4 a_position;\n"
                    + "layout(location = 1) in vec3 a_normal;\n"
                    + "layout (std140) uniform Transform_ubo{\n"
                    +  "mat4 u_view;\n"
                    +   "mat4 u_mvp;\n"
                    +   "mat4 u_mv;\n"
                    +  "mat4 u_mv_it;\n"
                    +   "mat4 u_model;\n"
                    +   "mat4 u_view_i;\n"
                    +   "vec4 u_right;};"
                    +  "layout (std140) uniform Material_ubo{ \n"
                    + "vec4 u_color;\n"
                    + "vec4 u_thickness; };"
                    + "void main() {\n"
                    + "  vec4 pos = vec4(a_position.xyz + a_normal * u_thickness.x, 1.0);\n"
                    + "  gl_Position = u_mvp * pos;\n"
                    + "}\n";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "out vec4 outColor;\n"
                    +  "layout (std140) uniform Material_ubo{ \n"
                    + "vec4 u_color;\n"
                    + "vec4 u_thickness; };\n"
                    + "void main() {\n"
                    + "  outColor = u_color;\n"
                    + "}\n";

    public OutlineShader(GVRContext gvrcontext) {
        super("float4 u_color; float u_thickness", "", "", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }


    protected void setMaterialDefaults(GVRShaderData material) {
        material.setVec4("u_color", 1, 1, 1, 1);
        material.setFloat("u_thickness", 1);
    }
}