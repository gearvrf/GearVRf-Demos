package com.samsung.accessibility.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorBackEaseIn implements GVRInterpolator {

    private static InterpolatorBackEaseIn sInstance = null;

    public InterpolatorBackEaseIn() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorBackEaseIn getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorBackEaseIn();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float overshoot = 1.70158f;

        return ratio * ratio * ((overshoot + 1) * ratio - overshoot);
    }

}
