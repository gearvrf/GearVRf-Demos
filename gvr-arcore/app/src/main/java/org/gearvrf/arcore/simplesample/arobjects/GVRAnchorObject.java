/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.arcore.simplesample.arobjects;

import com.google.ar.core.Anchor;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;

/**
 * Represents a ARCore anchor in the scene.
 *
 * Add anchored objects as child of GVRAnchorObject instance.
 */
public class GVRAnchorObject extends GVRPoseObject {
    private Anchor mARAnchor;

    public GVRAnchorObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * Sets ARCore anchor
     *
     * @param anchor ARCore Anchor instance
     */
    public void setARAnchor(Anchor anchor) {
        if (mARAnchor != null) {
            mARAnchor.detach();
        }
        mARAnchor = anchor;

        setEnable(anchor != null && anchor.getTrackingState() == TrackingState.TRACKING);
    }

    /**
     * @return ARCore Anchor instance
     */
    public Anchor getARAnchor() {
        return mARAnchor;
    }

    /**
     * Converts from ARCore world space to GVRf's world space.
     *
     * @param arViewMatrix Phone's camera view matrix.
     * @param vrCamMatrix GVRf Camera matrix.
     * @param scale Scale from AR to GVRf world.
     * @return True whether success, otherwise false.
     */
    public boolean update(float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        if (mARAnchor == null) {
            setEnable(false);
            return false;
        }

        if (mARAnchor.getTrackingState() != TrackingState.TRACKING) {
            mARAnchor.detach();
            mARAnchor = null;
            setEnable(false);
        } else {
            super.update(mARAnchor.getPose(), arViewMatrix, vrCamMatrix, scale);
            setEnable(true);
        }

        return isEnabled();
    }
}
