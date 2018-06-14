package org.gearvrf.videoplayer.component.video.title;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.videoplayer.R;

public class OverlayTitle extends GVRSceneObject{
    public OverlayTitle(GVRContext gvrContext, float widht, float height) {
        super(gvrContext,
                gvrContext.createQuad(widht, .1f * widht),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.ic_title)));
    }

}
