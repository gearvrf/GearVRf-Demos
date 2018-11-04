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

package org.gearvrf.arpet;

import android.support.annotation.NonNull;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.IMRCommon;

/**
 * Class representing a {@link GVRSceneObject} with an {@link GVRAnchor} and its AR pose matrix
 */
public abstract class AnchoredObject extends GVRSceneObject {

    final String TAG = getClass().getSimpleName();

    private GVRAnchor mAnchor;
    private IMRCommon mMixedReality;

    public AnchoredObject(@NonNull GVRContext context, @NonNull IMRCommon mixedReality) {
        super(context);
        this.mMixedReality = mixedReality;
    }

    public boolean updatePose(float[] poseMatrix) {
        mMixedReality.updateAnchorPose(mAnchor, poseMatrix);
        return true;
    }

    public GVRAnchor getAnchor() {
        return mAnchor;
    }

    public IMRCommon getMixedReality() {
        return mMixedReality;
    }

    public void setAnchor(GVRAnchor anchor) {
        if (mAnchor != null) {
            mAnchor.detachSceneObject(this);
            mMixedReality.removeAnchor(mAnchor);
        }
        mAnchor = anchor;
        mAnchor.attachSceneObject(this);
    }

}
