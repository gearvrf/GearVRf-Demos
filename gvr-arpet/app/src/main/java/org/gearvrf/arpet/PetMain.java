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
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.character.CharacterView;
import org.gearvrf.arpet.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.manager.connection.bluetooth.LocalBluetoothDevice;
import org.gearvrf.arpet.mode.EditMode;
import org.gearvrf.arpet.mode.HudMode;
import org.gearvrf.arpet.mode.IPetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;
import org.gearvrf.arpet.mode.OnGuestOrHostListener;
import org.gearvrf.arpet.mode.OnModeChange;
import org.gearvrf.arpet.mode.ShareAnchorMode;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.movement.OnPetActionListener;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.movement.targetwrapper.BallWrapper;
import org.gearvrf.arpet.sharing.AppConnectionManager;
import org.gearvrf.arpet.sharing.IAppConnectionManager;
import org.gearvrf.arpet.sharing.UiMessage;
import org.gearvrf.arpet.sharing.UiMessageHandler;
import org.gearvrf.arpet.sharing.UiMessageType;
import org.gearvrf.arpet.util.ContextUtils;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.physics.GVRWorld;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;


public class PetMain extends GVRMain {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private PetContext mPetContext;

    private PlaneHandler planeHandler;

    private IPetMode mCurrentMode;
    private HandlerModeChange mHandlerModeChange;
    private HandlerBackToHud mHandlerBackToHud;
    private HandlerGuestOrHost handlerGuestOrHost;

    private CloudAnchorManager mCloudAnchorManager;
    private IAppConnectionManager mConnectionManager;

    private CharacterController mPet = null;

    public PetMain(PetContext petContext) {
        mPetContext = petContext;
        EventBus.getDefault().register(this);
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        mPetContext.init(gvrContext);

        mScene = gvrContext.getMainScene();

        GVRWorld world = new GVRWorld(gvrContext);
        world.setGravity(0f, -50f, 0f);
        mScene.getRoot().attachComponent(world);

        mHandlerModeChange = new HandlerModeChange();
        mHandlerBackToHud = new HandlerBackToHud();
        handlerGuestOrHost = new HandlerGuestOrHost();

        mCloudAnchorManager = new CloudAnchorManager();

        planeHandler = new PlaneHandler(mPetContext);
        mPetContext.getMixedReality().registerPlaneListener(planeHandler);

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
            mCloudAnchorManager.hostAnchor(mPetContext, (AnchoredObject)mPet.view());
        }

        if (mCurrentMode instanceof EditMode) {
            Log.e(TAG, "Wrong state at first detection!");
        }

        if (mCurrentMode == null) {
            mCurrentMode = new HudMode(mPetContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
        //addPetObjectsToPlane(plane);
        //movePetToScreen();
        //movePetToBed();
    }

    private void createPet(final GVRPlane plane) {
        mPet = new CharacterController(mPetContext, plane);
        mPet.enter();
    }

    private void setEditModeEnabled(boolean enabled) {
        if (mPet != null) {
            /* FIXME: Should be a state of the pet
            mPet.setRotationEnabled(enabled);
            mPet.setScaleEnabled(enabled);
            mPet.setDraggingEnabled(enabled);*/
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
            // FIXME: Create this state to the PET
        }

        @Override
        public void onShareAnchor() {
            if (mCurrentMode instanceof ShareAnchorMode) {
                return;
            }

            if (mCurrentMode != null) {
                mCurrentMode.exit();
            }

            mCurrentMode = new ShareAnchorMode(mPetContext, handlerGuestOrHost);
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

            mCurrentMode = new EditMode(mPetContext, mHandlerBackToHud);
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
            mCurrentMode = new HudMode(mPetContext, mHandlerModeChange);
            mCurrentMode.enter();
        }
    }

    public class HandlerGuestOrHost implements OnGuestOrHostListener {

        @Override
        public void OnHost() {
            Log.d(TAG, "Search devices");

        }

        @Override
        public void OnGuest() {

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
                    handleConnectionEstablished();
                    break;
                case UiMessageType.CONNECTION_NOT_FOUND:
                    showToast("No connection found");
                    break;
                case UiMessageType.CONNECTION_LOST:
                    showToast("No active connection");
                    break;
                case UiMessageType.CONNECTION_LISTENER_STARTED:
                    showToast("Ready to accept connections");
                    new Handler().postDelayed(
                            // Stop listening after a few seconds
                            () -> mConnectionManager.stopUsersInvitation(),
                            30000);
                    break;
                case UiMessageType.ERROR_BLUETOOTH_NOT_ENABLED:
                    showToast("Bluetooth is disabled");
                    break;
                case UiMessageType.ERROR_DEVICE_NOT_DISCOVERABLE:
                    showToast("Device is not visible to other devices");
                    break;
                case UiMessageType.MESSAGE_RECEIVED:
                    Serializable data = msg.getData().getSerializable("data");
                    handleReceivedMessage(data);
                    break;
            }
        }
    }

    private void handleConnectionEstablished() {
        switch (mConnectionManager.getConnectionMode()) {
            case ConnectionMode.CLIENT: {
                Message message = AppConnectionManager.newMessage("Hello I'm the client "
                        + LocalBluetoothDevice.getDefault().getName());
                mConnectionManager.sendMessage(message);
                break;
            }
            case ConnectionMode.SERVER: {
                Message message = AppConnectionManager.newMessage("Hello I'm the server "
                        + LocalBluetoothDevice.getDefault().getName());
                mConnectionManager.sendMessage(message);
                break;
            }
            case ConnectionMode.NONE:
            default:
                break;
        }
    }

    @Override
    public void onAfterInit() {
        super.onAfterInit();
        new Handler().postDelayed(() -> {
            mConnectionManager = AppConnectionManager.getInstance(mPetContext, new AppMessageHandler());
            //mConnectionManager.startUsersInvitation();
            //mConnectionManager.acceptInvitation();
        }, 1000);
    }

    private void handleReceivedMessage(Serializable receivedMessage) {
        Message message = (Message) receivedMessage;
        Device device = message.getDevice();
        Object messageData = message.getData();
        showToast(messageData.toString());
    }

    private void showToast(String text) {
        Toast.makeText(getGVRContext().getActivity(), text, Toast.LENGTH_LONG).show();
    }
}

