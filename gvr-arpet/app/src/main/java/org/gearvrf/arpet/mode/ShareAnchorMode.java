package org.gearvrf.arpet.mode;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.cloud.anchor.CloudAnchor;
import org.gearvrf.arpet.cloud.anchor.CloudAnchorManager;
import org.gearvrf.arpet.cloud.anchor.CommandViewMessage;
import org.gearvrf.arpet.cloud.anchor.OnCloudAnchorManagerListener;
import org.gearvrf.arpet.cloud.anchor.ResolveStatusMessage;
import org.gearvrf.arpet.cloud.anchor.ShareSceneObjectsMessage;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.SendMessageCallback;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.manager.connection.IPetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEventType;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventHandler;
import org.gearvrf.mixedreality.GVRAnchor;

import java.util.ArrayList;
import java.util.List;

public class ShareAnchorMode extends BasePetMode {
    private final String TAG = getClass().getSimpleName();

    private final int DEFAULT_SERVER_LISTENNING_TIMEOUT = ApiConstants.DISCOVERABLE_DURATION * 1000; // time in ms
    private final int DEFAULT_GUEST_TIMEOUT = 10000;  // 10s for waiting to connect to the host
    private final int DEFAULT_SCREEN_TIMEOUT = 5000;  // 5s to change between screens

    private OnGuestOrHostListener mGuestOrHostListener;
    private PetConnectionEventHandler mMessageHandler;
    private IPetConnectionManager mConnectionManager;
    private final Handler mHandler = new Handler();
    private ShareAnchorView mShareAnchorView;
    private final List<AnchoredObject> mAnchoredObjects;
    private CloudAnchorManager mCloudAnchorManager;
    private int mCountResolveSuccess;
    private int mCountResolveFailure;

    public ShareAnchorMode(PetContext petContext, List<AnchoredObject> anchoredObjects) {
        super(petContext, new ShareAnchorView(petContext));
        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addEventHandler(new AppConnectionMessageHandler());
        mShareAnchorView = (ShareAnchorView) mModeScene;
        mShareAnchorView.setListenerShareAnchorMode(new HandlerSendingInvitation());
        mAnchoredObjects = anchoredObjects;
        mCloudAnchorManager = new CloudAnchorManager(petContext, new CloudAnchorManagerReadyListener());
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
            mConnectionManager.acceptInvitation();
            mShareAnchorView.modeGuest();
            mShareAnchorView.getProgressHandler().setDuration(DEFAULT_GUEST_TIMEOUT);
            mShareAnchorView.getProgressHandler().start();
        }
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
    private class AppConnectionMessageHandler extends Handler implements PetConnectionEventHandler {

        @Override
        public void handleEvent(PetConnectionEvent message) {
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
                    "handleEvent: {what: %s, data: %s}",
                    msg.what, msg.getData().getSerializable("data"));
            Log.d(TAG, log);
            Message data = (Message) msg.getData().getSerializable("data");

            @PetConnectionEventType
            int messageType = msg.what;

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
                    handleReceivedMessage(data);
                default:
                    break;
            }
        }
    }

    private void handleReceivedMessage(Message message) {
        Log.d(TAG, "message received, ready to handle it");
        if (message instanceof ShareSceneObjectsMessage && mConnectionManager.isConnectedAs(ConnectionMode.CLIENT)) {
            ArrayList<CloudAnchor> anchors = (ArrayList<CloudAnchor>) message.getData();
            mCountResolveSuccess = 0;
            mCountResolveFailure = 0;
            for (CloudAnchor cloudAnchor : anchors) {
                Log.d(TAG, "resolving cloud anchor ID...");
                mCloudAnchorManager.resolveAnchor(cloudAnchor.getCloudAnchorId(),
                        (anchor) -> {
                            if (anchor != null) {
                                Log.d(TAG, "anchor ID has been resolved successfully");
                                loadModel(cloudAnchor.getObjectType(), anchor);
                                mCountResolveSuccess++;
                            } else {
                                Log.e(TAG, "could not be possible to resolve a cloud anchor ID");
                                mCountResolveFailure++;
                            }

                            if ((mCountResolveSuccess + mCountResolveFailure) == anchors.size()) {
                                // inform the host that all anchors were resolved successfully in this client
                                mConnectionManager.sendMessage(new ResolveStatusMessage(ResolveStatusMessage.StatusType.RESOLVE_OK), new SendMessageCallback() {
                                    @Override
                                    public void onResult(int totalSent) {

                                    }
                                });
                            }
                        });
            }
        }

        if (mConnectionManager.isConnectedAs(ConnectionMode.CLIENT) && message instanceof CommandViewMessage) {
            @CommandViewMessage.CommandViewType
            int type = (int) message.getData();
            switch (type) {
                case CommandViewMessage.CommandViewType.SHOW_PAIRED_VIEW:
                    Log.d(TAG, "show paired view");
                    showParedView();
                    break;
                default:
                    Log.d(TAG, "invalid command view");
            }
        }

        if (mConnectionManager.isConnectedAs(ConnectionMode.SERVER) && message instanceof ResolveStatusMessage) {
            @ResolveStatusMessage.StatusType
            int type = (int) message.getData();
            switch (type) {
                case ResolveStatusMessage.StatusType.RESOLVE_OK:
                    showParedView();
                    mConnectionManager.sendMessage(new CommandViewMessage(CommandViewMessage.CommandViewType.SHOW_PAIRED_VIEW), new SendMessageCallback() {
                        @Override
                        public void onResult(int totalSent) {

                        }
                    });
                    break;
                default:
                    Log.d(TAG, "invalid status type");
            }
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
            Log.d(TAG, "sending a list of CloudAnchor objects to the clients");
            mConnectionManager.sendMessage(new ShareSceneObjectsMessage(mCloudAnchorManager.getCloudAnchors()), new SendMessageCallback() {
                @Override
                public void onResult(int totalSent) {

                }
            });
        }
    }
}
