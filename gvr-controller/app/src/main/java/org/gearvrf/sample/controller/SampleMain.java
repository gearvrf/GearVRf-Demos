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
import org.gearvrf.scene_objects.GVRCursorControllerSceneObject;
import org.gearvrf.scene_objects.GVRGearControllerSceneObject;
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
    private GVRSceneObject mControllerModel;
    private GVRScene mainScene;
    private GVRContext mGVRContext = null;
    private GVRActivity mActivity;
    private GearCursorController mGearController;
    private GVRGazeCursorController mGazeController;
    private GVRCursorController mCursorController;
    private GVRSceneObject cursor;

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
        inputManager.addCursorControllerListener(cursorControllerListener);
        cursor = new GVRSceneObject(mGVRContext, mGVRContext.createQuad(1f, 1f),
                                    mGVRContext.getAssetLoader().loadTexture(
                                    new GVRAndroidResource(mGVRContext, R.raw.cursor)));
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);

        /*
         * Adding Boards
         */
        GVRSceneObject object = getColorBoard();
        object.getTransform().setPosition(0.0f, BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(0.0f, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        attachSphereCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
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
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(4.0f, 0.0f, DEPTH);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(-4.0f, 0.0f, DEPTH);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, -4.0f, DEPTH);
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        GVRAssetLoader assetLoader = gvrContext.getAssetLoader();
        Future<GVRTexture> futureTexture = assetLoader.loadFutureTexture(
                new GVRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSphereSceneObject skyBox = new GVRSphereSceneObject(gvrContext, false, material);
        skyBox.getTransform().setScale(SCALE, SCALE, SCALE);
        skyBox.getRenderData().getMaterial().setMainTexture(futureTexture);
        mainScene.addSceneObject(skyBox);
    }

    private CursorControllerListener cursorControllerListener = new CursorControllerListener()
    {
        @Override
        public void onCursorControllerAdded(GVRCursorController gvrCursorController)
        {
            if (mCursorController == gvrCursorController)
            {
                return;
            }
            if ((gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) &&
                gvrCursorController.isConnected())
            {
                mGearController = (GearCursorController) gvrCursorController;
                if (mCursorController != null)
                {
                    deselectController();
                }
                selectGearController(gvrCursorController);
            }
            else if ((gvrCursorController.getControllerType() == GVRControllerType.GAZE) &&
                     gvrCursorController.isConnected())
            {
                mGazeController = (GVRGazeCursorController) gvrCursorController;
                if (mCursorController == gvrCursorController)
                {
                    return;
                }
                if ((mCursorController == null) ||
                    mCursorController.getControllerType() != GVRControllerType.CONTROLLER)
                {
                    selectGazeController(gvrCursorController);
                }
                else
                {
                    gvrCursorController.setEnable(false);
                }
            }
            else
            {
                gvrCursorController.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController)
        {
            if (gvrCursorController == mCursorController)
            {
                deselectController();
            }
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER)
            {
                android.util.Log.d(TAG, "Removed remote orientation controller");
            }
            else if (gvrCursorController.getControllerType() == GVRControllerType.GAZE)
            {
                android.util.Log.d(TAG, "Removed gaze controller");
            }
        }

        public void onCursorControllerActive(GVRCursorController gvrCursorController)
        {
            onCursorControllerAdded(gvrCursorController);
        }


        public void selectGearController(GVRCursorController controller)
        {
            GVRGearControllerSceneObject controllerModel = new GVRGearControllerSceneObject(mGVRContext);
            controllerModel.setCursorController(controller);
            controllerModel.setRayDepth(DEPTH);
            controllerModel.setCursor(cursor);
            controllerModel.enableSurfaceProjection();
            controller.addPickEventListener(mPickHandler);
            mControllerModel = controllerModel;
            mCursorController = controller;
            mGearController = (GearCursorController) controller;
        }


        public void selectGazeController(GVRCursorController controller)
        {
            GVRCursorControllerSceneObject controllerModel = new GVRCursorControllerSceneObject(mGVRContext);
            mainScene.getMainCameraRig().getHeadTransformObject().addChildObject(controllerModel);
            controllerModel.setCursorController(controller);
            controllerModel.setRayDepth(DEPTH);
            controllerModel.setCursor(cursor);
            controllerModel.enableSurfaceProjection();
            controller.addPickEventListener(mPickHandler);
            mControllerModel = controllerModel;
            mCursorController = controller;
            mGazeController = (GVRGazeCursorController) controller;
        }

        public void deselectController()
        {
            if (mCursorController != null)
            {
                mCursorController.removePickEventListener(mPickHandler);
                mCursorController.setEnable(false);
                mCursorController = null;
            }
            if (mControllerModel != null)
            {
                mControllerModel.setEnable(false);
                mControllerModel.getParent().removeChildObject(mControllerModel);
                mControllerModel = null;
            }
        }

        public void onCursorControllerInactive(GVRCursorController gvrCursorController)
        {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER)
            {
                android.util.Log.d(TAG, "Deactivated orientation remote controller");
                deselectController();
                if (mGazeController != null)
                {
                    selectGazeController(mGazeController);
                }
            }
            else if (gvrCursorController.getControllerType() == GVRControllerType.GAZE)
            {
                android.util.Log.d(TAG, "Deactivate gaze controller");
            }
        }

    };

    private ITouchEvents mPickHandler = new ITouchEvents()
    {
        private GVRSceneObject movingObject;

        private void stopMove()
        {
            GVRTransform objTrans = movingObject.getTransform();
            Matrix4f cursorMatrix = mControllerModel.getTransform().getModelMatrix4f();
            mControllerModel.removeChildObject(movingObject);
            mainScene.addSceneObject(movingObject);
            Matrix4f objMatrix = objTrans.getModelMatrix4f();
            objTrans.setModelMatrix(cursorMatrix.mul(objMatrix));
            movingObject = null;
        }

        private void startMove(GVRSceneObject sceneObj)
        {
            GVRTransform objTrans = sceneObj.getTransform();
            movingObject = sceneObj;
            Matrix4f controllerMtx = mControllerModel.getTransform().getModelMatrix4f();
            Matrix4f objMatrix = objTrans.getModelMatrix4f();
            controllerMtx.invert();
            objTrans.setModelMatrix(controllerMtx.mul(objMatrix));
            mainScene.removeSceneObject(sceneObj);
            mControllerModel.addChildObject(sceneObj);
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                                                           PICKED_COLOR_G, PICKED_COLOR_B,
                                                           PICKED_COLOR_A);
        }

        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if ((mControllerModel != null) && (movingObject == null))
            {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", CLICKED_COLOR_R,
                                                               CLICKED_COLOR_G, CLICKED_COLOR_B,
                                                               CLICKED_COLOR_A);
                startMove(sceneObj);
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                                                           PICKED_COLOR_G, PICKED_COLOR_B,
                                                           PICKED_COLOR_A);
            if ((mControllerModel != null) && (sceneObj == movingObject))
            {
                stopMove();
            }
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                                                           UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                                                           UNPICKED_COLOR_A);
            if ((mControllerModel != null) && (sceneObj == movingObject))
            {
                stopMove();
            }
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
        }

    };

    @Override
    public void onStep()
    {

    }

    private GVRSceneObject getColorBoard()
    {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.BeingGenerated.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                         UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRCubeSceneObject board = new GVRCubeSceneObject(mGVRContext);
        board.getRenderData().setMaterial(material);
        board.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        board.getRenderData().setShaderTemplate(ColorShader.class);
        return board;
    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh)
    {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.BeingGenerated.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                         UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);

        GVRSceneObject meshObject = new GVRSceneObject(mGVRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);
        meshObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        meshObject.getRenderData().setShaderTemplate(ColorShader.class);
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
