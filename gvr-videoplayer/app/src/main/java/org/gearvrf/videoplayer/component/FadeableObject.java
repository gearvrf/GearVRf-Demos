package org.gearvrf.videoplayer.component;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

public abstract class FadeableObject extends GVRSceneObject {

    private static final float FADE_DURATION = .2F;

    public FadeableObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    @NonNull
    protected abstract GVRSceneObject getFadeable();

    public final void fadeIn() {
        fadeIn(null);
    }

    public final void fadeOut() {
        fadeOut(null);
    }

    @CallSuper
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

    @CallSuper
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
