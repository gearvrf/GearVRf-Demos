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

package org.gearvrf.arpet.movement.toscreen;

import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.animation.GVRAccelerateDecelerateInterpolator;
import org.gearvrf.arpet.PetActivity;
import org.gearvrf.arpet.animation.CustomPositionAnimation;
import org.gearvrf.arpet.animation.OnPositionAnimationListener;
import org.gearvrf.arpet.movement.BaseMovement;
import org.gearvrf.arpet.movement.MovableObject;
import org.gearvrf.arpet.movement.OnMovementListener;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.joml.Vector3f;

/**
 * Class the represents a movement of given anchored object to the screen position.
 */
public class ToScreenMovement<Movable extends MovableObject>
        extends BaseMovement<Movable, OnMovementListener<Movable, ToScreenMovementPosition>> {

    private static final String TAG = ToScreenMovement.class.getSimpleName();

    private static final float CAMERA_DISPLACEMENT_THRESHOLD = 0.04f;

    private static final int POSE_X = 12;
    private static final int POSE_Y = 13;
    private static final int POSE_Z = 14;

    private CustomPositionAnimation mAnimation;
    private GVRMixedReality mMixedReality;
    private GVRPlane mBoundaryPlane;

    private float mCameraDisplacement;
    private float mDistanceToTarget;
    private float mAnimationDuration;
    private float[] mCameraPose;
    private float[] mObjectPose;
    private float[] mBoundaryPlaneCenterPose = new float[16];

    private Vector3f mObjectPosition = new Vector3f();
    private Vector3f mPreviousCameraPosition = new Vector3f();
    private Vector3f mCameraPosition = new Vector3f();

    /**
     * @param objectToMove The object to be moved.
     * @param mixedReality The mixed reality session.
     */
    public ToScreenMovement(@NonNull Movable objectToMove,
                            @NonNull final GVRMixedReality mixedReality,
                            OnMovementListener<Movable, ToScreenMovementPosition> listener) {
        super(objectToMove, listener);

        mMixedReality = mixedReality;

        updatePositionHolders();
        getPositionFromPose(mPreviousCameraPosition, mCameraPose);
    }

    /**
     * Stops running movement then starts a new movement.
     */
    @Override
    public void move() {
        if (mAnimation != null) {
            mAnimation.stop();
        }
        initializeAnimation();
        Log.d(TAG, "run: Movement stopped!");
        startMoveDelayed();
    }

    @Override
    public void stop() {
        if (mAnimation != null) {
            mAnimation.stop();
            mAnimation = null;
        }
    }

    @Override
    public boolean isMoving() {
        return mAnimation.isRunning();
    }

    private void startMoveDelayed() {

        Log.d(TAG, "initialPosition: " + mCameraPosition);
        PetActivity.PetContext.INSTANCE.runDelayedOnPetThread(new Runnable() {
            @Override
            public void run() {
                mAnimation.start();
                if (mOnMovementListener != null) {
                    mOnMovementListener.onStartMove();
                }
                Log.d(TAG, "run: Movement started!");
            }
        }, 500);
    }

    private void getPositionFromPose(Vector3f out, float[] pose) {
        out.x = pose[POSE_X];
        out.y = pose[POSE_Y];
        out.z = pose[POSE_Z];
    }

    private void updatePositionHolders() {

        mCameraPose = mMixedReality.getCameraPoseMatrix();
        mObjectPose = mMovable.getPoseMatrix();

        getPositionFromPose(mCameraPosition, mCameraPose);
        getPositionFromPose(mObjectPosition, mObjectPose);

        mCameraDisplacement = mCameraPosition.distance(mPreviousCameraPosition);
        mDistanceToTarget = mCameraPosition.distance(mObjectPosition);
    }

    private void checkCameraDisplacementThreshold() {

        if (mCameraDisplacement > CAMERA_DISPLACEMENT_THRESHOLD) {
            Log.d(TAG, "checkCameraDisplacementThreshold: the camera's displacement threshold has been reached!");
            synchronized (this) {
                mPreviousCameraPosition.set(mCameraPosition);
                resetMovement();
            }
        }
    }

    private void checkBoundaryPlane() {

        if (mBoundaryPlane != null) {

            mBoundaryPlane.getCenterPose(mBoundaryPlaneCenterPose);
            mObjectPose[POSE_Y] = mBoundaryPlaneCenterPose[POSE_Y];

            if (!mBoundaryPlane.isPoseInPolygon(mObjectPose)) {
                mAnimation.stop();
                if (mOnMovementListener != null) {
                    mOnMovementListener.onStopMove();
                }
            }
        }
    }

    private void resetMovement() {
        stop();
        initializeAnimation();
        mAnimation.start();
    }

    private void initializeAnimation() {
        mAnimation = new CustomPositionAnimation<>(mMovable, mCameraPosition, mAnimationDuration = calculateDuration());
        mAnimation.setInterpolator(GVRAccelerateDecelerateInterpolator.getInstance());
        mAnimation.setOnAnimationListener(mAnimationListener);
    }

    private float calculateDuration() {
        return 6 * (mDistanceToTarget / 0.650f);
    }

    private void printStatus() {
        Log.d(TAG, String.format("ObjectPosition: %s / CameraPosition= %s / DistanceToTarget= %.3f / CameraDisplacement= %.3f / AnimDuration= %.3f",
                mObjectPosition, mCameraPosition, mDistanceToTarget, mCameraDisplacement, mAnimationDuration));
    }

    /**
     * Sets the boundary for moving the object
     *
     * @param boundary the movement boundary.
     */
    public void setBoundaryPlane(GVRPlane boundary) {
        this.mBoundaryPlane = boundary;
    }

    private OnPositionAnimationListener mAnimationListener = new OnPositionAnimationListener() {

        ToScreenMovementPosition position = new ToScreenMovementPosition();

        @Override
        public void onAnimationStart() {
            Log.d(TAG, "onAnimationStart: ");
            if (mOnMovementListener != null) {
                mOnMovementListener.onStartMove();
            }
        }

        @Override
        public void onAnimate(float x, float y, float z) {
            updatePositionHolders();
            checkCameraDisplacementThreshold();
            printStatus();
            checkBoundaryPlane();
            if (mOnMovementListener != null) {
                // Move keeping Y value fixed in mStartY
                position.getValue().set(x, mObjectPosition.y, z);
                mOnMovementListener.onMove(mMovable, position);
            }
        }

        @Override
        public void onAnimationEnd() {
            Log.d(TAG, "onAnimationEnd: ");
            if (mOnMovementListener != null) {
                mOnMovementListener.onStopMove();
            }
        }
    };

}
