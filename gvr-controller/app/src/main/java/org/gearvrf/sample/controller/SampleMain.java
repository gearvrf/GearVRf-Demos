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
import android.view.KeyEvent;

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
import org.gearvrf.IPickEvents;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRGearControllerSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

public class SampleMain extends GVRMain {
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
    private GVRGearControllerSceneObject controller;

    private GVRScene mainScene;

    private enum ButtonState {
        UP,
        DOWN
    }

    private GVRContext mGVRContext = null;
    private PickHandler mPickHandler = new PickHandler();
    private GVRSceneObject selectedObject;

    private GVRActivity mActivity;
    private GVRPicker mPicker;

    SampleMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        mainScene.getEventReceiver().addListener(mPickHandler);

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
        mPicker = new GVRPicker(mGVRContext, mainScene);

        GVRMesh mesh = null;
        try {
            mesh = mGVRContext.getAssetLoader().loadMesh(new GVRAndroidResource(mGVRContext, "bunny.obj"));
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

        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(cursorControllerListener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            cursorControllerListener.onCursorControllerAdded(cursor);
        }

        Future<GVRTexture> futureTexture = assetLoader.loadFutureTexture(new
                GVRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSphereSceneObject skyBox = new GVRSphereSceneObject(gvrContext, false, material);
        skyBox.getTransform().setScale(SCALE, SCALE, SCALE);
        skyBox.getRenderData().getMaterial().setMainTexture(futureTexture);
        mainScene.addSceneObject(skyBox);
    }

    private CursorControllerListener cursorControllerListener = new CursorControllerListener() {
        @Override
        public void onCursorControllerAdded(GVRCursorController gvrCursorController) {

            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                android.util.Log.d(TAG, "Got the orientation remote controller");
                GVRSceneObject cursor = new GVRSceneObject(mGVRContext,
                        mGVRContext.createQuad(1f, 1f),
                        mGVRContext.getAssetLoader().loadTexture(new GVRAndroidResource(mGVRContext, R.raw.cursor)));
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                controller = new GVRGearControllerSceneObject(mGVRContext);
                controller.setCursorController(gvrCursorController);
                mPicker.setPickRay(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, DEPTH);
                controller.setRayDepth(DEPTH);
                controller.setCursor(cursor);
                controller.attachComponent(mPicker);
                gvrCursorController.addControllerEventListener(controllerEventListener);
            }else{
                gvrCursorController.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                android.util.Log.d(TAG, "Got the orientation remote controller");
                gvrCursorController.removeControllerEventListener(controllerEventListener);
                gvrCursorController.resetSceneObject();
            }
        }
    };

    private GVRCursorController.ControllerEventListener controllerEventListener = new
            GVRCursorController.ControllerEventListener() {
        @Override
        public void onEvent(GVRCursorController gvrCursorController) {
            KeyEvent keyEvent = gvrCursorController.getKeyEvent();
            if(keyEvent != null){
                mPickHandler.setClicked(keyEvent.getAction() == KeyEvent.ACTION_DOWN);
            }
        }
    };



    private class PickHandler implements IPickEvents {
        boolean clicked = false;
        boolean prevClicked = false;
        ButtonState state = ButtonState.UP;

        public void setClicked(boolean clicked) {
            prevClicked = this.clicked;
            this.clicked = clicked;

            if (clicked && !prevClicked) {
                state = ButtonState.DOWN;
            } else if (!clicked && prevClicked) {
                state = ButtonState.UP;
            }

            if (state == ButtonState.UP && selectedObject != null) {
                GVRTransform selectedTransform = selectedObject.getTransform();
                Matrix4f cursorModelMatrix = controller.getTransform().getModelMatrix4f();
                controller.removeChildObject(selectedObject);
                mainScene.addSceneObject(selectedObject);
                Matrix4f selectedModelMatrix = selectedTransform.getModelMatrix4f();
                selectedTransform.setModelMatrix(cursorModelMatrix.mul(selectedModelMatrix));
                selectedObject.getCollider().setEnable(true);
                selectedObject = null;
            }
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            if (!clicked) {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                        PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
            } else {

                sceneObj.getRenderData().getMaterial().setVec4("u_color", CLICKED_COLOR_R,
                        CLICKED_COLOR_G, CLICKED_COLOR_B, CLICKED_COLOR_A);
            }
        }

        public void onExit(GVRSceneObject sceneObj) {
            if (selectedObject != sceneObj) {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                        UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
            }
        }

        public void onNoPick(GVRPicker picker) {
        }

        public void onPick(GVRPicker picker) {
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            if (!clicked) {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                        PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
            } else {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", CLICKED_COLOR_R,
                        CLICKED_COLOR_G, CLICKED_COLOR_B, CLICKED_COLOR_A);
                if (state == ButtonState.DOWN && selectedObject == null) {
                    sceneObj.getCollider().setEnable(false);
                    selectedObject = sceneObj;
                    Matrix4f matrix4f = controller.getTransform().getModelMatrix4f();
                    Matrix4f selectedModelMatrix = selectedObject.getTransform().getModelMatrix4f();
                    matrix4f.invert();
                    selectedObject.getTransform().setModelMatrix(matrix4f.mul(selectedModelMatrix));
                    mainScene.removeSceneObject(selectedObject);
                    controller.addChildObject(selectedObject);
                }
            }
        }
    }

    @Override
    public void onStep() {

    }

    private GVRSceneObject getColorBoard() {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.BeingGenerated.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRCubeSceneObject board = new GVRCubeSceneObject(mGVRContext);
        board.getRenderData().setMaterial(material);
        board.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        board.getRenderData().setShaderTemplate(ColorShader.class);
        return board;
    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh) {
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
