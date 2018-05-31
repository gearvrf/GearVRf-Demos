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
import com.google.ar.core.Pose;

import org.gearvrf.GVRContext;

import java.lang.Math;
import org.joml.Quaternionf;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;


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

    public void setRotation(float x, float y, float z, float w) {
        mRotation = new Quaternionf(x, y, z, w);
    }

    public void setRotationByAxis(float angle, float x, float y, float z) {
        AxisAngle4f axisAngle = new AxisAngle4f(angle, x, y, z);
        mRotation = new Quaternionf(axisAngle);
    }

    public float getRotationYaw() {
        Vector3f rotationAngles = new Vector3f();
        mRotation.getEulerAnglesXYZ(rotationAngles);
        return (float)Math.toDegrees(rotationAngles.y);
    }

    Quaternionf mRotation;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private float mScaleZ = 1.0f;

    public void setScale(float x, float y, float z) {
        mScaleX = x;
        mScaleY = y;
        mScaleZ = z;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getScaleZ() {
        return mScaleZ;
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
            Pose finalPose = mARAnchor.getPose();
            /*
            Pose objectPose = Pose.makeRotation(
                    mRotation.x(),
                    mRotation.y(),
                    mRotation.z(),
                    mRotation.w()
                    );
            //Pose finalPose = mARAnchor.getPose().compose(objectPose);
            */

            float finalScale = mScaleX * scale;
        android.util.Log.d("taf", "transform scalex = " + mScaleX);
        android.util.Log.d("taf", "scale = " + scale);
        android.util.Log.d("taf", "finalScale = " + finalScale);
        android.util.Log.d("taf", "===================== ");
            /*
            */

            super.update(finalPose, arViewMatrix, vrCamMatrix, finalScale);
            setEnable(true);
        }

        return isEnabled();
    }
}
