package org.gearvrf.arpet.mode;

import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.mode.timer.SendingInvitationTimer;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class SendingInvitationScene extends BasePetScene implements IViewEvents, View.OnClickListener {
    private GVRContext mContext;
    private GVRSceneObject mInvitationObject;
    private OnSendingInviteListener mSendingInviteListener;
    SendingInvitationTimer mSendingInvitationTimer;

    public SendingInvitationScene(GVRContext gvrContext) {
        super(gvrContext);
        mContext = gvrContext;
        mInvitationObject = new GVRViewSceneObject(mContext, R.layout.invitation_sent, this);
        mSendingInvitationTimer = new SendingInvitationTimer(this);
    }

    public void setSendingInviteListener(OnSendingInviteListener listener) {
        mSendingInviteListener = listener;
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {

    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.getTransform().setScale(7.2f, 6.2f, 1.0f);
        gvrViewSceneObject.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        addChildObject(mInvitationObject);
        mInvitationObject.getRenderData().getMaterial().setColor(0.6f, 0.6f, 0.6f);
    }

    public void startTimer() {
        mSendingInvitationTimer.start();
    }

    public void OnSending() {
        mSendingInviteListener.OnSending();
    }

    @Override
    public void onClick(View view) {

    }
}
