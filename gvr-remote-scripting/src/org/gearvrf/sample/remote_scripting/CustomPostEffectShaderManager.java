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
import org.gearvrf.GVRCustomPostEffectShaderId;
import org.gearvrf.GVRPostEffectMap;
import org.gearvrf.GVRPostEffectShaderManager;

public class CustomPostEffectShaderManager {

    private final GVRCustomPostEffectShaderId mShaderId;
    private GVRPostEffectMap mCustomShader;

    public CustomPostEffectShaderManager(GVRContext gvrContext) {
        final GVRPostEffectShaderManager shaderManager = gvrContext
                .getPostEffectShaderManager();
        mShaderId = shaderManager.addShader(R.raw.vertex, R.raw.fragment);
        mCustomShader = shaderManager.getShaderMap(mShaderId);
        mCustomShader.addUniformFloatKey("u_gamma", "u_gamma");
    }

    public GVRCustomPostEffectShaderId getShaderId() {
        return mShaderId;
    }
}
