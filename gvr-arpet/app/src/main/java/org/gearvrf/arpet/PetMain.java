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
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.animation.PetAnimationHelper;
import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.mode.ShareAnchorMode;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.movement.OnPetActionListener;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.movement.targetwrapper.BallWrapper;
import org.gearvrf.arpet.sharing.AppConnectionManager;
import org.gearvrf.arpet.sharing.UiMessage;
import org.gearvrf.arpet.sharing.UiMessageHandler;
import org.gearvrf.arpet.sharing.UiMessageType;
import org.gearvrf.arpet.util.ContextUtils;
import org.gearvrf.arpet.util.LoadModelHelper;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;


public class PetMain extends GVRMain {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private GVRContext mContext;
    private PetActivity.PetContext mPetContext;
    private GVRMixedReality mMixedReality;

    private BallThrowHandler mBallThrowHandler;
    private PlaneHandler planeHandler;

    private Character mPet;


    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;

    private AppConnectionManager mConnectionManager;
    private CloudAnchorManager mCloudAnchorManager;

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


        mCloudAnchorManager = new CloudAnchorManager();

        //disableCursor();
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

    @Subscribe
    public void onPlaneDetected(final GVRPlane plane) {
        if (mPet == null) {
            createPet(plane);
        }

        // Host pet anchor
        if (isCloudAnchorApiKeySet()) {
            mCloudAnchorManager.hostAnchor(mMixedReality, mPet);
        }

        if (mCurrentMode instanceof EditMode) {
            Log.e(TAG, "Wrong state at first detection!");
        }

        if (mCurrentMode == null) {
            mCurrentMode = new HudMode(mContext,mHandlerModeChange);
            mCurrentMode.enter();
        }
        //addPetObjectsToPlane(plane);
        //movePetToScreen();
        //movePetToBed();
    }

    private void createPet(final GVRPlane plane) {
        mPet = new Character(mContext, mMixedReality, plane.getCenterPose(),
                new PetAnimationHelper(mContext));

        initPetActions(mPet);

        mPet.setBoundaryPlane(plane);
        mScene.addSceneObject(mPet.getAnchor());
        mPet.setCurrentAction(PetActions.TO_CAMERA.ID);

        // Enable action animations
        mPet.enableAction();
    }

    private void initPetActions(Character pet) {
        // TODO: move this to the Character class
        GVRTransform camTrans = mContext.getMainScene().getMainCameraRig().getTransform();

        pet.addAction(new PetActions.IDLE(pet, camTrans));

        pet.addAction(new PetActions.TO_BALL(pet, mBallThrowHandler.getBall().getTransform(),
                new OnPetActionListener() {
                    @Override
                    public void onActionEnd(IPetAction action) {
                        mPet.setCurrentAction(PetActions.TO_CAMERA.ID);
                        mPet.getChildByIndex(0).setEnable(false);
                        mBallThrowHandler.disable();
                        // TODO: Pet take the ball
                    }
                }));

        pet.addAction(new PetActions.TO_CAMERA(pet, camTrans,
                new OnPetActionListener() {
                    @Override
                    public void onActionEnd(IPetAction action) {
                        pet.setCurrentAction(PetActions.IDLE.ID);
                        // TODO: Improve this Ball handler api
                        mBallThrowHandler.enable();
                        mBallThrowHandler.reset();
                        mPet.getChildByIndex(0).setEnable(true);
                    }
                }));
    }

    private void setEditModeEnabled(boolean enabled) {
        if (mPet != null) {
            mPet.setRotationEnabled(enabled);
            mPet.setScaleEnabled(enabled);
            mPet.setDraggingEnabled(enabled);
        }
    }

    @Override
    public void onStep() {
        super.onStep();
        if (mBallThrowHandler.canBeReseted()) {
            mBallThrowHandler.reset();
        }

        if (mCurrentMode != null) {
            mCurrentMode.handleOrientation();
        }
    }

    @Subscribe
    public void onBallThrown(BallWrapper ballWrapper) {
        mPet.setCurrentAction(PetActions.TO_BALL.ID);
    }

    private IAnchorEventsListener mAnchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {

        }
    };

    public class HandlerModeChange implements OnModeChange {

        @Override
        public void onPlayBall() {

        }

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof ShareAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ShareAnchorMode(mContext);
            mCurrentMode.enter();
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

    public void onActivityResult(int requestCode, int resultCode) {
        mConnectionManager.onActivityResult(requestCode, resultCode);
    }

    @SuppressLint("HandlerLeak")
    private class AppMessageHandler extends Handler implements UiMessageHandler {

        @Override
        public void handleMessage(UiMessage message) {
            android.os.Message m = obtainMessage(message.getType());
            Bundle b = new Bundle();
            b.putSerializable("data", message.getData());
            m.setData(b);
            sendMessage(m);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            String log = String.format(
                    "handleMessage: {what: %s, data: %s}",
                    msg.what, msg.getData().getSerializable("data"));
            Log.d(TAG, log);

            @UiMessageType
            int messageType = msg.what;

            switch (messageType) {

                case UiMessageType.CONNECTION_ESTABLISHED:
                    toast("Connections established: " + mConnectionManager.getTotalConnected());
                    Message message = AppConnectionManager.newMessage(
                            getGVRContext().getContext().getString(R.string.lorem_ipsum));
                    mConnectionManager.sendMessage(message);
                    break;
                case UiMessageType.CONNECTION_NOT_FOUND:
                    toast("No connection found");
                    break;
                case UiMessageType.CONNECTION_LOST:
                    toast("No active connection");
                    break;
                case UiMessageType.CONNECTION_LISTENER_STARTED:
                    toast("Ready to accept connections");
                    new Handler().postDelayed(
                            // Stop listening after a few seconds
                            () -> mConnectionManager.stopConnectionListener(),
                            30000);
                    break;
                case UiMessageType.ERROR_BLUETOOTH_NOT_ENABLED:
                    toast("Bluetooth is disabled");
                    break;
                case UiMessageType.ERROR_DEVICE_NOT_DISCOVERABLE:
                    toast("Device is not visible to other devices");
                    break;
                case UiMessageType.MESSAGE_RECEIVED:
                    Serializable data = msg.getData().getSerializable("data");
                    toast("Message received from remote: " + data);
                    handleReceivedMessage(data);
                    break;
            }
        }

        private void toast(String text) {
            Toast.makeText(getGVRContext().getActivity(), text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAfterInit() {
        super.onAfterInit();
        new Handler().postDelayed(() -> {
            mConnectionManager = new AppConnectionManager(mContext.getActivity(), new AppMessageHandler());
            //mConnectionManager.inviteUsers();
            //mConnectionManager.acceptInvitation();
        }, 1000);
    }

    private void handleReceivedMessage(Serializable receivedData) {
        Message message = (Message) receivedData;
        Device device = message.getDevice();
        Serializable messageData = message.getData();
    }
}

