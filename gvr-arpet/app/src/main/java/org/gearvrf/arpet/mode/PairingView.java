package org.gearvrf.arpet.mode;

import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class PairingView extends BasePetView implements IViewEvents {
    private GVRSceneObject mPairing;

    public PairingView(PetContext petContext) {
        super(petContext);
        mPairing = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.paring_layout, this);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {

    }

    @Override
    public void onStartRendering(GVRViewSceneObject pairingSceneObject, View view) {
        pairingSceneObject.getTransform().setScale(3.2f, 3.2f, 1.0f);
        pairingSceneObject.getTransform().setPosition(0.0f, 1.4f, -5.0f);
        addChildObject(mPairing);
    }
}
