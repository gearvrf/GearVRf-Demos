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



package org.gearvrf.sample.remote_scripting;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderId;

public class GammaShader extends GVRShader {

    private  GVRShaderId mShaderId;

    private static final String VERTEX_SHADER =
            "#extension GL_ARB_separate_shader_objects : enable \n" +
            "#extension GL_ARB_shading_language_420pack : enable\n" +
            "layout ( location = 0 ) in vec3 a_position;\n" +
            "layout ( location = 1 ) in vec2 a_texcoord;\n" +
            "layout ( location = 0 ) out vec2 v_tex_coord;\n" +
            "@MATRIX_UNIFORMS \n" +
            "@MATERIAL_UNIFORMS \n" +
            "void main() {\n" +
            "  v_tex_coord = a_texcoord.xy;\n" +
            "  gl_Position = vec4(a_position,1.0);\n" +
            "}\n";
    private static final String FRAGMENT_SHADER =
            "#extension GL_ARB_separate_shader_objects : enable \n" +
            "#extension GL_ARB_shading_language_420pack : enable \n" +
            "precision mediump float;\n" +
            "layout(binding = 4) uniform sampler2D u_texture;\n" +
            "@MATERIAL_UNIFORMS\n" +
            "layout ( location = 0 ) in vec2 v_tex_coord;\n" +
            "layout ( location = 0 ) out vec4 outColor;\n" +
            "void main() {\n" +
            "  vec4 tex = texture(u_texture, v_tex_coord);\n" +
            "  vec3 color = pow(tex.rgb, vec3(1.0/u_gamma));\n" +
            "  outColor = vec4(color, tex.a);\n" +
            "}\n";

    public GammaShader(GVRContext gvrContext) {
        super("float u_gamma", "sampler2D u_texture", "float3 a_position float2 a_texcoord", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    public void setMaterialDefaults(GVRShaderData mtl)
    {
        mtl.setFloat("u_gamma", 1.0f);
    }
}