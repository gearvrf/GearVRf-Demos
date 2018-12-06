package org.gearvrf.arpet;

import android.util.DisplayMetrics;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

public class CurrentSplashScreen {
    private GVRSceneObject mSplashScreen;
    private GVRContext mContext;

    public CurrentSplashScreen(GVRContext context) {
        mContext = context;

        onInit();
    }

    private void onInit() {
        final GVRPerspectiveCamera cam = mContext.getMainScene().getMainCameraRig().getCenterCamera();
        final float aspect = cam.getAspectRatio();
        final float near = cam.getNearClippingDistance();
        final double fov = Math.toRadians(cam.getFovY());
        final float z = 1.0f;
        final float h = (float)(z * Math.tan(fov * 0.5f));
        final float w = aspect * h;

        GVRTexture tex = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.drawable.splash_view));
        mSplashScreen = new GVRSceneObject(mContext, 2 * w, 2 * h);
        mSplashScreen.getRenderData().getMaterial().setMainTexture(tex);
        mSplashScreen.getTransform().setPosition(0.0f, 0.0f, -z);
    }

    protected void onShow() {
        mContext.getMainScene().getMainCameraRig().addChildObject(mSplashScreen);
    }

    protected void onHide(final GVRScene mainScene) {
        GVROpacityAnimation mAnimation;
        mAnimation = new GVROpacityAnimation(mSplashScreen, .8f, 0);
        mAnimation.setOnFinish(new GVROnFinish() {
                                   @Override
                                   public void finished(GVRAnimation gvrAnimation) {
                                       mContext.getMainScene().getMainCameraRig().removeChildObject(mSplashScreen);
                                       mContext.setMainScene(mainScene);
                                   }
                               });
        mAnimation.start(mContext.getAnimationEngine());
    }

}
