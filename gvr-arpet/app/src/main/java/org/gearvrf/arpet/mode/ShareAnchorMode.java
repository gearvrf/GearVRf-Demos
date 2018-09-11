package org.gearvrf.arpet.mode;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;

public class ShareAnchorMode extends BasePetMode {
    private OnGuestOrHostListener mGuestOrHostListener;

    public ShareAnchorMode(GVRContext context, OnGuestOrHostListener listener) {
        super(context, new ShareAnchorView(context));
        mGuestOrHostListener = listener;

        ((ShareAnchorView) mModeScene).setListenerShareAnchorMode(new HandlerSendingInvitation());
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


    private class HandlerSendingInvitation implements OnGuestOrHostListener {

        @Override
        public void OnHost() {
            mGuestOrHostListener.OnHost();
        }

        @Override
        public void OnGuest() {

        }
    }

}
