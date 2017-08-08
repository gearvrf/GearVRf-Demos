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

package org.gearvrf.keyboard.shader;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class TransparentButtonShaderThreeStates extends GVRShader {

    public static final String TEXTURE_KEY = "texture_t";
    public static final String TEXTURE_HOVER_KEY = "textureHover";

    public static final String TEXTURE_TEXT_KEY = "textTexture";
    public static final String TEXTURE_TEXT_HOVER_KEY = "textHoverTexture";

    public static final String TEXTURE_TEXT_UPPER_KEY = "textUpperTexture";
    public static final String TEXTURE_TEXT_HOVER_UPPER_KEY = "textHoverUpperTexture";

    public static final String TEXTURE_TEXT_SPECIAL_KEY = "textSpecialTexture";
    public static final String TEXTURE_TEXT_HOVER_SPECIAL_KEY = "textHoverSpecialTexture";

    public static final String TEXTURE_SWITCH = "textureSwitch";
    public static final String OPACITY = "u_opacity";

    private static final String VERTEX_SHADER = "" //
            + "#extension GL_ARB_separate_shader_objects : enable\n"
            + "#extension GL_ARB_shading_language_420pack : enable\n"
            + "precision mediump float;\n"
            + "layout(location = 0) in vec3 a_position;\n"
            //+ "in vec3 a_normal;\n" //
            + "layout(location = 1) in vec2 a_texcoord;\n"
            //          + "uniform mat4 u_mvp;\n" //
            //+ "out vec3 normal;\n"
            + "layout(location = 0) out vec2 coord;\n" //
            + "@MATRIX_UNIFORMS\n"
            //   "};"

            + "void main() {\n"
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp * vec4(a_position, 1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //+ "#version 300 es\n"
            + "#extension GL_ARB_separate_shader_objects : enable\n"
            + "#extension GL_ARB_shading_language_420pack : enable\n"
            + "precision mediump float;\n"
            + "layout(location = 0) in vec2  coord;\n"
            + "layout(binding = 4) uniform sampler2D "+ TEXTURE_KEY + ";\n"
            + "layout(binding = 5) uniform sampler2D "+ TEXTURE_HOVER_KEY + ";\n"
            + "layout(binding = 6) uniform sampler2D "+ TEXTURE_TEXT_KEY + ";\n"
            + "layout(binding = 7) uniform sampler2D "+ TEXTURE_TEXT_HOVER_KEY + ";\n"
            + "layout(binding = 8) uniform sampler2D "+ TEXTURE_TEXT_UPPER_KEY + ";\n"
            + "layout(binding = 9) uniform sampler2D "+ TEXTURE_TEXT_HOVER_UPPER_KEY + ";\n"
            + "layout(binding = 10) uniform sampler2D "+ TEXTURE_TEXT_SPECIAL_KEY + ";\n"
            + "layout(binding = 11) uniform sampler2D "+ TEXTURE_TEXT_HOVER_SPECIAL_KEY + ";\n"
            + "@MATERIAL_UNIFORMS\n"
            + "layout(location = 0) out vec4 outColor;\n"
            + "void main() {\n" //
//            + "  vec4 color = vec4(0.0, 0.0, 0.0, 0.0);\n"
            + "  vec4 color = texture(texture_t, coord);\n"
            + "  vec4 text = vec4(0.0, 0.0, 0.0, 1.0);\n"
            + " if(textureSwitch == 0.0){"
            + "  text = texture("+ TEXTURE_TEXT_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch == 1.0){"
            + "  text = texture("+ TEXTURE_TEXT_HOVER_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch == 2.0){"
            + "  text = texture("+ TEXTURE_TEXT_UPPER_KEY + ", coord);\n"
            + "  color = texture(" + TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch == 3.0){"
            + "  text = texture("+ TEXTURE_TEXT_HOVER_UPPER_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch == 4.0){"
            + "  text = texture("+ TEXTURE_TEXT_SPECIAL_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch == 5.0){"
            + "  text = texture("+ TEXTURE_TEXT_HOVER_SPECIAL_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + "  color = color + text;\n"
            + "  color = color * u_opacity;\n"
            + "  outColor = vec4(color);\n" //
            + "}\n";


    public TransparentButtonShaderThreeStates(GVRContext gvrContext) {
        super(" float u_opacity, float textureSwitch",
                "sampler2D texture_t sampler2D textureHover sampler2D textTexture sampler2D textHoverTexture sampler2D textUpperTexture sampler2D textHoverUpperTexture sampler2D textSpecialTexture sampler2D textHoverSpecialTexture",
                "float3 a_position, float2 a_texcoord", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setFloat("u_opacity", 1);
        material.setFloat("textureSwitch", 0);
    }

}
