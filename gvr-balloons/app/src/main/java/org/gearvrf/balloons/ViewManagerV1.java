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

package org.gearvrf.balloons;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import android.view.MotionEvent;

public class ViewManagerV1 extends GVRMain {

    private GVRContext mGVRContext = null;
    private GVRScene mScene = null;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        /*
         * Set the background color
         */
        mScene = mGVRContext.getNextMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        GVRSceneObject headTracker = new GVRSceneObject(gvrContext,
                new FutureWrapper<GVRMesh>(gvrContext.createQuad(0.1f, 0.1f)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);                
        /*
         * Make balloon particle
         */
        GVRSceneObject particleProto = makeBalloon(gvrContext);
        mScene.addSceneObject(particleProto);
    }
    
    /*
     * Make balloon particle
     */
    GVRSceneObject makeBalloon(GVRContext gvrContext)
    {
        GVRSceneObject particleProto = new GVRSphereSceneObject(gvrContext, true);
        GVRRenderData rdata = particleProto.getRenderData();
        GVRMaterial mtl = new GVRMaterial(gvrContext);
        mtl.setDiffuseColor(1.0f, 0.0f, 1.0f, 0.5f);
        particleProto.setName("balloon");
        rdata.setShaderTemplate(GVRPhongShader.class);
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mtl);
        rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        particleProto.getTransform().setPositionZ(-3.0f);
        return particleProto;
    }
    
    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
    }

}
