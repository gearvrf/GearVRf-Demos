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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.ILoadEvents;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.mode.ShareAnchorMode;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRGazeCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class PetMain extends DisableNativeSplashScreen {
    private static final String TAG = "GVR_ARPET";

    private PetContext mPetContext;

    private PlaneHandler mPlaneHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CharacterController mPet = null;
    private GVRAnchor mWorldCenterAnchor = null;

    private GVRCursorController mCursorController = null;

    private CurrentSplashScreen mCurrentSplashScreen;
    private SharedMixedReality mSharedMixedReality;

    public PetMain(PetContext petContext) {
        mPetContext = petContext;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        mCurrentSplashScreen = new CurrentSplashScreen(gvrContext);
        mCurrentSplashScreen.onShow();

        mPetContext.init(gvrContext);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();

        mPlaneHandler = new PlaneHandler(mPetContext);

        configTouchScreen();

        mSharedMixedReality = (SharedMixedReality) mPetContext.getMixedReality();

        mPet = new CharacterController(mPetContext);
        mPet.load(new ILoadEvents() {
            @Override
            public void onSuccess() {
                // Will wet pet's scene as the main scene
                mCurrentSplashScreen.onHide(mPetContext.getMainScene());
            }

            @Override
            public void onFailure() {
                mPetContext.getActivity().finish();
            }
        });
    }

    private void configTouchScreen() {
        mCursorController = null;
        GVRInputManager inputManager = mPetContext.getGVRContext().getInputManager();
        inputManager.selectController((newController, oldController) -> {
            if (newController instanceof GVRGazeCursorController) {
                ((GVRGazeCursorController) newController).setEnableTouchScreen(true);
                newController.setCursor(null);
            }

            if (mCursorController != null) {
                mCursorController.removePickEventListener(mTouchEventsHandler);
            }
            newController.addPickEventListener(mTouchEventsHandler);
            mCursorController = newController;
        });
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

    @Override
    public boolean onBackPress() {
        if (mCurrentMode instanceof ShareAnchorMode) {
            getGVRContext().runOnGlThread(() -> mHandlerBackToHud.OnBackToHud());
        }

        if (mCurrentMode instanceof HudMode || mCurrentMode == null) {
            getGVRContext().getActivity().finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
        return true;
    }

    @Override
    public void onStep() {
        super.onStep();
        if (mCurrentMode != null) {
            mCurrentMode.handleOrientation();
        }
    }

    @Subscribe
    public void handleBallEvent(BallThrowHandlerEvent event) {
        if (event.getPerformedAction().equals(BallThrowHandlerEvent.THROWN)) {
            mPet.setCurrentAction(PetActions.TO_BALL.ID);
        } else if (event.getPerformedAction().equals(BallThrowHandlerEvent.RESET)) {
        }
    }

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onPlayBall() {
            mPet.playBall();
            mPet.setCurrentAction(PetActions.IDLE.ID);
        }

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof ShareAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            // Get the model matrix from the actual Pet's position and create an anchor to be
            // hosted by Cloud Anchor service
            float[] anchorMatrix = mPet.getView().getAnchor().getTransform().getModelMatrix();
            if (mWorldCenterAnchor != null) {
                mSharedMixedReality.removeAnchor(mWorldCenterAnchor);
            }
            mWorldCenterAnchor = mSharedMixedReality.createAnchor(anchorMatrix);
            mCurrentMode = new ShareAnchorMode(mPetContext, mWorldCenterAnchor, mHandlerBackToHud);
            mCurrentMode.enter();
            mPet.stopBall();
            mPet.setCurrentAction(PetActions.AT_SHARE.ID);
        }

        @Override
        public void onEditMode() {
            if (mCurrentMode instanceof EditMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new EditMode(mPetContext, mHandlerBackToHud, mPet);
            mCurrentMode.enter();
            ((EditMode) mCurrentMode).onEnableGesture(mCursorController);
            mPet.stopBall();
            mPet.setCurrentAction(PetActions.AT_EDIT.ID);

            // Edit mode will handle picker events
            mCursorController.removePickEventListener(mTouchEventsHandler);
        }

        @Override
        public void onScreenshot() {

        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            if (mCurrentMode instanceof EditMode) {
                mCursorController.addPickEventListener(mTouchEventsHandler);
            }

            mPetContext.registerPlaneListener(mPlaneHandler);
            mCurrentMode.exit();
            mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
            mCurrentMode.enter();

            if (mPetContext.getMode() != SharedMixedReality.OFF) {
                mPet.playBall();
            }
            mPet.setCurrentAction(PetActions.IDLE.ID);
        }
    }

    ITouchEvents mTouchEventsHandler = new ITouchEvents() {
        @Override
        public void onEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onTouchEnd(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
            Log.d(TAG, "onTouchEnd " + gvrSceneObject.getName());

            // TODO: Improve this if
            if (gvrSceneObject != null && gvrSceneObject.getParent() instanceof GVRPlane) {
                final float[] modelMtx = gvrSceneObject.getParent().getTransform().getModelMatrix();
                final float[] hitPos = gvrPickedObject.hitLocation;

                if (!mPet.isRunning()) {
                    mPet.setPlane((GVRPlane)gvrSceneObject.getParent());
                    mPet.setAnchor(mPetContext.getMixedReality().createAnchor(modelMtx));
                    mPet.enter();
                    mPet.setInitialScale();
                    mPet.enableActions();

                    if (mCurrentMode == null) {
                        mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
                        mCurrentMode.enter();
                    }
                }

                mPet.goToTap(hitPos[0], hitPos[1], hitPos[2]);
            }
        }

        @Override
        public void onInside(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {

        }
    };
}

