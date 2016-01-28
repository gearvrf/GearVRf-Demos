
package com.samsung.accessibility;

import org.gearvrf.animation.GVRInterpolator;

final class GVRAccessibilityInterpolatorStrongEaseInOut implements GVRInterpolator {

    private static GVRAccessibilityInterpolatorStrongEaseInOut sInstance = null;

    public GVRAccessibilityInterpolatorStrongEaseInOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized GVRAccessibilityInterpolatorStrongEaseInOut getInstance() {
        if (sInstance == null) {
            sInstance = new GVRAccessibilityInterpolatorStrongEaseInOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float r = (ratio < 0.5) ? ratio * 2 : (1 - ratio) * 2;
        r *= r * r * r * r;
        return (ratio < 0.5) ? r / 2 : 1 - (r / 2);
    }

}
