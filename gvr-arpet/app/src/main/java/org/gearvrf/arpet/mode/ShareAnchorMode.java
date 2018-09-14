package org.gearvrf.arpet.mode;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.sharing.IPetConnectionManager;
import org.gearvrf.arpet.sharing.PetConnectionManager;
import org.gearvrf.arpet.sharing.PetConnectionMessage;
import org.gearvrf.arpet.sharing.PetConnectionMessageHandler;
import org.gearvrf.arpet.sharing.PetConnectionMessageType;

public class ShareAnchorMode extends BasePetMode {
    private final String TAG = getClass().getSimpleName();

    private final int DEFAULT_SERVER_LISTENNING_TIMEOUT = 30000; // 30s for waiting incoming connections
    private final int DEFAULT_GUEST_TIMEOUT = 12000;  // 12s according to bluetooth timeout

    private OnGuestOrHostListener mGuestOrHostListener;
    private PetConnectionMessageHandler mMessageHandler;
    private IPetConnectionManager mConnectionManager;
    private final Handler mHandler = new Handler();
    private ShareAnchorView mShareAnchorView;

    public ShareAnchorMode(PetContext petContext) {
        super(petContext, new ShareAnchorView(petContext));
        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addMessageHandler(new AppConnectionMessageHandler());
        mShareAnchorView = (ShareAnchorView) mModeScene;
        mShareAnchorView.setListenerShareAnchorMode(new HandlerSendingInvitation());
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
            mShareAnchorView.getmProgressHandler().setDuration(DEFAULT_GUEST_TIMEOUT);
            mShareAnchorView.getmProgressHandler().start();
        }
    }

    private void showWaitingForScreen() {
        mShareAnchorView.modeHost();
        mShareAnchorView.getmProgressHandler().setDuration(DEFAULT_SERVER_LISTENNING_TIMEOUT);
        mShareAnchorView.getmProgressHandler().start();
        mHandler.postDelayed(this::OnWaitingForConnection, DEFAULT_SERVER_LISTENNING_TIMEOUT);
    }

    private void showInviteAcceptedScreen() {
        if (mConnectionManager.getConnectionMode() == ConnectionMode.SERVER) {
            Log.d(TAG, "host");
            mShareAnchorView.inviteAcceptedHost(mConnectionManager.getTotalConnected());
            mHandler.postDelayed(() -> showParingScreen(), Toast.LENGTH_LONG);
        } else {
            Log.d(TAG, "guest");
            mShareAnchorView.inviteAcceptedGuest();
            mHandler.postDelayed(() -> showParingScreen(), Toast.LENGTH_LONG);
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
    private class AppConnectionMessageHandler extends Handler implements PetConnectionMessageHandler {

        @Override
        public void handleMessage(PetConnectionMessage message) {
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

            @PetConnectionMessageType
            int messageType = msg.what;

            switch (messageType) {

                case PetConnectionMessageType.CONNECTION_ESTABLISHED:
                    handleConnectionEstablished();
                    break;
                case PetConnectionMessageType.CONNECTION_NOT_FOUND:
                    showInviteMain();
                    showToast("No connection found");
                    break;
                case PetConnectionMessageType.CONNECTION_LOST:
                    showToast("No active connection");
                    break;
                case PetConnectionMessageType.CONNECTION_LISTENER_STARTED:
                    showToast("Ready to accept connections");
                    showWaitingForScreen();
                    break;
                case PetConnectionMessageType.ERROR_BLUETOOTH_NOT_ENABLED:
                    showToast("Bluetooth is disabled");
                    break;
                case PetConnectionMessageType.ERROR_DEVICE_NOT_DISCOVERABLE:
                    showToast("Device is not visible to other devices");
                    break;
                case PetConnectionMessageType.MESSAGE_RECEIVED:
                default:
                    break;
            }
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

    private void showParingScreen() {
        mShareAnchorView.pairingView();
    }

    private void showStayInPositionToPair() {
        mShareAnchorView.toPairView();
    }

    private void showParedView() {
        mShareAnchorView.paredView();
    }

    private void showStatusModeView() {
        mShareAnchorView.modeView();
    }
}
