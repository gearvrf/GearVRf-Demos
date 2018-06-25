package org.gearvrf.videoplayer.component;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public abstract class FadeableViewObject extends GVRViewSceneObject {

    private static final float FADE_DURATION = .2F;

    public FadeableViewObject(GVRContext gvrContext, View view, float width, float height) {
        super(gvrContext, view, width, height);
    }
    
    @NonNull
    protected abstract GVRSceneObject getFadeable();

    public final void fadeIn(final FadeInCallback callback) {
        Log.d(getClass().getSimpleName(), "fadeIn: " + getClass().getSimpleName());
        GVROpacityAnimation animation = new GVROpacityAnimation(
                getFadeable(), FADE_DURATION, 1);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                if (callback != null) {
                    callback.onFadeIn();
                }
            }
        });
        animation.start(getGVRContext().getAnimationEngine());
    }

    public final void fadeOut(final FadeOutCallback callback) {
        Log.d(getClass().getSimpleName(), "fadeOut: " + getClass().getSimpleName());
        GVROpacityAnimation animation = new GVROpacityAnimation(
                getFadeable(), FADE_DURATION, 0);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                if (callback != null) {
                    callback.onFadeOut();
                }
            }
        });
        animation.start(getGVRContext().getAnimationEngine());
    }

    public interface FadeInCallback {
        void onFadeIn();
    }

    public interface FadeOutCallback {
        void onFadeOut();
    }

}
