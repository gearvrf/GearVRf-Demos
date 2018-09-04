package org.gearvrf.arpet.mode;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;

public class ShareAnchorMode extends BasePetMode {
    ShareAnchorView mSendingInvitationScene;

    public ShareAnchorMode(GVRContext context) {
        super(context, new ShareAnchorView(context));

        ((ShareAnchorView) mModeScene).setSendingInviteListener(new HandlerSendingInvitation());
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


    private class HandlerSendingInvitation implements OnShareAnchorModeListener {

        @Override
        public void OnSending() {
            mSendingInvitationScene.OnSending();
        }
    }

}
