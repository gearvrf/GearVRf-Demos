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


package org.gearvrf.simplegallery;

import org.gearvrf.GVRContext;

import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class CustomPostEffectShaderManager extends GVRShader {

    private final String VERTEX_SHADER = "" //
            + "in vec3 a_position;\n"
            + "in vec2 a_texcoord;\n" //
            + "out vec2 v_tex_coord;\n"
            + "void main() {\n" //
            + "  v_tex_coord = a_texcoord.xy;\n"
            + "  gl_Position = vec4(a_position,1.0);\n" //
            + "}\n";

    private final String FRAGMENT_SHADER = "" //
            + "precision mediump float;\n"
            + "uniform sampler2D u_texture;\n"
            +  "layout (std140) uniform Material_ubo{ \n"
            + "vec3 u_ratio_r;\n"
            + "vec3 u_ratio_g;\n"
            + "vec3 u_ratio_b; };\n"
            + "in vec2 v_tex_coord;\n"
            + "out vec4 outColor;\n"
            + "void main() {\n"
            + "  vec4 tex = texture(u_texture, v_tex_coord);\n"
         //   + "  float r = tex.r * 0.393 + tex.g * 0.769 + tex.b * 0.189;\n"
       //     + "  float g = tex.r * 0.349 + tex.g * 0.686 + tex.b * 0.168;\n"
      //      + "  float b = tex.r * 0.272 + tex.g * 0.534 + tex.b * 0.131;\n"

              + "  float r = tex.r * u_ratio_r.r + tex.g * u_ratio_r.g + tex.b * u_ratio_r.b;\n"
            + "  float g = tex.r * u_ratio_g.r + tex.g * u_ratio_g.g + tex.b * u_ratio_g.b;\n"
            + "  float b = tex.r * u_ratio_b.r + tex.g * u_ratio_b.g + tex.b * u_ratio_b.b;\n"
            + "  vec3 color = vec3(r, g, b);\n"
            + "  outColor = vec4(color, tex.a);\n"
            + "}\n";

    public CustomPostEffectShaderManager(GVRContext gvrContext) {

        super("float3 u_ratio_r; float3 u_ratio_g, float3 u_ratio_b", "sampler2D u_texture", "float3 a_position , float2 a_texcoord ", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }
   protected void setMaterialDefaults(GVRShaderData material) {
        material.setVec3("u_ratio_r", 0.393f, 0.769f, 0.189f);
        material.setVec3("u_ratio_g", 0.349f, 0.686f, 0.168f);
        material.setVec3("u_ratio_b", 0.272f, 0.534f, 0.131f);
    }
}
