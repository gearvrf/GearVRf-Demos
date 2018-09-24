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
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.manager.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.manager.cloud.anchor.OnCloudAnchorManagerListener;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventHandler;
import org.gearvrf.arpet.manager.connection.PetConnectionEventType;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.sharing.SharingMessageCallback;
import org.gearvrf.arpet.sharing.SharingService;
import org.gearvrf.arpet.sharing.SharingServiceMessageReceiver;
import org.gearvrf.arpet.sharing.SyncTask;
import org.gearvrf.arpet.sharing.message.Command;
import org.gearvrf.mixedreality.GVRAnchor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShareAnchorMode extends BasePetMode {
    private final String TAG = getClass().getSimpleName();

    private final int DEFAULT_SERVER_LISTENNING_TIMEOUT = ApiConstants.DISCOVERABLE_DURATION * 1000; // time in ms
    private final int DEFAULT_GUEST_TIMEOUT = 10000;  // 10s for waiting to connect to the host
    private final int DEFAULT_SCREEN_TIMEOUT = 5000;  // 5s to change between screens

    private IPetConnectionManager mConnectionManager;
    private final Handler mHandler = new Handler();
    private ShareAnchorView mShareAnchorView;
    private final List<AnchoredObject> mAnchoredObjects;
    private CloudAnchorManager mCloudAnchorManager;
    private SharingService mSharingService;

    public ShareAnchorMode(PetContext petContext, List<AnchoredObject> anchoredObjects) {
        super(petContext, new ShareAnchorView(petContext));
        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addEventHandler(new AppConnectionMessageHandler());
        mShareAnchorView = (ShareAnchorView) mModeScene;
        mShareAnchorView.setListenerShareAnchorMode(new HandlerSendingInvitation());
        mAnchoredObjects = anchoredObjects;
        mCloudAnchorManager = new CloudAnchorManager(petContext, new CloudAnchorManagerReadyListener());

        mSharingService = SharingService.getInstance();
        mSharingService.addMessageReceiver(new MessageReceiver());
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
        mShareAnchorView.getProgressHandler().setDuration(DEFAULT_SERVER_LISTENNING_TIMEOUT);
        mShareAnchorView.getProgressHandler().start();
        mHandler.postDelayed(this::OnWaitingForConnection, DEFAULT_SERVER_LISTENNING_TIMEOUT);
    }

    private void showInviteAcceptedScreen() {
        if (mConnectionManager.getConnectionMode() == ConnectionMode.SERVER) {
            Log.d(TAG, "host");
            mShareAnchorView.inviteAcceptedHost(mConnectionManager.getTotalConnected());
            mHandler.postDelayed(() -> showParingScreen(ConnectionMode.SERVER), DEFAULT_SCREEN_TIMEOUT);
        } else {
            Log.d(TAG, "guest");
            mShareAnchorView.inviteAcceptedGuest();
            mHandler.postDelayed(() -> showParingScreen(ConnectionMode.CLIENT), DEFAULT_SCREEN_TIMEOUT);
        }
    }

    private void showInviteMain() {
        mShareAnchorView.invitationView();
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

        @Override
        public void handleEvent(PetConnectionEvent message) {
            mPetContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                        case PetConnectionEventType.MESSAGE_RECEIVED:
                        default:
                            break;
                    }
                }
            });
        }
    }

    private void loadModel(int type, GVRAnchor anchor) {
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
        mShareAnchorView.pairingView();
        if (mode == ConnectionMode.SERVER) {
            for (AnchoredObject object : mAnchoredObjects) {
                mCloudAnchorManager.hostAnchor(object);
            }
        }
        mHandler.postDelayed(() -> showStayInPositionToPair(), DEFAULT_SCREEN_TIMEOUT);
    }

    private void showStayInPositionToPair() {
        mShareAnchorView.toPairView();
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

    private class CloudAnchorManagerReadyListener implements OnCloudAnchorManagerListener {
        @Override
        public void onHostReady() {
            Log.d(TAG, "sending a list of CloudAnchor objects to the guests");
            ArrayList<CloudAnchor> cloudAnchors = mCloudAnchorManager.getCloudAnchors();
            int listSize = cloudAnchors.size();
            mSharingService.shareScene(cloudAnchors.toArray(new CloudAnchor[listSize]), new SharingMessageCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "all guests have resolved their cloud anchors");
                    mSharingService.sendCommand(Command.SHOW_PAIRED_VIEW, new SharingMessageCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            showParedView();
                        }

                        @Override
                        public void onFailure(Exception error) {

                        }
                    });
                }

                @Override
                public void onFailure(Exception error) {

                }
            });
        }
    }

    private class MessageReceiver implements SharingServiceMessageReceiver {
        @Override
        public void onReceiveSharedScene(Serializable[] sharedObjects) {
            Log.d(TAG, "Sharing received: " + Arrays.toString(sharedObjects));
            // This method will return only after loader finish
            new SharedSceneLoader(sharedObjects).start();
        }

        @Override
        public void onReceiveCommand(@Command String command) {
            Log.d(TAG, "Command received: " + command);
            if (command.equals(Command.SHOW_PAIRED_VIEW)) {
                showParedView();
            }
        }
    }

    private class SharedSceneLoader extends SyncTask {

        Serializable[] mSharedObjects;
        int countResolveFailure;
        int countResolveSuccess;

        SharedSceneLoader(Serializable[] sharedObjects) {
            this.mSharedObjects = sharedObjects;
        }

        @Override
        public void process() {
            Log.d(TAG, "Loading shared scene");
            handleResolvedAnchors((CloudAnchor[]) mSharedObjects);
        }

        private void handleResolvedAnchors(CloudAnchor[] cloudAnchors) {
            for (CloudAnchor cloudAnchor : cloudAnchors) {
                Log.d(TAG, "resolving cloud anchor ID...");
                mCloudAnchorManager.resolveAnchor(cloudAnchor.getCloudAnchorId(),
                        (anchor) -> {
                            if (anchor != null) {
                                Log.d(TAG, "anchor ID has been resolved successfully");
                                loadModel(cloudAnchor.getObjectType(), anchor);
                                countResolveSuccess++;
                            } else {
                                Log.e(TAG, "could not be possible to resolve a cloud anchor ID");
                                countResolveFailure++;
                            }
                            notifyProcessed();
                            // TODO: handle the exception in sharing service
                            /* if (countResolveFailure > 0) {
                                throw new RuntimeException("all objects were not resolved");
                            }*/
                        });
            }
        }
    }
}
