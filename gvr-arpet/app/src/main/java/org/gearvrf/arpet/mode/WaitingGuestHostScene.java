package org.gearvrf.arpet.mode;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class WaitingGuestHostScene extends BasePetScene implements IViewEvents, View.OnClickListener {
    private GVRContext mContext;
    private GVRSceneObject mInvitedObject;
    private Button allowButton, denyButton;
    private OnWaitingGuestHostListener mlistenerReceiveInvite;
    private ProgressBar mProgressBar;

    public void setListener(OnWaitingGuestHostListener listener) {
        mlistenerReceiveInvite = listener;
    }

    public WaitingGuestHostScene(GVRContext context) {
        super(context);
        mContext = context;
        mInvitedObject = new GVRViewSceneObject(mContext, R.layout.waiting_guest_host, this);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        allowButton = view.findViewById(R.id.guest_button);
        denyButton = view.findViewById(R.id.host_button);
        mProgressBar = view.findViewById(R.id.progress);
        mProgressBar.setIndeterminate(false);

        allowButton.setOnClickListener(this);
        denyButton.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject invitedSceneObject, View view) {
        invitedSceneObject.getTransform().setScale(3.2f, 3.2f, 1.0f);
        invitedSceneObject.getTransform().setPosition(0.0f, 1.4f, -5.0f);
        addChildObject(mInvitedObject);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.guest_button) {
            allowButton.post(new Runnable() {
                @Override
                public void run() {
                    mlistenerReceiveInvite.OnHost();
                }
            });
        } else if (view.getId() == R.id.host_button) {
            denyButton.post(new Runnable() {
                @Override
                public void run() {
                    mlistenerReceiveInvite.OnGuest();
                }
            });
        }
    }
}
