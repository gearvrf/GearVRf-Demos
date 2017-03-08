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

package org.gearvrf.gvrexposeapi;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.IPickEvents;
import org.gearvrf.utility.Log;

import java.util.concurrent.Future;

public class SampleCubeMain extends GVRMain {

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onNoPick(GVRPicker picker)
        {
            PickedObject = null;
        }
        public void onPick(GVRPicker picker)
        {
            GVRPicker.GVRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
        }
    }

    private PickHandler mPickHandler;
    private GVRPicker mPicker;
    private static final float CUBE_WIDTH = 20.0f;
    private GVRContext mGVRContext = null;
    private GVRSceneObject mFrontFace = null;
    private GVRSceneObject mFrontFace2 = null;
    private GVRSceneObject mFrontFace3 = null;
    private static final float SCALE_FACTOR = 2.0f;
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        
        GVRScene scene = mGVRContext.getMainScene();

        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));
        Future<GVRTexture> futureCubemapTexture = gvrContext
                .loadFutureCubemapTexture(new GVRAndroidResource(mGVRContext,
                        R.raw.beach));
        mFrontFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace.setName("front");
        scene.addSceneObject(mFrontFace);
        mFrontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        mFrontFace2 = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace2.setName("front2");
        scene.addSceneObject(mFrontFace2);
        mFrontFace2.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f * 2.0f);

        mFrontFace3 = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        mFrontFace3.setName("front3");
        scene.addSceneObject(mFrontFace3);
        mFrontFace3.getTransform().setPosition(0.0f, 0.0f,
                -CUBE_WIDTH * 0.5f * 3.0f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.back)));
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.left)));
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        leftFace.getRenderData().setRenderMask(GVRRenderMaskBit.Left);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.right)));
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        rightFace.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.top)));
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.bottom)));
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

        mFrontFace.attachComponent(new GVRMeshCollider(gvrContext, mFrontFace.getRenderData().getMesh()));
        mFrontFace2.attachComponent(new GVRMeshCollider(gvrContext, mFrontFace2.getRenderData().getMesh()));
        mFrontFace3.attachComponent(new GVRMeshCollider(gvrContext, mFrontFace3.getRenderData().getMesh()));

        // reflective object
        Future<GVRMesh> futureSphereMesh = gvrContext
                .loadFutureMesh(new GVRAndroidResource(mGVRContext,
                        R.raw.sphere));
        GVRMaterial cubemapReflectionMaterial = new GVRMaterial(gvrContext,
                GVRMaterial.GVRShaderType.CubemapReflection.ID);
        cubemapReflectionMaterial.setMainTexture(futureCubemapTexture);
        cubemapReflectionMaterial.setOpacity(0.25f);

        GVRSceneObject sphere = new GVRSceneObject(gvrContext,
                futureSphereMesh, futureCubemapTexture);
        sphere.getRenderData().setMaterial(cubemapReflectionMaterial);
        sphere.setName("sphere");
        scene.addSceneObject(sphere);
        sphere.getTransform()
                .setScale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        sphere.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.25f);

        /*
         * Respond to picking events
         */
        mPicker = new GVRPicker(gvrContext, scene);
        mPickHandler = new PickHandler();
        scene.getEventReceiver().addListener(mPickHandler);
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
        mFrontFace.getRenderData().getMaterial().setOpacity(1.0f);
        mFrontFace2.getRenderData().getMaterial().setOpacity(1.0f);
        mFrontFace3.getRenderData().getMaterial().setOpacity(1.0f);
        if (mPickHandler.PickedObject.equals(mFrontFace)) {
            mFrontFace.getRenderData().getMaterial().setOpacity(0.5f);
        }
        if (mPickHandler.PickedObject.equals(mFrontFace2)) {
            mFrontFace2.getRenderData().getMaterial().setOpacity(0.5f);
        }
        if (mPickHandler.PickedObject.equals(mFrontFace3)) {
            mFrontFace3.getRenderData().getMaterial().setOpacity(0.5f);
        }

    }

}
