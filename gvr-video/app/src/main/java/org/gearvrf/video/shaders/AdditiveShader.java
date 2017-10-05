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


package org.gearvrf.video.shaders;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class AdditiveShader extends GVRShader{

    public static final String TEXTURE_KEY = "texture";
    public static final String WEIGHT_KEY = "u_weight";
    public static final String FADE_KEY = "u_fade";

    private static final String VERTEX_SHADER = "" //
            + "precision highp float;\n"
            + "in vec3 a_position;\n" //
            + "in vec2 a_texcoord;\n"

            + "@MATRIX_UNIFORMS\n"
            + "out vec2 coord;\n"
            + "void main() {\n" //
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp * vec4(a_position,1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision highp float;\n"
            + "in vec2  coord;\n" //

            + "@MATERIAL_UNIFORMS\n"
            + "uniform sampler2D u_texture;\n"

            + "out vec4 FragColor;\n"
            + "void main() {\n"
            + "  vec3 color1 = texture(u_texture, coord).rgb;\n"
            + "  vec3 color2 = vec3(0.0);\n"
            + "  vec3 color  = color1*(1.0-u_weight)+color2*u_weight;\n"
            + "  float alpha = length(color);\n"
            + "  FragColor = vec4( u_fade*color, alpha );\n" //
            + "}\n";

    public AdditiveShader(GVRContext gvrContext) {
        super("float u_weight, float u_fade", "sampler2D u_texture", "float3 a_position, float2 a_tex_coord", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setFloat("u_weight", 1);
        material.setFloat("u_fade", 1);
    }
}