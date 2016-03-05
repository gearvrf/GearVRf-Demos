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
