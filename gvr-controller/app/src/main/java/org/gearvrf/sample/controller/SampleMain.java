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
package org.gearvrf.sample.controller;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.concurrent.Future;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.GearCursorController;
import org.gearvrf.io.GVRGazeCursorController;

import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

public class SampleMain extends GVRMain
{
    private static final String TAG = "SampleMain";

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private static final float CLICKED_COLOR_R = 0.5f;
    private static final float CLICKED_COLOR_G = 0.5f;
    private static final float CLICKED_COLOR_B = 1.0f;
    private static final float CLICKED_COLOR_A = 1.0f;

    private static final float SCALE = 200.0f;
    private static final float DEPTH = -7.0f;
    private static final float BOARD_OFFSET = 2.0f;
    private GVRScene mainScene;
    private GVRContext mGVRContext = null;
    private GVRActivity mActivity;
    private GVRSceneObject cursor;
    private GVRCursorController controller;

    SampleMain(GVRActivity activity)
    {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        mainScene.getEventReceiver().addListener(mPickHandler);
        GVRInputManager inputManager = mGVRContext.getInputManager();
        cursor = new GVRSceneObject(mGVRContext, mGVRContext.createQuad(1f, 1f),
                                    mGVRContext.getAssetLoader().loadTexture(
                                    new GVRAndroidResource(mGVRContext, R.raw.cursor)));
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);

        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mPickHandler);
                }
                controller = newController;
                newController.addPickEventListener(mPickHandler);
                newController.setCursor(cursor);
                newController.setCursorDepth(DEPTH);
                newController.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
            }
        });

        /*
         * Adding Boards
         */
        GVRSceneObject object = getColorBoard();
        object.getTransform().setPosition(0.0f, BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard1");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(0.0f, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard2");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard3");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard4");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        object.setName("MeshBoard5");
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard6");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        attachSphereCollider(object);
        object.setName("SphereBoard1");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        object.setName("SphereBoard2");
        attachSphereCollider(object);
        mainScene.addSceneObject(object);

        GVRMesh mesh = null;
        try
        {
            mesh = mGVRContext.getAssetLoader().loadMesh(
                    new GVRAndroidResource(mGVRContext, "bunny.obj"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mesh = null;
        }
        if (mesh == null)
        {
            mActivity.finish();
            Log.e(TAG, "Mesh was not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, 0.0f, DEPTH);
        object.setName("BoundsBunny1");
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(4.0f, 0.0f, DEPTH);
        attachBoundsCollider(object);
        object.setName("BoundsBunny2");
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(-4.0f, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBunny3");
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, -4.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBunny4");
        mainScene.addSceneObject(object);

        GVRAssetLoader assetLoader = gvrContext.getAssetLoader();
        GVRTexture texture = assetLoader.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSphereSceneObject skyBox = new GVRSphereSceneObject(gvrContext, false, material);
        skyBox.getTransform().setScale(SCALE, SCALE, SCALE);
        skyBox.getRenderData().getMaterial().setMainTexture(texture);
        mainScene.addSceneObject(skyBox);
    }

    private ITouchEvents mPickHandler = new GVREventListeners.TouchEvents()
    {
        private GVRSceneObject movingObject;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                                                           PICKED_COLOR_G, PICKED_COLOR_B,
                                                           PICKED_COLOR_A);
        }

        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if (movingObject == null)
            {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", CLICKED_COLOR_R,
                                                               CLICKED_COLOR_G, CLICKED_COLOR_B,
                                                               CLICKED_COLOR_A);
                if (controller.startDrag(sceneObj))
                {
                    movingObject = sceneObj;
                }
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                                                           PICKED_COLOR_G, PICKED_COLOR_B,
                                                           PICKED_COLOR_A);
            if (sceneObj == movingObject)
            {
                controller.stopDrag();
                movingObject = null;
            }
         }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                                                           UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                                                           UNPICKED_COLOR_A);
            if (sceneObj == movingObject)
            {
                controller.stopDrag();
                movingObject = null;
            }
        }
    };

    @Override
    public void onStep()
    {

    }

    private GVRSceneObject getColorBoard()
    {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                         UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRCubeSceneObject board = new GVRCubeSceneObject(mGVRContext);
        board.getRenderData().setMaterial(material);
        board.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        return board;
    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh)
    {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                         UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);

        GVRSceneObject meshObject = new GVRSceneObject(mGVRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);
        meshObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        return meshObject;
    }

    private void attachMeshCollider(GVRSceneObject sceneObject)
    {
        sceneObject.attachComponent(new GVRMeshCollider(mGVRContext, false));
    }

    private void attachSphereCollider(GVRSceneObject sceneObject)
    {
        sceneObject.attachComponent(new GVRSphereCollider(mGVRContext));
    }

    private void attachBoundsCollider(GVRSceneObject sceneObject)
    {
        sceneObject.attachComponent(new GVRMeshCollider(mGVRContext, true));
    }
}
