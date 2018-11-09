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
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.mainview.IConnectionFinishedView;
import org.gearvrf.arpet.mainview.IExitView;
import org.gearvrf.arpet.mainview.MainViewController;
import org.gearvrf.arpet.manager.connection.event.PetConnectionEvent;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.ILoadEvents;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.mode.photo.ScreenshotMode;
import org.gearvrf.arpet.mode.sharing.ShareAnchorMode;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.util.EventBusUtils;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRGazeCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;
import org.greenrobot.eventbus.Subscribe;

import java.util.EnumSet;

import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;


public class PetMain extends DisableNativeSplashScreen {
    private static final String TAG = "GVR_ARPET";

    private PetContext mPetContext;

    private PlaneHandler mPlaneHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CharacterController mPet = null;

    private GVRCursorController mCursorController = null;

    private CurrentSplashScreen mCurrentSplashScreen;
    private SharedMixedReality mSharedMixedReality;

    private MainViewController mMainViewController;

    public PetMain(PetContext petContext) {
        mPetContext = petContext;
        EventBusUtils.register(this);
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
        mPetContext.setPlaneListener(mPlaneHandler);

        configTouchScreen();

        mSharedMixedReality = (SharedMixedReality) mPetContext.getMixedReality();

        mPet = new CharacterController(mPetContext);
        mPet.load(new ILoadEvents() {
            @Override
            public void onSuccess() {
                // Will wet pet's scene as the main scene
                mCurrentSplashScreen.onHide(mPetContext.getMainScene());
                // Start detecting planes
                mPetContext.startDetectingPlanes();
                // Set pet controller in pet context
                mPetContext.setPetController(mPet);
            }

            @Override
            public void onFailure() {
                mPetContext.getActivity().finish();
            }
        });
    }

    private void configTouchScreen() {
        mCursorController = null;
        final int cursorDepth = 5;
        GVRInputManager inputManager = mPetContext.getGVRContext().getInputManager();
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS,
                GVRPicker.EventOptions.SEND_TO_HIT_OBJECT);

        inputManager.selectController((newController, oldController) -> {
            if (newController instanceof GVRGazeCursorController) {
                ((GVRGazeCursorController) newController).setEnableTouchScreen(true);
                newController.setCursor(null);
            }

            if (mCursorController != null) {
                mCursorController.removePickEventListener(mTouchEventsHandler);
            }
            newController.addPickEventListener(mTouchEventsHandler);
            newController.setCursorDepth(cursorDepth);
            newController.getPicker().setPickClosest(false);
            newController.getPicker().setEventOptions(eventOptions);
            mCursorController = newController;
        });
    }

    public void resume() {
        EventBusUtils.register(this);
    }

    public void pause() {
        EventBusUtils.unregister(this);
    }

    private void showViewExit() {

        mMainViewController = new MainViewController(mPetContext);
        mMainViewController.onShow(mPetContext.getMainScene());
        IExitView iExitView = mMainViewController.makeView(IExitView.class);

        iExitView.setOnCancelClickListener(view -> {
            mMainViewController.onHide(mPetContext.getMainScene());
            mMainViewController = null;
        });
        iExitView.setOnConfirmClickListener(view -> {
            getGVRContext().getActivity().finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });

        iExitView.show();
    }

    private void showViewConnectionFinished() {

        mMainViewController = new MainViewController(mPetContext);
        mMainViewController.onShow(mPetContext.getMainScene());

        IConnectionFinishedView iFinishedView =
                mMainViewController.makeView(IConnectionFinishedView.class);

        iFinishedView.setOkClickListener(view -> {
            mMainViewController.onHide(mPetContext.getMainScene());
            mMainViewController = null;
        });

        String text = getGVRContext().getActivity().getString(
                mSharedMixedReality.getMode() == PetConstants.SHARE_MODE_GUEST
                        ? R.string.view_host_disconnected
                        : R.string.view_guests_disconnected);
        iFinishedView.setStatusText(text);
        iFinishedView.show();
    }

    @Override
    public boolean onBackPress() {
        if (mCurrentMode instanceof ShareAnchorMode || mCurrentMode instanceof EditMode) {
            getGVRContext().runOnGlThread(() -> mHandlerBackToHud.OnBackToHud());
        }

        if (mCurrentMode instanceof HudMode || mCurrentMode == null) {
            getGVRContext().runOnGlThread(this::showViewExit);
        }
        return true;
    }

    @Subscribe
    public void handleConnectionEvent(PetConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            if (mCurrentMode instanceof HudMode) {
                getGVRContext().runOnGlThread(this::showViewConnectionFinished);
                mSharedMixedReality.stopSharing();
                mPet.stopBall();
            }
        }
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
            if (mPet.isPlaying()) {
                mPet.stopBall();
            } else {
                mPet.playBall();
            }
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

            mCurrentMode = new ShareAnchorMode(mPetContext, mHandlerBackToHud);
            mCurrentMode.enter();
            mPet.stopBall();
            mPet.setCurrentAction(PetActions.IDLE.ID);
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
            if (mCurrentMode instanceof ScreenshotMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ScreenshotMode(mPetContext, mHandlerBackToHud);
            mCurrentMode.enter();
        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            if (mCurrentMode instanceof EditMode) {
                mCursorController.addPickEventListener(mTouchEventsHandler);
            }

            mCurrentMode.exit();
            mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
            mCurrentMode.enter();

            if (mPetContext.getMode() != PetConstants.SHARE_MODE_NONE) {
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

            if (mMainViewController != null && mMainViewController.isEnabled()) {
                return;
            }

            // TODO: Improve this if
            if (gvrSceneObject != null && gvrSceneObject.getParent() instanceof GVRPlane) {
                final float[] modelMtx = gvrSceneObject.getTransform().getModelMatrix();


                if (!mPet.isRunning()) {
                    mPet.setPlane(gvrSceneObject);
                    mPet.getView().getTransform().setPosition(modelMtx[12], modelMtx[13], modelMtx[14]);
                    mPet.enter();
                    mPet.setInitialScale();
                    mPet.enableActions();

                    if (mCurrentMode == null) {
                        mCurrentMode = new HudMode(mPetContext, mPet, mHandlerModeChange);
                        mCurrentMode.enter();
                    } else if (mCurrentMode instanceof HudMode) {
                        mCurrentMode.view().show(mPetContext.getMainScene());
                    }

                    mPlaneHandler.setSelectedPlane((GVRPlane) gvrSceneObject.getParent());
                }

                if (gvrSceneObject == mPet.getPlane() && mCurrentMode instanceof HudMode) {
                    final float[] hitPos = gvrPickedObject.hitLocation;
                    mPet.goToTap(hitPos[0], hitPos[1], hitPos[2]);
                }
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

