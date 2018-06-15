package org.gearvrf.videoplayer.component.video.light;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPointLight;

public class LightScene extends GVRPointLight {
    public LightScene(GVRContext ctx) {
        super(ctx);
        setDiffuseIntensity(0.55f, 0.55f, 0.55f, 0.55f);
    }
}
