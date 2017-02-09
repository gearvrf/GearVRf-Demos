/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.samsung.accessibility.util;

import android.content.Context;

import org.gearvrf.GVRContext;
//import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRShader;
import org.gearvrf.utility.TextFile;

import com.samsung.accessibility.R;

public class AccessibilitySceneShader extends GVRShader{

    public static final String TEXTURE_KEY = "u_texture";
    public static final String BLUR_INTENSITY = "blur";
    //private GVRCustomMaterialShaderId mShaderId;
    private GVRMaterialMap mCustomShader = null;

    public AccessibilitySceneShader(GVRContext gvrContext) {
        /*final GVRMaterialShaderManager shaderManager = gvrContext
                .getMaterialShaderManager();
        mShaderId = shaderManager.addShader(R.raw.scene_shader_vertex, R.raw.scene_shader_fragment);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addTextureKey("texture", TEXTURE_KEY);
        mCustomShader.addUniformFloatKey(BLUR_INTENSITY, BLUR_INTENSITY);
        */
        super("float blur", "sampler2D u_texture", "float3 a_position float2 a_texcoord", 300);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.scene_shader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.pos_tex_ubo));
    }

    /*public GVRCustomMaterialShaderId getShaderId() {
        return mShaderId;
    }*/
}
