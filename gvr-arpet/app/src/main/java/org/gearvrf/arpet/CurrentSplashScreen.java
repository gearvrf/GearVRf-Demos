package org.gearvrf.arpet;

import android.util.DisplayMetrics;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVROpacityAnimation;

public class CurrentSplashScreen {
    private GVRSceneObject mSplashScreen;
    private GVRContext mContext;

    public CurrentSplashScreen(PetContext petContext) {
        mContext = petContext.getGVRContext();

        onInit();
    }

    private void onInit() {
        final int mDisplayWidth;
        final int mDisplayHeight;

        GVRTexture tex = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.drawable.splash_view));
        final DisplayMetrics metrics = new DisplayMetrics();
        mContext.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        mDisplayWidth = metrics.widthPixels;
        mDisplayHeight = metrics.heightPixels;
        final float size = (float) Math.max(mDisplayWidth, mDisplayHeight);
        mSplashScreen = new GVRSceneObject(mContext, mDisplayWidth / size, mDisplayHeight / size);
        mSplashScreen.getRenderData().getMaterial().setMainTexture(tex);
        mSplashScreen.getTransform().setPosition(0.0f, 0.0f, -0.7f);
    }

    protected void onShow() {
        mContext.getMainScene().getMainCameraRig().addChildObject(mSplashScreen);
    }

    protected void onHide() {
        GVROpacityAnimation mAnimation;
        mAnimation = new GVROpacityAnimation(mSplashScreen, .8f, 0);
        mAnimation.setOnFinish(gvrAnimation -> mContext.getMainScene().getMainCameraRig().removeChildObject(mSplashScreen));
        mAnimation.start(mContext.getAnimationEngine());
    }

}
