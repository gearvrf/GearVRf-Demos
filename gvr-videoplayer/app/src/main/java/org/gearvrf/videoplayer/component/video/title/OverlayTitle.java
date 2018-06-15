package org.gearvrf.videoplayer.component.video.title;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.videoplayer.R;

public class OverlayTitle extends GVRSceneObject {

    private static final float ASPECT_RATIO = .1f;

    public OverlayTitle(GVRContext gvrContext, float width) {
        super(gvrContext,
                gvrContext.createQuad(width, ASPECT_RATIO * width),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.ic_app_name_highlight)));
        getRenderData().getMaterial().setOpacity(0.8f);
    }
}
