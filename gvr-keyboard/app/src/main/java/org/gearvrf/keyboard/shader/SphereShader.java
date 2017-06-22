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

import android.content.Context;

import org.gearvrf.GVRContext;
//import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;
import org.gearvrf.keyboard.R;
import org.gearvrf.utility.TextFile;

public class SphereShader extends GVRShader{

    public static final String LIGHT_KEY = "u_light";
    public static final String EYE_KEY = "u_eye";
    public static final String TRANSITION_COLOR = "trans_color";
    public static final String TEXTURE_KEY = "texture_t";
    public static final String SECUNDARY_TEXTURE_KEY = "second_texture";
    public static final String ANIM_TEXTURE = "animTexture";
    public static final String BLUR_INTENSITY = "blur";
    public static final String HDRI_TEXTURE_KEY = "HDRI_texture";

    //private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public SphereShader(GVRContext gvrContext) {
        /*final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();

        mShaderId = shaderManager.addShader(R.raw.sphereshader_vertex, R.raw.sphereshader_fragment);

        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformVec3Key(LIGHT_KEY, LIGHT_KEY);
        mCustomShader.addUniformVec3Key(TRANSITION_COLOR, TRANSITION_COLOR);
        mCustomShader.addUniformVec3Key(EYE_KEY, EYE_KEY);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        mCustomShader.addTextureKey(SECUNDARY_TEXTURE_KEY, SECUNDARY_TEXTURE_KEY);
        mCustomShader.addUniformFloatKey(ANIM_TEXTURE, ANIM_TEXTURE);
        mCustomShader.addUniformFloatKey(BLUR_INTENSITY, BLUR_INTENSITY);
        mCustomShader.addTextureKey("hdri_texture", HDRI_TEXTURE_KEY);*/

        super("float3 u_eye, float3 u_light, float3 trans_color, float animTexture, float blur, float u_radius", "sampler2D texture_t sampler2D second_texture sampler2D HDRI_texture", "float4 a_position, float3 a_normal, float2 a_texcoord",300);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.sphereshader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.sphereshader_vertex));
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {

        material.setVec3("u_eye", 0, 0, 0);
        material.setVec3("u_light", 1, 1, 1);
        material.setVec3("trans_color", 1, 1, 1);
        material.setFloat("blur", 1);
        material.setFloat("animTexture", 1);
        material.setFloat("u_radius", 1);
    }
}
