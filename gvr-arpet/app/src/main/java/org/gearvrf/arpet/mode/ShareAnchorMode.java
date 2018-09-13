package org.gearvrf.arpet.mode;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.connection.socket.ConnectionMode;
import org.gearvrf.arpet.sharing.PetConnectionManager;
import org.gearvrf.arpet.sharing.IPetConnectionManager;
import org.gearvrf.arpet.sharing.PetConnectionMessage;
import org.gearvrf.arpet.sharing.PetConnectionMessageHandler;
import org.gearvrf.arpet.sharing.PetConnectionMessageType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ShareAnchorMode extends BasePetMode {

    @IntDef({Mode.HOST, Mode.GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        int HOST = 0;
        int GUEST = 1;
    }

    private OnGuestOrHostListener mGuestOrHostListener;
    private PetConnectionMessageHandler mMessageHandler;
    private IPetConnectionManager mConnectionManager;
    private final Handler mHandler = new Handler();
    private ShareAnchorView mShareAnchorView;
    @Mode
    private int mMode;

    public ShareAnchorMode(PetContext petContext) {
        super(petContext, new ShareAnchorView(petContext));
        mConnectionManager = PetConnectionManager.getInstance();
        mConnectionManager.addUiMessageHandler(new AppConnectionMessageHandler());
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
            mMode = Mode.HOST;
            mConnectionManager.startUsersInvitation();
        }

        @Override
        public void OnGuest() {
            mMode = Mode.GUEST;
            mConnectionManager.acceptInvitation();
            mShareAnchorView.modeGuest();
        }
    }

    private void showWaitingForScreen() {
        mShareAnchorView.modeHost();
        mHandler.postDelayed(this::OnWaitingForConnection, 30000);
    }

    private void showInviteAcceptedScreen() {
        if (mMode == Mode.HOST) {
            mShareAnchorView.inviteAcceptedHost(mConnectionManager.getTotalConnected());
        } else {
            mShareAnchorView.inviteAcceptedGuest();
        }
    }

    private void showInviteMain() {
        mShareAnchorView.invitationView();
    }

    private void OnWaitingForConnection() {
        Log.d("XX", "OnWaitingForConnection: time up");
        mConnectionManager.stopUsersInvitation();
        if (mConnectionManager.getTotalConnected() > 0) {
            Log.d("XX", "Total" + mConnectionManager.getTotalConnected());
            mShareAnchorView.inviteAcceptedGuest();
            // 3 segundos tela de conectados

            // pareando
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
                showInviteMain();
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
}
