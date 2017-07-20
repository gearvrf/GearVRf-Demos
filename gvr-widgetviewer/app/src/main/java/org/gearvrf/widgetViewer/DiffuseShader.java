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


package org.gearvrf.widgetViewer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class DiffuseShader extends GVRShader {

    public static final String COLOR_KEY = "u_color";
    public static final String LIGHT_KEY = "u_light";
    public static final String EYE_KEY = "u_eye";
    public static final String TEXTURE_KEY = "texture_t";

    public static final String MAT1_KEY = "u_mat1";
    public static final String MAT2_KEY = "u_mat2";
    public static final String MAT3_KEY = "u_mat3";
    public static final String MAT4_KEY = "u_mat4";

    private static final String VERTEX_SHADER = "" //
            + "precision mediump float;\n"
            + "in vec3 a_position;\n"
            + "in vec3 a_normal;\n" //
            + "in vec2 a_texcoord;\n"
            + "@MATRIX_UNIFORMS\n"
            + "@MATERIAL_UNIFORMS\n"
            + "out vec3 normal;\n"
            + "out vec3 view;\n" //
            + "out vec3 light;\n"
            + "out vec2 coord;\n" //

            + "void main() {\n"
            + "  normal = a_normal;\n" //
            + "  view  = vec3(u_eye.x, u_eye.y, u_eye.z) - a_position.xyz;\n"
            + "  light = vec3(u_light.x, u_light.y, u_light.z) - a_position.xyz;\n"
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp * vec4(a_position,1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision mediump float;\n"
            + "@MATERIAL_UNIFORMS\n"
            + "in vec2  coord;\n"
            + "in vec3  normal;\n" //
            + "in vec3  view;\n"
            + "in vec3  light;\n" //
            + "uniform sampler2D texture_t;\n"
            + "out vec4  outColor;\n" //
            + "void main() {\n" //
            + "  vec3  v = normalize(view);\n"
            + "  vec3  l = normalize(light);\n"
            + "  vec3  n = normalize(normal);\n"
            + "  vec3 color = texture(texture_t, coord).rgb;\n"
            + "  vec3  h = normalize(v+l);\n"
            + "  float diffuse  = max ( dot(l,n), 0.1 );\n"
            + "  float specular = max ( dot(h,n), 0.0 );\n"
            + "  specular = pow (specular, 300.0);\n" //
            + "  color *= diffuse;\n" //
            + "  color *= u_color.rgb;\n"
            + "  color += 0.5*(1.0- color)*specular;\n"
            + "  outColor = vec4( color, 1.0 );\n" //
            + "}\n";

    public DiffuseShader(GVRContext gvrContext) {
        super("float3 u_eye, float3 u_light, float4 u_color ", "sampler2D texture_t", "float3 a_position, float3 a_normal, float2 a_tex_coord", GLSLESVersion.V300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);

    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec4("u_color", 1, 1, 1, 1);
        material.setVec3("u_light", 1, 1, 1);
        material.setVec3("u_eye", 0, 0, 0);
    }
}
