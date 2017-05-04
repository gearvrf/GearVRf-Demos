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
import org.gearvrf.GVRPostEffect;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRPostEffectShaderManager;
import org.gearvrf.GVRShaderTemplate;

import java.lang.Runnable;

public class DisplayUtils {
    GVRPostEffect postEffect;
    GVRContext gvrContext;

    public DisplayUtils(GVRContext context) {
        gvrContext = context;
    }

    public void addGammaCorrection() {
        // add a custom post effect for dynamically adjusting gamma
        GVRPostEffectShaderManager shaderManager = gvrContext.getPostEffectShaderManager();
        GVRShaderTemplate postEffectShader = shaderManager.retrieveShaderTemplate(CustomPostEffectShaderManager.class);
        postEffect = new GVRPostEffect(gvrContext, GVRPostEffect.GVRPostEffectShaderType.HorizontalFlip.ID);
        postEffect.setFloat("u_gamma", 2.2f);
        postEffectShader.bindShader(gvrContext, postEffect);
        GVRCameraRig rig = gvrContext.getMainScene().getMainCameraRig();
        rig.getLeftCamera().addPostEffect(postEffect);
        rig.getRightCamera().addPostEffect(postEffect);
    }

    public void setGamma(float gammaLevel) {
        final float gamma = gammaLevel;
        gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    if(postEffect == null) {
                        addGammaCorrection();
                    }
                    postEffect.setFloat("u_gamma", gamma);
                }
            });
    }
}

