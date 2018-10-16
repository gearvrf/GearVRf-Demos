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

import android.os.Handler;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.mode.ShareAnchorMode;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRGazeCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.physics.GVRWorld;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


public class PetMain extends DisableNativeSplashScreen {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private PetContext mPetContext;

    private PlaneHandler mPlaneHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private CharacterController mPet = null;

    private ArrayList<AnchoredObject> mAnchoredObjects;
    private GVRCursorController mCursorController = null;

    private CurrentSplashScreen mCurrentSplashScreen;

    public PetMain(PetContext petContext) {
        mPetContext = petContext;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        mPetContext.init(gvrContext);
        mScene = gvrContext.getMainScene();

        mCurrentSplashScreen = new CurrentSplashScreen(mPetContext);
        splashScreenBehavior();

        GVRWorld world = new GVRWorld(gvrContext);
        world.setGravity(0f, -200f, 0f);
        mScene.getRoot().attachComponent(world);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();

        mPlaneHandler = new PlaneHandler(mPetContext);

        mPet = new CharacterController(mPetContext);

        // Add in this array all objects that will be hosted by Cloud Anchor
        mAnchoredObjects = new ArrayList<>();
        mAnchoredObjects.add((AnchoredObject) mPet.view());

        configTouchScreen();
    }

    private void splashScreenBehavior() {
        mCurrentSplashScreen.onShow();
        new Handler().postDelayed(() -> mCurrentSplashScreen.onHide(), 3000);
    }

    private void configTouchScreen() {
        GVRInputManager inputManager = mPetContext.getGVRContext().getInputManager();
        inputManager.selectController((newController, oldController) -> {
            if (newController instanceof GVRGazeCursorController) {
                ((GVRGazeCursorController) newController).setEnableTouchScreen(true);
                newController.setCursor(null);
            }
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

    @Subscribe
    public void onPlaneDetected(final GVRPlane plane) {
        mPet.setPlane(plane);
        mPet.enter();
        mPet.setInitialScale();

        ((SharedMixedReality) mPetContext.getMixedReality())
                .registerSharedObject(plane.getSceneObject(), ArPetObjectType.PLANE);

        if (mCurrentMode instanceof EditMode) {
            Log.e(TAG, "Wrong state at first detection!");
        }

        if (mCurrentMode == null) {
            mCurrentMode = new HudMode(mPetContext, mHandlerModeChange);
            mCurrentMode.enter();
            mPet.stopBall();
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
        }
    }

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onPlayBall() {
            mPet.playBall();
        }

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof ShareAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ShareAnchorMode(mPetContext, mAnchoredObjects, mHandlerBackToHud);
            mCurrentMode.enter();
            mPet.stopBall();
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
        }

        @Override
        public void onScreenshot() {

        }
    }

    public class HandlerBackToHud implements OnBackToHudModeListener {

        @Override
        public void OnBackToHud() {
            mPetContext.registerPlaneListener(mPlaneHandler);
            mCurrentMode.exit();
            mCurrentMode = new HudMode(mPetContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
    }
}

