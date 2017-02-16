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

package org.gearvrf.controls.shaders;

import android.content.Context;

import org.gearvrf.GVRContext;
//import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderId;
import org.gearvrf.controls.R;
import org.gearvrf.utility.TextFile;

public class ButtonShader extends GVRShader{

    public static final String STATE1_BACKGROUND_TEXTURE = "state1Background";
    public static final String STATE1_TEXT_TEXTURE = "state1Text";
    public static final String STATE2_BACKGROUND_TEXTURE = "state2Background";
    public static final String STATE2_TEXT_TEXTURE = "state2Text";
    public static final String STATE3_BACKGROUND_TEXTURE = "state3Background";
    public static final String STATE3_TEXT_TEXTURE = "state3Text";
    public static final String TEXTURE_SWITCH = "textureSwitch";

    private GVRShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public ButtonShader(GVRContext gvrContext) {
        /*
        final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(R.raw.buttonshader_vertex,
                R.raw.buttonshader_fragment);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey(STATE1_BACKGROUND_TEXTURE, STATE1_BACKGROUND_TEXTURE);
        mCustomShader.addTextureKey(STATE1_TEXT_TEXTURE, STATE1_TEXT_TEXTURE);
        mCustomShader.addTextureKey(STATE2_BACKGROUND_TEXTURE, STATE2_BACKGROUND_TEXTURE);
        mCustomShader.addTextureKey(STATE2_TEXT_TEXTURE, STATE2_TEXT_TEXTURE);
        mCustomShader.addTextureKey(STATE3_BACKGROUND_TEXTURE, STATE3_BACKGROUND_TEXTURE);
        mCustomShader.addTextureKey(STATE3_TEXT_TEXTURE, STATE3_TEXT_TEXTURE);
        mCustomShader.addUniformFloatKey(TEXTURE_SWITCH, TEXTURE_SWITCH);
        mCustomShader.addUniformFloatKey("opacity", "opacity");*/

        super("float textureSwitch float opacity", "sampler2D state1Background sampler2D state1Text sampler2D state2Background sampler2D state2Text sampler2D state3Background sampler2D state3Text", "float3 a_position, float3 a_normal, float2 a_texcoord",300);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.buttonshader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context,R.raw.buttonshader_vertex));

    }

    public GVRShaderId getShaderId() {
        return mShaderId;
    }
}