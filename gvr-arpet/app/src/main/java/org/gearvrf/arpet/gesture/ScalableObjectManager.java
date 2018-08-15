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

package org.gearvrf.arpet.gesture;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ScalableObjectManager {

    INSTANCE;

    private List<ScalableObject> mScalableObjects = new ArrayList<>();

    public void addScalableObject(@NonNull ScalableObject... objects) {
        mScalableObjects.addAll(Arrays.asList(objects));
    }

    public void applyScale(float factor) {
        for (ScalableObject object : mScalableObjects) {
            object.scale(factor);
        }
    }

    public void setAutoScaleObjectsFrom(ScalableObject scalableObject) {
        scalableObject.addOnScaleListener(mOnScaleListener);
    }

    public List<ScalableObject> getScalableObjects() {
        return new ArrayList<>(mScalableObjects);
    }

    private OnScaleListener mOnScaleListener = new OnScaleListener() {
        @Override
        public void onScale(float factor) {
            applyScale(factor);
        }
    };

    public <T extends ScalableObject> ScalableObject getObjectByType(Class<T> type) {
        for (ScalableObject scalableObject : mScalableObjects) {
            if (scalableObject.getClass() == type) {
                return scalableObject;
            }
        }
        return null;
    }
}
