package org.gearvrf.videoplayer.component;

import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

public abstract class FadeableComponent extends GVRSceneObject {

    private static final float FADE_DURATION = .2F;

    public FadeableComponent(GVRContext gvrContext) {
        super(gvrContext);
    }

    public FadeableComponent(GVRContext gvrContext, GVRMesh mesh) {
        super(gvrContext, mesh);
    }

    public FadeableComponent(GVRContext gvrContext, float width, float height) {
        super(gvrContext, width, height);
    }

    public FadeableComponent(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture, GVRShaderId shaderId) {
        super(gvrContext, mesh, texture, shaderId);
    }

    public FadeableComponent(GVRContext gvrContext, GVRMesh mesh, GVRMaterial material) {
        super(gvrContext, mesh, material);
    }

    public FadeableComponent(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
    }

    public FadeableComponent(GVRContext gvrContext, GVRAndroidResource mesh, GVRAndroidResource texture) {
        super(gvrContext, mesh, texture);
    }

    public FadeableComponent(GVRContext gvrContext, float width, float height, GVRTexture texture, GVRShaderId shaderId) {
        super(gvrContext, width, height, texture, shaderId);
    }

    public FadeableComponent(GVRContext gvrContext, float width, float height, String meshDesc, GVRMaterial material) {
        super(gvrContext, width, height, meshDesc, material);
    }

    public FadeableComponent(GVRContext gvrContext, float width, float height, GVRTexture texture) {
        super(gvrContext, width, height, texture);
    }

    @NonNull
    protected abstract GVRSceneObject getFadeable();

    public final void fadeIn() {
        fadeIn(null);
    }

    public final void fadeOut() {
        fadeOut(null);
    }

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
