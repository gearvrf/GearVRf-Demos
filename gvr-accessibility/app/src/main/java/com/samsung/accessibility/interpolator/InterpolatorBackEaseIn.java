/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
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
