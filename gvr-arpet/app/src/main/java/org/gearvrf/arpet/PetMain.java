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

package org.gearvrf.arpet;

import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.ITouchEvents;
import org.gearvrf.arpet.events.CollisionEvent;
import org.gearvrf.arpet.gesture.scale.BaseScalableObject;
import org.gearvrf.arpet.gesture.scale.ScalableObjectManager;
import org.gearvrf.arpet.movement.targetwrapper.BallWrapper;
import org.gearvrf.arpet.petobjects.Bed;
import org.gearvrf.arpet.petobjects.Bowl;
import org.gearvrf.arpet.petobjects.Hydrant;
import org.gearvrf.arpet.petobjects.Toy;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.physics.GVRWorld;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PetMain extends GVRMain {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private GVRContext mContext;
    private PetActivity.PetContext mPetContext;
    private GVRMixedReality mMixedReality;

    private BallThrowHandler ballThrowHandler;
    private PlaneHandler planeHandler;

    private GVRSceneObject cube;
    private Character mPet;

    private Hud mHud;

    public PetMain(PetActivity.PetContext petContext) {
        mPetContext = petContext;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        mMixedReality = new GVRMixedReality(gvrContext);
        mMixedReality.resume();

        GVRWorld world = new GVRWorld(gvrContext);
        world.setGravity(0f, -50f, 0f);
        mScene.getRoot().attachComponent(world);

        ballThrowHandler = new BallThrowHandler(gvrContext);

        planeHandler = new PlaneHandler(gvrContext, mPetContext, mMixedReality);
        mMixedReality.registerPlaneListener(planeHandler);

        mHud = new Hud(mContext);
        mHud.registerListener(new HudEventHandler());


        cube = new GVRSceneObject(gvrContext);
        cube.getTransform().setPosition(0f, 0f, -10f);
        GVRBoxCollider collider = new GVRBoxCollider(gvrContext);
        collider.setHalfExtents(0.5f, 0.5f, 0.5f);
        cube.attachComponent(collider);

        mScene.addSceneObject(cube);

        disableCursor();
    }

    public void resume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void pause() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe
    public void onPlaneDetected(final GVRPlane plane) {
        mPet = new Character(mContext, mMixedReality, plane.getCenterPose());
        mScene.addSceneObject(mPet.getAnchor());

        mPet.lookAt(new BallWrapper(ballThrowHandler.getBall()));

        // addPetObjectsToPlane(plane);

        mScene.getMainCameraRig().addChildObject(mHud);

        // setEditModeEnabled(true);
        // movePetToScreen(plane);
    }

    private void setEditModeEnabled(boolean enabled) {
        if (mPet != null) {
            mPet.setRotationEnabled(enabled);
            mPet.setScaleEnabled(enabled);
            mPet.setDraggingEnabled(enabled);
        }
    }

    private void movePetToScreen(final GVRPlane boundary) {
        mPetContext.runDelayedOnPetThread(new Runnable() {
            @Override
            public void run() {
                mPet.setBoundaryPlane(boundary);
                mPet.goToScreen();
            }
        }, 1500);
    }

    private void addPetObjectsToPlane(GVRPlane plane) {

        ScalableObjectManager.INSTANCE.addScalableObject(
                new Bed(mContext, mMixedReality, plane.getCenterPose()),
                new Bowl(mContext, mMixedReality, plane.getCenterPose()),
                new Hydrant(mContext, mMixedReality, plane.getCenterPose()),
                new Toy(mContext, mMixedReality, plane.getCenterPose())
        );

        ScalableObjectManager.INSTANCE.setAutoScaleObjectsFrom(mPet);

        for (BaseScalableObject scalableObject : ScalableObjectManager.INSTANCE.getScalableObjects()) {
            mScene.addSceneObject(scalableObject.getAnchor());
        }




    }

    @Override
    public void onStep() {
        super.onStep();
        if (ballThrowHandler.canBeReseted()) {
            ballThrowHandler.reset();
        }

        HudOrientation();

    }

    @Subscribe
    public void onCollisionDetected(CollisionEvent event) {
        // TODO: Handle here the collision event according to its type
    }

    private IAnchorEventsListener mAnchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {

        }
    };

    private void disableCursor() {
        GVRInputManager inputManager = mContext.getInputManager();
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                newController.setCursor(null);
            }
        });
    }

    public class TouchEvents implements ITouchEvents {
        @Override
        public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onExit(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onInside(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onMotionOutside(GVRPicker picker, MotionEvent motionEvent) {

        }
    }

    private void HudOrientation() {
        float rotationZAxis = mScene.getMainCameraRig().getHeadTransform().getRotationRoll();
        GVRTransform cameraTransform = mScene.getMainCameraRig().getHeadTransform();

        if (rotationZAxis < 3 && rotationZAxis > -60 || rotationZAxis > -80 && rotationZAxis < 175) {
            mHud.getTransform().setPosition(cameraTransform.getPositionX() + 2.8f, cameraTransform.getPositionY(), cameraTransform.getPositionZ() - 5);
            mHud.getTransform().setRotation(cameraTransform.getRotationW(), cameraTransform.getRotationX(), cameraTransform.getRotationY(), cameraTransform.getRotationZ());
            Log.d(TAG, "Landscape " + " Z: " + rotationZAxis);
        } else {
            mHud.getTransform().setPosition(cameraTransform.getPositionX() + 1.4f, cameraTransform.getPositionY() + 1.4f, cameraTransform.getPositionZ() - 5);
            mHud.getTransform().setRotation(cameraTransform.getRotationW(), cameraTransform.getRotationX(), cameraTransform.getRotationY(), -cameraTransform.getRotationZ());
            Log.d(TAG, "Portrait " + " Z: " + rotationZAxis);
        }
    }

    private class HudEventHandler implements OnHudItemClicked {
        private OnHudItemClicked mOnHudItemClicked;

        @Override
        public void onBallClicked() {
            //ballThrowHandler.enable();
        }

        @Override
        public void onShareAnchorClicked() {

        }

        @Override
        public void onEditModeClicked() {

        }

        @Override
        public void onCameraClicked() {

        }
    }

}

