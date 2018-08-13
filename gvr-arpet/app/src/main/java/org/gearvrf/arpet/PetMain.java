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

import android.view.MotionEvent;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.arpet.events.CollisionEvent;
import org.gearvrf.arpet.gesture.scale.BaseScalableObject;
import org.gearvrf.arpet.gesture.scale.ScalableObjectManager;
import org.gearvrf.arpet.movement.lookatobject.ObjectToLookAt;
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
        ballThrowHandler.enable();

        planeHandler = new PlaneHandler(gvrContext, mPetContext, mMixedReality);
        mMixedReality.registerPlaneListener(planeHandler);

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
        mPet.lookAt(new ObjectToLookAt(ballThrowHandler.getBall()));

        // addPetObjectsToPlane(plane);
        // setEditModeEnabled(true);
        // movePetToScreen(plane, 1500);
    }

    private void setEditModeEnabled(boolean enabled) {
        if (mPet != null) {
            mPet.setRotationEnabled(enabled);
            mPet.setScaleEnabled(enabled);
            mPet.setDraggingEnabled(enabled);
        }
    }

    private void movePetToScreen(final GVRPlane boundary, int delayToStart) {
        mPetContext.runDelayedOnPetThread(new Runnable() {
            @Override
            public void run() {
                mPet.setBoundaryPlane(boundary);
                mPet.goToScreen();
            }
        }, delayToStart);
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

}

