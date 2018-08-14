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

package org.gearvrf.arpet.animation;

import android.support.annotation.NonNull;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRTransformAnimation;
import org.gearvrf.arpet.AnchoredObject;
import org.joml.Vector3f;

/**
 * Custom class animation to move a {@link AnchoredObject} to a given position
 */
public class CustomPositionAnimation<T extends AnchoredObject> extends GVRTransformAnimation {

    private static final int POSE_X = 12;
    private static final int POSE_Y = 13;
    private static final int POSE_Z = 14;

    private Vector3f mStart = new Vector3f();
    private Vector3f mDelta = new Vector3f();
    private Vector3f mPosition = new Vector3f();

    private OnPositionAnimationListener mOnAnimationListener;
    private T mObjectToMove;
    private float[] mCurrentPose;
    private boolean mIsRunning;

    public CustomPositionAnimation(@NonNull T objectToMove, Vector3f endPosition, final float duration) {
        super(objectToMove, duration);

        mObjectToMove = objectToMove;
        mCurrentPose = mObjectToMove.getPoseMatrix();
        setEndPosition(endPosition);
        setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                mIsRunning = false;
                if (mOnAnimationListener != null) {
                    mOnAnimationListener.onAnimationEnd();
                }
            }
        });
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {

        if (mOnAnimationListener != null) {
            mPosition.set(
                    mStart.x + ratio * mDelta.x,
                    mStart.y + ratio * mDelta.y,
                    mStart.z + ratio * mDelta.z
            );
            mOnAnimationListener.onAnimate(mPosition);
        }
    }

    public void setOnAnimationListener(OnPositionAnimationListener listener) {
        this.mOnAnimationListener = listener;
    }

    private void setEndPosition(Vector3f end) {
        mStart.set(mCurrentPose[POSE_X], mCurrentPose[POSE_Y], mCurrentPose[POSE_Z]);
        mDelta.set(end.x - mStart.x, end.y - mStart.y, end.z - mStart.z);
    }

    /**
     * Starts the position animation
     */
    public void start() {
        start(GVRAnimationEngine.getInstance(mObjectToMove.getGVRContext()));
    }

    /**
     * Stops this animation
     */
    public void stop() {
        GVRAnimationEngine.getInstance(mObjectToMove.getGVRContext()).stop(this);
        mIsRunning = false;
    }

    @Override
    public GVRAnimation start(GVRAnimationEngine engine) {
        GVRAnimation animation = super.start(engine);
        mIsRunning = true;
        if (mOnAnimationListener != null) {
            mOnAnimationListener.onAnimationStart();
        }
        return animation;
    }

    /**
     * Checks if this animation is running.
     *
     * @return whether this animation is running or not.
     */
    public boolean isRunning() {
        return mIsRunning;
    }
}
