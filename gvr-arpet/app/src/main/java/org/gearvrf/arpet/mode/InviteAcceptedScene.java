package org.gearvrf.arpet.mode;

import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class InviteAcceptedScene extends BasePetScene implements IViewEvents {
    private GVRContext mContext;
    private GVRSceneObject mInvited;

    public InviteAcceptedScene(GVRContext gvrContext) {
        super(gvrContext);
        mContext = gvrContext;
        mInvited = new GVRViewSceneObject(mContext, R.layout.invite_accepted_layout, this);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {

    }

    @Override
    public void onStartRendering(GVRViewSceneObject inviteAcceptedObject, View view) {
        inviteAcceptedObject.getTransform().setScale(3.2f, 3.2f, 1.0f);
        inviteAcceptedObject.getTransform().setPosition(0.0f, 1.4f, -4.0f);
        addChildObject(mInvited);
    }
}
