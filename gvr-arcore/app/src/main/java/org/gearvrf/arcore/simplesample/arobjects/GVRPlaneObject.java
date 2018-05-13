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

import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;

/**
 * Represents a ARCore plane in the scene.
 *
 * Add your custom plane object as child of GVRPlaneObject instance.
 */
public class GVRPlaneObject extends GVRPoseObject {
    private Plane mARPlane;

    public GVRPlaneObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * Sets ARCore plane
     *
     * @param plane ARCore Plane instance
     */
    public void setARPlane(Plane plane) {
        mARPlane = plane;

        setEnable(plane != null && plane.getTrackingState() == TrackingState.TRACKING
                && plane.getSubsumedBy() == null);
    }

    /**
     * @return ARCore Plane instance
     */
    public Plane getARPlane() {
        return mARPlane;
    }

    /**
     * Converts from ARCore world space to GVRf's world space.
     *
     * @param arViewMatrix Phone's camera view matrix
     * @param vrCamMatrix GVRf Camera matrix
     * @param scale Scale from AR to GVRf world
     * @return True whether success, otherwise false.
     */
    public boolean update(float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        if (mARPlane == null) {
            setEnable(false);
            return false;
        }

        if (mARPlane.getTrackingState() != TrackingState.TRACKING
                || mARPlane.getSubsumedBy() != null) {
            mARPlane = null;
            setEnable(false);
        } else {
            super.update(mARPlane.getCenterPose(), arViewMatrix, vrCamMatrix, scale);
            setEnable(true);
        }

        return isEnabled();
    }
}
