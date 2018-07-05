package org.gearvrf.videoplayer.component;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public abstract class FadeableObject extends GVRSceneObject {

    private static final float FADE_DURATION = .2F;

    public FadeableObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    public FadeableObject(GVRContext mContext, GVRMesh quad, GVRTexture gvrTexture) {
        super(mContext, quad, gvrTexture);
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
    public final void fadeIn(final OnFadeFinish callback) {
        if (getFadeable() instanceof GVRViewSceneObject) {
            getGVRContext().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doFadeIn(callback);
                }
            });
        } else {
            doFadeIn(callback);
        }
    }

    @CallSuper
    public final void fadeOut(final OnFadeFinish callback) {
        if (getFadeable() instanceof GVRViewSceneObject) {
            getGVRContext().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doFadeOut(callback);
                }
            });
        } else {
            doFadeOut(callback);
        }
    }

    private void doFadeIn(final OnFadeFinish callback) {
        GVROpacityAnimation animation = new GVROpacityAnimation(
                getFadeable(), FADE_DURATION, 1);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                if (callback != null) {
                    callback.onFadeFinished();
                }
            }
        });
        animation.start(getGVRContext().getAnimationEngine());
    }

    private void doFadeOut(final OnFadeFinish callback) {
        GVROpacityAnimation animation = new GVROpacityAnimation(
                getFadeable(), FADE_DURATION, 0);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                if (callback != null) {
                    callback.onFadeFinished();
                }
            }
        });
        animation.start(getGVRContext().getAnimationEngine());
    }

    public void show() {
        show(null);
    }

    public void show(final OnFadeFinish fadeInCallback) {
        if (!isEnabled()) {
            setEnable(true);
            fadeIn(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    if (fadeInCallback != null) {
                        fadeInCallback.onFadeFinished();
                    }
                }
            });
        }
    }

    public void hide() {
        hide(null);
    }

    public void hide(final OnFadeFinish fadeOutCallback) {
        if (isEnabled()) {
            fadeOut(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    setEnable(false);
                    if (fadeOutCallback != null) {
                        fadeOutCallback.onFadeFinished();
                    }
                }
            });
        }
    }
}
