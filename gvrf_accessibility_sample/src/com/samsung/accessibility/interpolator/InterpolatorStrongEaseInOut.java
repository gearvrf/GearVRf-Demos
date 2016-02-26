package com.samsung.accessibility.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorStrongEaseInOut implements GVRInterpolator {

    private static InterpolatorStrongEaseInOut sInstance = null;

    public InterpolatorStrongEaseInOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorStrongEaseInOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorStrongEaseInOut();
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
