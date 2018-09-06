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


package org.gearvrf.widgetlibviewer.shaders;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class DiffuseShader2  extends GVRShader {

    public static final String COLOR_KEY = "u_color";
    public static final String LIGHT_KEY = "u_light";
    public static final String EYE_KEY = "u_eye";
    public static final String TEXTURE_KEY = "u_texture";

    public static final String MAT1_KEY = "u_mat1";
    public static final String MAT2_KEY = "u_mat2";
    public static final String MAT3_KEY = "u_mat3";
    public static final String MAT4_KEY = "u_mat4";

    private static final String VERTEX_SHADER = "" //
            + "#extension GL_ARB_separate_shader_objects : enable\n"
            + "#extension GL_ARB_shading_language_420pack : enable\n"
            + "precision mediump float;\n"

            + "layout(location = 0) in vec3 a_position;\n"
            + "layout(location = 2) in vec3 a_normal;\n" //
            + "layout(location = 1) in vec2 a_texcoord;\n"
            + "@MATRIX_UNIFORMS\n"
            + "@MATERIAL_UNIFORMS\n"
            + "layout(location = 0) out vec3 n;\n"
            + "layout(location = 1) out vec3 v;\n" //
            + "layout(location = 2) out vec3 l;\n"
            + "layout(location = 3) out vec2  coord;\n" //
            + "void main() {\n"
            + "  mat4 model;\n"
            + "  model[0] = u_mat1;\n" //
            + "  model[1] = u_mat2;\n"
            + "  model[2] = u_mat3;\n" //
            + "  model[3] = u_mat4;\n" //
            + "  model = inverse(model);\n"
            + "  vec4 pos = model* vec4(a_position,1.0);\n"
            + "  vec4 nrm = model*vec4(a_normal,1.0);\n"
            + "  n = normalize(nrm.xyz);\n"
            + "  v = normalize(vec3(u_eye.x, u_eye.y, u_eye.z)-pos.xyz);\n"
            + "  l = normalize(vec3(u_light.x, u_light.y, u_light.z)-pos.xyz);\n"
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp*vec4(a_position,1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "#extension GL_ARB_separate_shader_objects : enable\n"
            + "#extension GL_ARB_shading_language_420pack : enable\n"
            + "precision mediump float;\n"
            + "@MATERIAL_UNIFORMS\n"
            + "layout(location = 0) in vec3 n;\n"
            + "layout(location = 1) in vec3 v;\n" //
            + "layout(location = 2) in vec3 l;\n"
            + "layout(location = 3) in vec2  coord;\n" //
            + "layout(set = 0, binding = 4) uniform sampler2D u_texture;\n"
            + "layout(location = 0) out vec4 FragColor;\n"
            + "void main() {\n" //
            + "  vec4 color = texture(u_texture, coord);\n"
            + "  vec3  h = normalize(v+l);\n"
            + "  float diffuse  = max ( dot(l,n), 0.1 );\n"
            + "  float specular = max ( dot(h,n), 0.0 );\n"
            + "  specular = pow (specular, 300.0);\n"
            + "  color.rgb *= diffuse;\n" //
            + "  color.rgb *= u_color.rgb;\n"
            + "  color.rgb += 0.5*(1.0- color.rgb)*specular;\n"
            + "  FragColor = vec4( color );\n" //
            + "}\n";

    public DiffuseShader2(GVRContext gvrContext) {
        super("float4 u_mat1, float4 u_mat2, float4 u_mat3, float4 u_mat4, float3 u_eye, float3 u_light, float4 u_color", "sampler2D u_texture", "float3 a_position, float2 a_texcoord, float3 a_normal", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec4("u_color", 1, 1, 1, 1);
        material.setVec3("u_light", 1, 1, 1);
        material.setVec3("u_eye", 0, 0, 0);
        material.setVec4("u_mat1", 1, 1, 1, 1);
        material.setVec4("u_mat2", 1, 1, 1, 1);
        material.setVec4("u_mat3", 1, 1, 1, 1);
        material.setVec4("u_mat4", 1, 1, 1, 1);
    }
}
