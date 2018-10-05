package org.gearvrf.arpet;

import android.view.View;

import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.arpet.mode.BasePetView;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class CurrentSplashScreen extends BasePetView implements IViewEvents {
    private GVRViewSceneObject mSplashScreenObject;

    public CurrentSplashScreen(PetContext petContext) {
        super(petContext);
        mSplashScreenObject = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.splash_layout, this);
        getTransform().setPosition(0.0f, 0.0f, -0.7f);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {

    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.setTextureBufferSize(ApiConstants.TEXTURE_BUFFER_SIZE);
        addChildObject(gvrViewSceneObject);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        GVROpacityAnimation mAnimation;
        mAnimation = new GVROpacityAnimation(mSplashScreenObject, .8f, 0);
        mAnimation.setOnFinish(gvrAnimation -> mainScene.getMainCameraRig().removeChildObject(CurrentSplashScreen.this));
        mAnimation.start(getGVRContext().getAnimationEngine());
    }

}
