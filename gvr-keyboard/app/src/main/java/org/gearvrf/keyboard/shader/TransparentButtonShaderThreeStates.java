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
//import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
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
            + "#version 300 es\n" +
            "precision mediump float;\n"
            + "in vec4 a_position;\n"
            + "in vec3 a_normal;\n" //
            + "in vec2 a_texcoord;\n"
            //          + "uniform mat4 u_mvp;\n" //
            + "out vec3 normal;\n"
            + "out vec2 coord;\n" //
            + "layout (std140) uniform Transform_ubo{\n" +

            " #ifdef HAS_MULTIVIEW\n" +
            "     mat4 u_view_[2];\n" +
            "     mat4 u_mvp_[2];\n" +
            "     mat4 u_mv_[2];\n" +
            "     mat4 u_mv_it_[2];\n" +
            " #else\n" +
            "     mat4 u_view;\n" +
            "     mat4 u_mvp;\n" +
            "     mat4 u_mv;\n" +
            "     mat4 u_mv_it;\n" +
            " #endif\n" +
            "     mat4 u_model;\n" +
            "     mat4 u_view_i;\n" +
            "     vec4 u_right;\n" +
            "};"

            + "void main() {\n"
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "#version 300 es\n"
            + "precision mediump float;\n"
            + "in vec2  coord;\n"
            + "uniform sampler2D "+ TEXTURE_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_HOVER_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_HOVER_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_UPPER_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_HOVER_UPPER_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_SPECIAL_KEY + ";\n"
            + "uniform sampler2D "+ TEXTURE_TEXT_HOVER_SPECIAL_KEY + ";\n"
//            + "uniform float u_opacity;\n"
//            + "uniform float " + TEXTURE_SWITCH + ";\n"
            + "layout (std140) uniform Material_ubo{\n" +
            "    vec4 u_opacity;\n" +
            "    vec4 textureSwitch;\n" +
            "};\n"
            + "out vec4 outColor;\n"
            + "void main() {\n" //
//            + "  vec4 color = vec4(0.0, 0.0, 0.0, 0.0);\n"
            + "  vec4 color = texture(texture_t, coord);\n"
            + "  vec4 text = vec4(0.0, 0.0, 0.0, 1.0);\n"
            + " if(textureSwitch.x== 0.0){"
            + "  text = texture("+ TEXTURE_TEXT_KEY + ", coord);\n"
            + "  vec4 color = texture("+ TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch.x == 1.0){"
            + "  text = texture("+ TEXTURE_TEXT_HOVER_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch.x== 2.0){"
            + "  text = texture("+ TEXTURE_TEXT_UPPER_KEY + ", coord);\n"
            + "  color = texture(" + TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch.x == 3.0){"
            + "  text = texture("+ TEXTURE_TEXT_HOVER_UPPER_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch.x == 4.0){"
            + "  text = texture("+ TEXTURE_TEXT_SPECIAL_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_KEY + ", coord);\n"
            + " }"
            + " if(textureSwitch.x== 5.0){"
            + "  text = texture("+ TEXTURE_TEXT_HOVER_SPECIAL_KEY + ", coord);\n"
            + "  color = texture("+ TEXTURE_HOVER_KEY + ", coord);\n"
            + " }"
            + "  color = color + text;\n"
            + "  color = color * u_opacity.x;\n"
            + "  outColor = vec4(color);\n" //
            + "}\n";


    // private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public TransparentButtonShaderThreeStates(GVRContext gvrContext) {
        /*
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);

        mCustomShader.addTextureKey("textureHover", TEXTURE_HOVER_KEY);
        mCustomShader.addTextureKey("textHoverTexture", TEXTURE_TEXT_HOVER_KEY);

        mCustomShader.addTextureKey("textUpperTexture", TEXTURE_TEXT_UPPER_KEY);
        mCustomShader.addTextureKey("textHoverUpperTexture", TEXTURE_TEXT_HOVER_UPPER_KEY);

        mCustomShader.addTextureKey(TEXTURE_TEXT_SPECIAL_KEY, TEXTURE_TEXT_SPECIAL_KEY);
        mCustomShader.addTextureKey(TEXTURE_TEXT_HOVER_SPECIAL_KEY, TEXTURE_TEXT_HOVER_SPECIAL_KEY);

        mCustomShader.addTextureKey("textTexture", TEXTURE_TEXT_KEY);
        mCustomShader.addUniformFloatKey("opacity", OPACITY);
        mCustomShader.addUniformFloatKey("textureSwitch", TEXTURE_SWITCH);*/
        super(" float u_opacity, float textureSwitch",
                "sampler2D texture_t sampler2D textureHover sampler2D textTexture sampler2D textHoverTexture sampler2D textUpperTexture sampler2D textHoverUpperTexture sampler2D textSpecialTexture sampler2D textHoverSpecialTexture",
                "float4 a_position, float3 a_normal, float2 a_texcoord");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        //material.setVec4("u_color", 1, 1, 1, 1);
        material.setFloat("u_opacity", 1);
        material.setFloat("textureSwitch", 0);
    }

}
