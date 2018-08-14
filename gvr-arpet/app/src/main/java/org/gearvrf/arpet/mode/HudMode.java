package org.gearvrf.arpet.mode;

import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.OnHudItemClicked;

public class HudMode implements OnHudItemClicked {
    private HudScene mHudScene;
    private GVRScene mScene;

    public HudMode(GVRContext context, GVRScene scene) {
        mHudScene = new HudScene(context, this);
        mScene = scene;
    }

    public HudScene getPlayScene() {
        return mHudScene;
    }

    @Override
    public void onBallClicked() {

    }

    @Override
    public void onShareAnchorClicked() {

    }

    @Override
    public void onEditModeClicked() {
        Log.d("XX", "Edit Mode");
    }

    @Override
    public void onCameraClicked() {

    }

    public void handleOrientation() {
        GVRTransform cameraTransform = mScene.getMainCameraRig().getHeadTransform();
        float rotationZAxis = cameraTransform.getRotationRoll();

        if (rotationZAxis < 3 && rotationZAxis > -60 || rotationZAxis > -80 && rotationZAxis < 175) {
            mHudScene.getTransform().setPosition(cameraTransform.getPositionX() + 1.1f, cameraTransform.getPositionY(), cameraTransform.getPositionZ() - 5);
            mHudScene.getTransform().setRotation(0.9836236f, -0.09276326f, -0.15403655f, -0.012204962f);
            Log.d("XX", "Landscape " + " Z: " + rotationZAxis);
        } else {
            mHudScene.getTransform().setPosition(cameraTransform.getPositionX() + 0.7f, cameraTransform.getPositionY() + 1.4f, cameraTransform.getPositionZ() - 5);
            mHudScene.getTransform().setRotation(0.6800038f - 1.0f, -0.166928f, 0.07900262f, -0.709568f);
            Log.d("XX", "Portrait " + " Z: " + rotationZAxis);
        }
    }

}
