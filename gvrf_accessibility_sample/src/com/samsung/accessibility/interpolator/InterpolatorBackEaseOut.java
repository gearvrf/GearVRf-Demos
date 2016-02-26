package com.samsung.accessibility.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorBackEaseOut implements GVRInterpolator {

    private static InterpolatorBackEaseOut sInstance = null;

    public InterpolatorBackEaseOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorBackEaseOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorBackEaseOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float overshoot = 1.70158f;

        return ((ratio = ratio - 1) * ratio * ((overshoot + 1) * ratio + overshoot) + 1);
    }

}
