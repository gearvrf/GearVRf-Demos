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

import android.annotation.SuppressLint;
import android.support.v4.util.Preconditions;
import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.arpet.Character.PetAction;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.animation.PetAnimationHelper;
import org.gearvrf.arpet.events.CollisionEvent;
import org.gearvrf.arpet.gesture.ScalableObjectManager;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.movement.targetwrapper.BallWrapper;
import org.gearvrf.arpet.petobjects.AnchoredScalableObject;
import org.gearvrf.arpet.petobjects.Bed;
import org.gearvrf.arpet.petobjects.Bowl;
import org.gearvrf.arpet.petobjects.Hydrant;
import org.gearvrf.arpet.petobjects.Toy;
import org.gearvrf.arpet.util.LoadModelHelper;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class PetMain extends GVRMain {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private GVRContext mContext;
    private PetActivity.PetContext mPetContext;
    private GVRMixedReality mMixedReality;

    private BallThrowHandler mBallThrowHandler;
    private PlaneHandler planeHandler;

    private Character mPet;
    private GVRModelSceneObject petSceneObject;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;


    public PetMain(PetActivity.PetContext petContext) {
        mPetContext = petContext;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        mMixedReality = new GVRMixedReality(gvrContext, true);
        mMixedReality.resume();

        GVRWorld world = new GVRWorld(gvrContext);
        world.setGravity(0f, -50f, 0f);
        mScene.getRoot().attachComponent(world);

        mBallThrowHandler = BallThrowHandler.getInstance(gvrContext, mMixedReality);
        mBallThrowHandler.enable();

        planeHandler = new PlaneHandler(gvrContext, mPetContext, mMixedReality);
        mMixedReality.registerPlaneListener(planeHandler);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();

        petSceneObject = LoadModelHelper.loadModelSceneObject(gvrContext, LoadModelHelper.PET_MODEL_PATH);

//        disableCursor();
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

    private boolean isCloudAnchorApiKeySet() {
        return ContextUtils.isMetaDataSet(getGVRContext().getContext(),
                ApiConstants.GOOGLE_CLOUD_ANCHOR_KEY_NAME);
    }

    @SuppressLint("RestrictedApi")
    private void testAnchorSharing(GVRAnchor anchor) {

        if (!isCloudAnchorApiKeySet()) {
            Log.d(TAG, " Cloud Anchor API key is not set.");
            return;
        }

        Log.d(TAG, "hosting anchor...");
        mMixedReality.hostAnchor(
                anchor,
                (hostedAnchor) -> {

                    Preconditions.checkStringNotEmpty(hostedAnchor.getCloudAnchorId(),
                            TAG + ": Error hosting anchor.");

                    Log.d(TAG, "anchor hosted successful! Anchor ID = \""
                            + hostedAnchor.getCloudAnchorId() + "\"");

                    Log.d(TAG, "resolving anchor...");
                    mMixedReality.resolveCloudAnchor(
                            hostedAnchor.getCloudAnchorId(),
                            (resolvedAnchor) -> {

                                Preconditions.checkStringNotEmpty(resolvedAnchor.getCloudAnchorId(),
                                        TAG + ": Error resolving anchor.");

                                Log.d(TAG, "anchor resolved successful! Anchor ID = \""
                                        + resolvedAnchor.getCloudAnchorId() + "\"");
                            }
                    );
                }
        );
    }

    @Subscribe
    public void onPlaneDetected(final GVRPlane plane) {

        if (mPet == null) {
            mPet = new Character(mContext, mMixedReality, plane.getCenterPose(),
                    new PetActionListener(), new PetAnimationHelper(mContext));
            mPet.lookAt(mBallThrowHandler.getBallWrapper());
            mPet.set3DModel(petSceneObject);
            mPet.setBoundaryPlane(plane);
            mScene.addSceneObject(mPet.getAnchor());
        }

        // Host pet anchor
        testAnchorSharing(mPet.getAnchor());

        if (mCurrentMode instanceof EditMode) {
            Log.e(TAG, "Wrong state at first detection!");
        }

        if (mCurrentMode == null) {
            mCurrentMode = new HudMode(mContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
        //addPetObjectsToPlane(plane);
        //movePetToScreen();
        //movePetToBed();
    }

    private void setEditModeEnabled(boolean enabled) {
        if (mPet != null) {
            mPet.setRotationEnabled(enabled);
            mPet.setScaleEnabled(enabled);
            mPet.setDraggingEnabled(enabled);
        }
    }

    private void movePetToScreen() {
        mPetContext.runDelayedOnPetThread(new Runnable() {
            @Override
            public void run() {
                mPet.goToScreen();
            }
        }, 1500);
    }

    private void movePetToBed() {
        mPetContext.runDelayedOnPetThread(new Runnable() {
            @Override
            public void run() {
                mPet.goToBed();
            }
        }, 1500);
    }

    private void addPetObjectsToPlane(GVRPlane plane) {

        AnchoredScalableObject[] objects = {
                new Bed(mContext, mMixedReality, plane.getCenterPose()),
                new Bowl(mContext, mMixedReality, plane.getCenterPose()),
                new Hydrant(mContext, mMixedReality, plane.getCenterPose()),
                new Toy(mContext, mMixedReality, plane.getCenterPose())
        };

        // Anchor objects to the plane
        for (AnchoredScalableObject scalableObject : objects) {
            mScene.addSceneObject(scalableObject.getAnchor());
        }

        // Manages scalable objects
        ScalableObjectManager.INSTANCE.addScalableObject(objects);

        // Enables objects resizing on pet scale
        ScalableObjectManager.INSTANCE.setAutoScaleObjectsFrom(mPet);
    }

    @Override
    public void onStep() {
        super.onStep();
        mBallThrowHandler.tryReset();

        if (mCurrentMode != null) {
            mCurrentMode.handleOrientation();
        }
    }

    @Subscribe
    public void onCollisionDetected(CollisionEvent event) {
//        if (event.getType() == CollisionEvent.Type.ENTER) {
//            mBallThrowHandler.setResetOnTouchEnabled(false);
//            mPet.goToBall();
//        }
    }

    @Subscribe
    public void onBallThrown(BallWrapper ballWrapper) {
//        mBallThrowHandler.setResetOnTouchEnabled(false);
        mPet.goToBall(ballWrapper);
    }

    private class PetActionListener implements OnPetActionListener {

        @Override
        public void onActionStart(int action) {
            Log.d(TAG, "onActionStart: " + action);
        }

        @Override
        public void onActionEnd(int action) {
            Log.d(TAG, "onActionEnd: " + action);
            if (action == PetAction.TO_BALL) {
                mBallThrowHandler.disable();
                mPet.goToScreen();
            } else if (action == PetAction.TO_SCREEN) {
                mBallThrowHandler.enable();
                mBallThrowHandler.reset();
                mPet.lookAt(mBallThrowHandler.getBallWrapper());
                mBallThrowHandler.setResetOnTouchEnabled(true);
            }
        }
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

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onPlayBall() {

        }

        @Override
        public void onShareAnchor() {

        }

        @Override
        public void onEditMode() {
            if (mCurrentMode instanceof EditMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new EditMode(mContext, mHandlerBackToHud);
            mCurrentMode.enter();
            setEditModeEnabled(true);
        }

        @Override
        public void onScreenshot() {

        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            mCurrentMode.exit();
            mCurrentMode = new HudMode(mContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
    }

}

