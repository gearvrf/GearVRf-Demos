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

package org.gearvrf.gvrswitch;

import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRConeSceneObject;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

public class SampleMain extends GVRMain {

    private GVRContext mGVRContext = null;
    private GVRScene scene = null;
    private GVRSwitch mSwitchNode;
    private Integer mSelectedIndex = 0;
    private int mMaxIndex = 0;
    private int counter = 0;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        scene = mGVRContext.getMainScene();

        /*
         * Add a head tracking pointer to the scene
         */
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.headtrackingpointer));
        GVRSceneObject headTracker = new GVRSceneObject(gvrContext, gvrContext.createQuad(0.1f, 0.1f), texture);
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        scene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add a light to the scene that looks down the negative Z axis
         */
        GVRSceneObject lightObj = new GVRSceneObject(gvrContext);
        GVRDirectLight light = new GVRDirectLight(gvrContext);
        
        lightObj.getTransform().setPositionZ(2.0f);
        lightObj.attachComponent(light);
        scene.addSceneObject(lightObj);
        /*
         * Add a root node with four geometric shapes as children
         */
        GVRMaterial red = new GVRMaterial(gvrContext);
        GVRMaterial blue = new GVRMaterial(gvrContext);
        GVRSceneObject root = new GVRSceneObject(gvrContext);
        GVRCubeSceneObject cube = new GVRCubeSceneObject(gvrContext, true, red);
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(gvrContext, true, blue);
        GVRCylinderSceneObject cylinder = new GVRCylinderSceneObject(gvrContext, true, red);
        GVRConeSceneObject cone = new GVRConeSceneObject(gvrContext, true, blue);
        
        mMaxIndex = 3;
        red.setDiffuseColor(1,  0,  0, 1);
        blue.setDiffuseColor(0, 0,  1, 1);
        cube.setName("cube");
        cube.getRenderData().setShaderTemplate(GVRPhongShader.class);
        sphere.setName("sphere");
        sphere.getRenderData().setShaderTemplate(GVRPhongShader.class);
        cylinder.getRenderData().setShaderTemplate(GVRPhongShader.class);
        cylinder.setName("cylinder");
        cone.getRenderData().setShaderTemplate(GVRPhongShader.class);
        cone.setName("cone");
        root.addChildObject(cube);
        root.addChildObject(sphere);
        root.addChildObject(cylinder);
        root.addChildObject(cone);
        root.getTransform().setPositionZ(-5.0f);
        mSwitchNode = new GVRSwitch(gvrContext);
        root.attachComponent(mSwitchNode);
        scene.addSceneObject(root);
    }


    public void onStep() {
        counter++;
        if(counter > 120) {
            mSelectedIndex++;
            if (mSelectedIndex > 3) {
                mSelectedIndex = 0;
            }
            Log.d("ASD","Set Switch Index:" + mSelectedIndex);
            mSwitchNode.setSwitchIndex(mSelectedIndex);
            counter = 0;
        }
    }

}
