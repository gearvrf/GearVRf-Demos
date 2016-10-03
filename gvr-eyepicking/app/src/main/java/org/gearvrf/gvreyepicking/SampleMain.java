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
package org.gearvrf.gvreyepicking;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.IPickEvents;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleMain extends GVRMain {
    public class PickHandler implements IPickEvents
    {
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R, PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
        }
        public void onExit(GVRSceneObject sceneObj)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        }
        public void onNoPick(GVRPicker picker) { }
        public void onPick(GVRPicker picker) { }
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }      
    }

    private static final String TAG = "SampleMain";

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;
    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private GVRContext mGVRContext = null;
    private List<GVRSceneObject> mObjects = new ArrayList<GVRSceneObject>();
    private IPickEvents mPickHandler = new PickHandler();
    private GVRPicker mPicker;

    private GVRActivity mActivity;
    
    SampleMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene mainScene = mGVRContext.getNextMainScene();

        mainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mainScene.getEventReceiver().addListener(mPickHandler);
        mPicker = new GVRPicker(gvrContext, mainScene);

        /*
         * Adding Boards
         */
        GVRSceneObject object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, -3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, 0.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, 0.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, 3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(3.0f, -3.0f, -5.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, 3.0f, -5.0f);
        attachSphereCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(-3.0f, -3.0f, -5.0f);
        attachSphereCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        /*
         * Adding bunnies.
         */

        GVRMesh mesh = null;
        try {
            mesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext,
                    "bunny.obj"));
        } catch (IOException e) {
            e.printStackTrace();
            mesh = null;
        }
        if (mesh == null) {
            mActivity.finish();
            Log.e(TAG, "Mesh was not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;

        // These 2 are testing by the whole mesh.
        object = getColorMesh(1.0f, mesh);
        object.getTransform().setPosition(0.0f, 0.0f, -2.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorMesh(1.0f, mesh);
        object.getTransform().setPosition(3.0f, 3.0f, -2.0f);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);
        object.getRenderData().setCullTest(false);
        mObjects.add(object);

        // These 2 are testing by the bounding box of the mesh.
        object = getColorMesh(2.0f, mesh);
        object.getTransform().setPosition(-5.0f, 0.0f, -2.0f);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getColorMesh(1.0f, mesh);
        object.getTransform().setPosition(0.0f, -5.0f, -2.0f);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);
        mObjects.add(object);
    }

    @Override
    public void onStep() {
    }

    private GVRSceneObject getColorBoard(float width, float height) {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.BeingGenerated.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(mGVRContext, width, height);
        board.getRenderData().setMaterial(material);
        board.getRenderData().setShaderTemplate(ColorShader.class);
        return board;
    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh) {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.BeingGenerated.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);

        GVRSceneObject meshObject = null;
        meshObject = new GVRSceneObject(mGVRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);
        meshObject.getRenderData().setShaderTemplate(ColorShader.class);
        return meshObject;
    }

    private void attachMeshCollider(GVRSceneObject sceneObject) {
        sceneObject.attachComponent(new GVRMeshCollider(mGVRContext, false));
    }

    private void attachSphereCollider(GVRSceneObject sceneObject) {
        sceneObject.attachComponent(new GVRSphereCollider(mGVRContext));
    }
    
    private void attachBoundsCollider(GVRSceneObject sceneObject) {
        sceneObject.attachComponent(new GVRMeshCollider(mGVRContext, true));
    }
}
