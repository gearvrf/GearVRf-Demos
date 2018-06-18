package org.gearvrf.videoplayer.component.video.loading;

import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.videoplayer.R;

public class LoadingAsset extends GVRSceneObject {
    private static final String TAG = LoadingAsset.class.getSimpleName();
    GVRAnimation mAnimation;

    public LoadingAsset(GVRContext gvrContext, float width, float height) {
        super(gvrContext,
                gvrContext.createQuad(width, height),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.ic_loading)));

        mAnimation = new GVRRotationByAxisWithPivotAnimation(this, 2, -360f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f).start(gvrContext.getAnimationEngine());
        mAnimation.setRepeatMode(1);
        mAnimation.setRepeatCount(-1);

        Log.d(TAG, "RotateAsset ");
    }

}
