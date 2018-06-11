package org.gearvrf.videoplayer.component;

import android.support.annotation.NonNull;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class DefaultFadeableObject extends FadeableObject {

    public DefaultFadeableObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    public DefaultFadeableObject(GVRContext mContext, GVRMesh quad, GVRTexture gvrTexture) {
        super(mContext, quad, gvrTexture);
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return this;
    }
}
