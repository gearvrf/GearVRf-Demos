/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.mode;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.PlaneHandler;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.common.Task;
import org.gearvrf.arpet.common.TaskException;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorException;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.manager.cloud.anchor.OnCloudAnchorManagerListener;
import org.gearvrf.arpet.manager.cloud.anchor.ResolvedCloudAnchor;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventHandler;
import org.gearvrf.arpet.manager.connection.PetConnectionEventType;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageCallback;
import org.gearvrf.arpet.service.MessageException;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.arpet.service.data.SharedCamera;
import org.gearvrf.arpet.service.data.SharedObject;
import org.gearvrf.arpet.service.data.SharedPlane;
import org.gearvrf.arpet.service.data.SharedScene;
import org.gearvrf.arpet.service.data.ViewCommand;
import org.gearvrf.mixedreality.GVRAnchor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ShareAnchorMode extends BasePetMode {
    private final String TAG = getClass().getSimpleName();

    private static final int DEFAULT_SERVER_LISTENING_TIMEOUT = ApiConstants.DISCOVERABLE_DURATION * 1000; // time in ms
    private static final int DEFAULT_GUEST_TIMEOUT = 10000;  // 10s for waiting to connect to the host
    private final int DEFAULT_SCREEN_TIMEOUT = 5000;  // 5s to change between screens

    private IPetConnectionManager mConnectionManager;
    private final Handler mHandler = new Handler();
    private ShareAnchorView mShareAnchorView;
    private final List<AnchoredObject> mAnchoredObjects;
    private CloudAnchorManager mCloudAnchorManager;
    private IMessageService mMessageService;

    public ShareAnchorMode(PetContext petContext, List<AnchoredObject> anchoredObjects) {
        super(petContext, new ShareAnchorView(petContext));
        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addEventHandler(new AppConnectionMessageHandler());
        mShareAnchorView = (ShareAnchorView) mModeScene;
        mShareAnchorView.setListenerShareAnchorMode(new HandlerSendingInvitation());
        mAnchoredObjects = anchoredObjects;
        mCloudAnchorManager = new CloudAnchorManager(petContext, new CloudAnchorManagerReadyListener());

        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new MessageReceiver());
    }

    @Override
    protected void onEnter() {

    }

    @Override
    protected void onExit() {

    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {

    }

    public class HandlerSendingInvitation implements OnGuestOrHostListener {

        @Override
        public void OnHost() {
            mConnectionManager.startUsersInvitation();
        }

        @Override
        public void OnGuest() {
            // Clear the scene once the user is in Guest mode and the objects will
            // be shared by the Host.
            clearSceneObjects();
            mConnectionManager.acceptInvitation();
            mShareAnchorView.modeGuest();
            mShareAnchorView.getProgressHandler().setDuration(DEFAULT_GUEST_TIMEOUT);
            mShareAnchorView.getProgressHandler().start();
        }
    }

    private void clearSceneObjects() {
        for (AnchoredObject object : mAnchoredObjects) {
            mPetContext.getMainScene().removeSceneObject(object.getAnchor());
        }
        mPetContext.getMainScene().removeSceneObjectByName(PlaneHandler.PLANE_NAME);
    }

    private void showWaitingForScreen() {
        mShareAnchorView.modeHost();
        mShareAnchorView.getProgressHandler().setDuration(DEFAULT_SERVER_LISTENING_TIMEOUT);
        mShareAnchorView.getProgressHandler().start();
        mHandler.postDelayed(this::OnWaitingForConnection, DEFAULT_SERVER_LISTENING_TIMEOUT);
    }

    private void showInviteAcceptedScreen() {
        if (mConnectionManager.getConnectionMode() == ConnectionMode.SERVER) {
            Log.d(TAG, "host");
            mShareAnchorView.inviteAcceptedHost(mConnectionManager.getTotalConnected());
            mHandler.postDelayed(() -> showParingScreen(ConnectionMode.SERVER), DEFAULT_SCREEN_TIMEOUT);
        } else {
            Log.d(TAG, "guest");
            mShareAnchorView.inviteAcceptedGuest();
        }
    }

    private void showInviteMain() {
        mPetContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShareAnchorView.invitationView();
            }
        });
    }

    private void OnWaitingForConnection() {
        Log.d(TAG, "OnWaitingForConnection: time up");
        mConnectionManager.stopUsersInvitation();
        if (mConnectionManager.getTotalConnected() > 0) {
            Log.d(TAG, "Total" + mConnectionManager.getTotalConnected());
            mShareAnchorView.inviteAcceptedGuest();
        }
    }

    @SuppressLint("HandlerLeak")
    private class AppConnectionMessageHandler implements PetConnectionEventHandler {

        @SuppressLint("SwitchIntDef")
        @Override
        public void handleEvent(PetConnectionEvent message) {
            mPetContext.getActivity().runOnUiThread(() -> {
                @PetConnectionEventType
                int messageType = message.getType();
                switch (messageType) {
                    case PetConnectionEventType.CONNECTION_ESTABLISHED:
                        handleConnectionEstablished();
                        break;
                    case PetConnectionEventType.CONNECTION_NOT_FOUND:
                        showInviteMain();
                        showToast("No connection found");
                        break;
                    case PetConnectionEventType.CONNECTION_ALL_LOST:
                        showToast("No active connection");
                        break;
                    case PetConnectionEventType.CONNECTION_LISTENER_STARTED:
                        showToast("Ready to accept connections");
                        showWaitingForScreen();
                        break;
                    case PetConnectionEventType.ERROR_BLUETOOTH_NOT_ENABLED:
                        showToast("Bluetooth is disabled");
                        break;
                    case PetConnectionEventType.ERROR_DEVICE_NOT_DISCOVERABLE:
                        showToast("Device is not visible to other devices");
                        break;
                    default:
                        break;
                }
            });
        }
    }

    private void loadModel(@AnchoredObject.ObjectType String type, GVRAnchor anchor) {
        Log.d(TAG, "loading model...");
        switch (type) {
            case AnchoredObject.ObjectType.CHARACTER:
                CharacterController pet = new CharacterController(mPetContext);
                pet.setAnchor(anchor);
                pet.enter();
                Log.d(TAG, "pet model has been loaded successfully!");
                break;
            default:
                Log.d(TAG, "invalid object type to load");
        }
    }

    private void handleConnectionEstablished() {
        switch (mConnectionManager.getConnectionMode()) {
            case ConnectionMode.CLIENT: {
                showInviteAcceptedScreen();
                break;
            }
            case ConnectionMode.SERVER: {
                showInviteAcceptedScreen();
                break;
            }
            case ConnectionMode.NONE:
            default:
                break;
        }
    }

    private void showToast(String text) {
        Toast.makeText(mPetContext.getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private void showParingScreen(@ConnectionMode int mode) {
        mPetContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShareAnchorView.pairingView();
            }
        });

        if (mode == ConnectionMode.SERVER) {
            sendCommandToShowPairingView();
            for (AnchoredObject object : mAnchoredObjects) {
                mCloudAnchorManager.hostAnchor(object);
            }
        }
    }

    private void showStayInPositionToPair() {
        mPetContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShareAnchorView.toPairView();
            }
        });
    }

    private void showParedView() {
        mPetContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShareAnchorView.paredView();
                mHandler.postDelayed(() -> showStatusModeView(), DEFAULT_SCREEN_TIMEOUT);
            }
        });
    }

    private void showStatusModeView() {
        mPetContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mShareAnchorView.modeView();
            }
        });
    }

    private void sendCommandToShowPairingView() {
        ViewCommand command = new ViewCommand(ViewCommand.SHOW_PAIRING_VIEW);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "command to show 'pairing view' was performed successfully");
            }

            @Override
            public void onFailure(Exception error) {
                Log.d(TAG, "command to show 'pairing view' has failed");
            }
        });
    }

    private void sendCommandToShowStayInPosition() {
        ViewCommand command = new ViewCommand(ViewCommand.SHOW_STAY_IN_POSITION_TO_PAIR);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "command to show 'stay in position' was performed successfully");
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "command to show 'stay in position' has failed");
            }
        });
    }

    private void sendCommandToShowPairedView() {
        ViewCommand command = new ViewCommand(ViewCommand.SHOW_PAIRED_VIEW);
        mMessageService.sendViewCommand(command, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showParedView();
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "command to show 'paired view' has failed");
            }
        });
    }

    private void shareScene(CloudAnchor[] anchors) {
        mMessageService.shareScene(new SharedScene(anchors), new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "all guests have resolved their cloud anchors");
                // Inform the guests to change their view
                sendCommandToShowPairedView();
            }

            @Override
            public void onFailure(Exception error) {
                Log.e(TAG, "some guest didn't get to resolve a cloud anchor");
            }
        });
    }

    private class CloudAnchorManagerReadyListener implements OnCloudAnchorManagerListener {
        @Override
        public void onHostReady() {
            // Just inform the guests to change their view
            sendCommandToShowStayInPosition();
            // Change the host view
            showStayInPositionToPair();

            Log.d(TAG, "sending a list of CloudAnchor objects to the guests");
            ArrayList<CloudAnchor> cloudAnchors = mCloudAnchorManager.getCloudAnchors();
            int listSize = cloudAnchors.size();
            shareScene(cloudAnchors.toArray(new CloudAnchor[listSize]));
        }
    }

    private class MessageReceiver extends SimpleMessageReceiver {

        @Override
        public void onReceiveSharedScene(SharedScene sharedScene) throws MessageException {
            Log.d(TAG, "Sharing received: " + Arrays.toString(sharedScene.getSceneObjects()));
            Task task = new SharedSceneLoader(sharedScene);
            // This method gets locked and will return after loader finish
            task.start();
            // Lock released, now checks thread result
            if (task.getError() != null) {
                showToast("Error loading objects: " + task.getError().getMessage());
                throw new MessageException(task.getError());
            } else {
                showToast("All objects successfully loaded");
            }
        }

        @Override
        public void onReceiveViewCommand(ViewCommand command) {
            Log.d(TAG, "View command received: " + command);
            switch (command.getType()) {
                case ViewCommand.SHOW_PAIRED_VIEW:
                    showParedView();
                    break;
                case ViewCommand.SHOW_STAY_IN_POSITION_TO_PAIR:
                    showStayInPositionToPair();
                    break;
                case ViewCommand.SHOW_PAIRING_VIEW:
                    showParingScreen(mConnectionManager.getConnectionMode());
                    break;
                default:
                    Log.d(TAG, "Unknown view command: " + command.getType());
                    break;
            }
        }

        @Override
        public void onReceiveUpdateSharedObject(SharedObject sharedObject) {
            if (sharedObject instanceof SharedCamera) {
                SharedCamera sharedCamera = sharedObject.get();
            } else if (sharedObject instanceof SharedPlane) {
                SharedPlane sharedPlane = sharedObject.get();
            }
        }
    }

    private class SharedSceneLoader extends Task {

        SharedScene mSharedScene;

        SharedSceneLoader(SharedScene sharedScene) {
            this.mSharedScene = sharedScene;
        }

        @Override
        public void execute() {

            Log.d(TAG, "Loading shared objects");
            mCloudAnchorManager.resolveAnchors(mSharedScene.getSceneObjects(), new CloudAnchorManager.OnResolveCallback() {
                @Override
                public void onAllResolved(List<ResolvedCloudAnchor> resolvedCloudAnchors) {
                    Log.i(TAG, "All anchors successfully resolved");
                    for (ResolvedCloudAnchor resolvedCloudAnchor : resolvedCloudAnchors) {
                        try {
                            loadModel(resolvedCloudAnchor.getCloudAnchor().getObjectType(), resolvedCloudAnchor.getAnchor());
                            String successString = String.format(Locale.getDefault(),
                                    "Success loading model for object of type %s",
                                    resolvedCloudAnchor.getCloudAnchor().getObjectType());
                            Log.i(TAG, successString);
                        } catch (Exception e) {
                            String errorString = String.format(Locale.getDefault(),
                                    "Error loading model for object of type %s",
                                    resolvedCloudAnchor.getCloudAnchor().getObjectType());
                            setError(new TaskException(errorString, e));
                            Log.e(TAG, errorString);
                            break;
                        }
                    }
                    notifyExecuted();
                }

                @Override
                public void onError(CloudAnchorException e) {
                    String errorString = "Error resolving anchors";
                    setError(new TaskException(errorString, e));
                    Log.e(TAG, errorString + ": " + e.getMessage());
                    notifyExecuted();
                }
            });
        }
    }
}
