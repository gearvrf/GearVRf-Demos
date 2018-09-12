package org.gearvrf.arpet.mode;

import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.Log;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;

import org.gearvrf.arpet.PetContext;

import org.gearvrf.arpet.sharing.IAppConnectionManager;
import org.gearvrf.arpet.sharing.UiMessageHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ShareAnchorMode extends BasePetMode {
    @IntDef({Mode.HOST,
            Mode.GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
        int HOST = 0;
        int GUEST = 1;
    }

    private OnGuestOrHostListener mGuestOrHostListener;
    private UiMessageHandler mMessageHandler;
    private IAppConnectionManager mConnectionManager;
    private final Handler mHandler = new Handler();
    private ShareAnchorView mShareAnchorView;
    @Mode
    private int mMode;

    public ShareAnchorMode(PetContext petContext, IAppConnectionManager iAppConnectionManager) {
        super(petContext, new ShareAnchorView(petContext));
        mConnectionManager = iAppConnectionManager;

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

    public void showWaitingForScreen(){
        mShareAnchorView.modeHost();
        mHandler.postDelayed(() -> OnWaitingForConnection(), 30000);
    }

    public void showInviteAcceptedScreen(){
        if (mMode == Mode.HOST){
            mShareAnchorView.inviteAcceptedHost(mConnectionManager.getTotalConnected());
        }else {
            mShareAnchorView.inviteAcceptedGuest();
        }
    }

    public void showInviteMain(){
        mShareAnchorView.invitationView();
    }

    private void OnWaitingForConnection() {
        Log.d("XX", "OnWaitingForConnection: time up");
        mConnectionManager.stopUsersInvitation();
        if (mConnectionManager.getTotalConnected() > 0){
            Log.d("XX", "Total"+ mConnectionManager.getTotalConnected());
            mShareAnchorView.inviteAcceptedGuest();
         // 3 segundos tela de conectados

         // pareando

        }else {
            //enviar mensagem de sem conexão
        }
    }

}
