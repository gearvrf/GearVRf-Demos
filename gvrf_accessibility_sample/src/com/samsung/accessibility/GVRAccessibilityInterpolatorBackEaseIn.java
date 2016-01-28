
package com.samsung.accessibility;

import org.gearvrf.animation.GVRInterpolator;

final class GVRAccessibilityInterpolatorBackEaseIn implements GVRInterpolator {

    private static GVRAccessibilityInterpolatorBackEaseIn sInstance = null;

    public GVRAccessibilityInterpolatorBackEaseIn() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized GVRAccessibilityInterpolatorBackEaseIn getInstance() {
        if (sInstance == null) {
            sInstance = new GVRAccessibilityInterpolatorBackEaseIn();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float overshoot = 1.70158f;

        return ratio * ratio * ((overshoot + 1) * ratio - overshoot);
    }

}
