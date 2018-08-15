/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.petobjects;

import android.support.annotation.NonNull;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.gesture.OnScaleListener;
import org.gearvrf.arpet.gesture.ScalableObject;
import org.gearvrf.mixedreality.GVRMixedReality;

import java.util.ArrayList;
import java.util.List;

public abstract class AnchoredScalableObject extends AnchoredObject implements ScalableObject {

    private List<OnScaleListener> mOnScaleListeners = new ArrayList<>();

    public AnchoredScalableObject(@NonNull GVRContext context, @NonNull GVRMixedReality mixedReality, @NonNull float[] poseMatrix) {
        super(context, mixedReality, poseMatrix);
    }

    @Override
    public void scale(float factor) {
        GVRTransform t = getTransform();
        t.setScale(factor, factor, factor);
        notifyScale(factor);
    }

    @Override
    public synchronized void addOnScaleListener(OnScaleListener listener) {
        if (!mOnScaleListeners.contains(listener)) {
            mOnScaleListeners.add(listener);
        }
    }

    private synchronized void notifyScale(float factor) {
        for (OnScaleListener listener : mOnScaleListeners) {
            listener.onScale(factor);
        }
    }
}
