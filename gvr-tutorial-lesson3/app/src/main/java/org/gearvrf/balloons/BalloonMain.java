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
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import android.view.MotionEvent;
import org.gearvrf.GVRPicker;
import org.gearvrf.IPickEvents;
import org.gearvrf.GVRPicker.GVRPickedObject;

import java.util.concurrent.Future;

public class BalloonMain extends GVRMain {

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject   PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onNoPick(GVRPicker picker)
        {
            if (PickedObject != null)
            {
                PickedObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 0, 0.5f);
            }
            PickedObject = null;
        }
        public void onPick(GVRPicker picker)
        {
            GVRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
            PickedObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 1, 0.5f);
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    }

    private GVRScene mScene = null;
    private PickHandler mPickHandler;

    @Override
    public void onInit(GVRContext context) {
        /*
         * Set the background color
         */
        mScene = context.getNextMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        GVRSceneObject headTracker = new GVRSceneObject(context,
                context.createQuad(0.1f, 0.1f),
                context.loadTexture(new GVRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the environment and a single balloon
         */
        GVRSceneObject balloon = makeBalloon(context);
        mScene.addSceneObject(balloon);
        GVRSceneObject environment = makeEnvironment(context);
        mScene.addSceneObject(environment);
        /*
         * Respond to picking events
         */
        mScene.getMainCameraRig().getOwnerObject().attachComponent(new GVRPicker(context, mScene));
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
    }
    

    GVRSceneObject makeBalloon(GVRContext context)
    {
        GVRSceneObject sphere = new GVRSphereSceneObject(context, true);
        GVRRenderData rdata = sphere.getRenderData();
        GVRMaterial mtl = new GVRMaterial(context);
        GVRSphereCollider collider = new GVRSphereCollider(context);

        collider.setRadius(1.0f);
        sphere.attachComponent(collider);
        mtl.setDiffuseColor(1.0f, 0.0f, 1.0f, 0.5f);
        sphere.setName("balloon");
        rdata.setShaderTemplate(GVRPhongShader.class);
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mtl);
        rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        sphere.getTransform().setPositionZ(-3.0f);
        return sphere;
    }

    GVRSceneObject makeEnvironment(GVRContext context)
    {
        Future<GVRTexture> tex = context.loadFutureCubemapTexture(new GVRAndroidResource(context, R.raw.lycksele3));
        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        GVRSphereSceneObject environment = new GVRSphereSceneObject(context, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        GVRDirectLight sunLight = new GVRDirectLight(context);
        sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
        sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
        environment.attachComponent(sunLight);
        return environment;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if (mPickHandler.PickedObject != null)
                {
                    mPickHandler.PickedObject.getRenderData().getMaterial().setDiffuseColor(0, 0, 1, 1);
                }
                break;

            default:
                break;
        }
    }

}
