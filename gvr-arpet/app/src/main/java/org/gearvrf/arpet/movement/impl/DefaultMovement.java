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

package org.gearvrf.arpet.movement.impl;

import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.animation.GVRAccelerateDecelerateInterpolator;
import org.gearvrf.arpet.PetActivity;
import org.gearvrf.arpet.animation.CustomPositionAnimation;
import org.gearvrf.arpet.animation.OnPositionAnimationListener;
import org.gearvrf.arpet.movement.MovableObject;
import org.gearvrf.arpet.movement.Movement;
import org.gearvrf.arpet.movement.OnMovementListener;
import org.gearvrf.arpet.movement.TargetObject;
import org.gearvrf.mixedreality.GVRPlane;
import org.joml.Vector3f;

public class DefaultMovement<
        Movable extends MovableObject,
        Target extends TargetObject,
        Listener extends OnMovementListener<Movable, Vector3f>>
        extends Movement<Movable, Target, Listener> {

    private static final String TAG = DefaultMovement.class.getSimpleName();

    private static final float TARGET_DISPLACEMENT_THRESHOLD = 0.04f;

    private static final int POSE_X = 12;
    private static final int POSE_Y = 13;
    private static final int POSE_Z = 14;

    private CustomPositionAnimation mAnimation;
    private GVRPlane mBoundaryPlane;

    private float mTargetDisplacement;
    private float mDistanceToTarget;
    private float mAnimationDuration;

    private float[] mTargetPose;
    private float[] mObjectPose;
    private float[] mBoundaryPlaneCenterPose = new float[16];

    private Vector3f mObjectPosition = new Vector3f();
    private Vector3f mPreviousTargetPosition = new Vector3f();
    private Vector3f mTargetPosition = new Vector3f();

    /**
     * @param objectToMove The object to be moved.
     * @param targetObject The target object.
     */
    DefaultMovement(@NonNull Movable objectToMove,
                    @NonNull Target targetObject,
                    @NonNull Listener listener) {
        super(objectToMove, targetObject, listener);

        updatePositionHolders();
        getPositionFromPose(mPreviousTargetPosition, mTargetPose);
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

        Log.d(TAG, "initialPosition: " + mTargetPosition);
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

        mObjectPose = mMovable.getPoseMatrix();
        mTargetPose = mTarget.getPoseMatrix();

        getPositionFromPose(mObjectPosition, mObjectPose);
        getPositionFromPose(mTargetPosition, mTargetPose);

        mTargetDisplacement = mTargetPosition.distance(mPreviousTargetPosition);
        mDistanceToTarget = mTargetPosition.distance(mObjectPosition);
    }

    private void checkTargetDisplacementThreshold() {

        if (mTargetDisplacement > TARGET_DISPLACEMENT_THRESHOLD) {
            Log.d(TAG, "checkTargetDisplacementThreshold: the target's displacement threshold has been reached!");
            synchronized (this) {
                mPreviousTargetPosition.set(mTargetPosition);
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
        mAnimation = new CustomPositionAnimation<>(mMovable, mTargetPosition, mAnimationDuration = calculateDuration());
        mAnimation.setInterpolator(GVRAccelerateDecelerateInterpolator.getInstance());
        mAnimation.setOnAnimationListener(mAnimationListener);
    }

    private float calculateDuration() {
        return 6 * (mDistanceToTarget / 0.650f);
    }

    private void printStatus() {
        Log.d(TAG, String.format("ObjectPosition: %s / TargetPosition= %s / DistanceToTarget= %.3f / TargetDisplacement= %.3f / AnimDuration= %.3f",
                mObjectPosition, mTargetPosition, mDistanceToTarget, mTargetDisplacement, mAnimationDuration));
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

        Vector3f mPosition = new Vector3f();

        @Override

        public void onAnimationStart() {
            Log.d(TAG, "onAnimationStart: ");
            if (mOnMovementListener != null) {
                mOnMovementListener.onStartMove();
            }
        }

        @Override
        public void onAnimate(Vector3f position) {
            updatePositionHolders();
            checkTargetDisplacementThreshold();
            printStatus();
            checkBoundaryPlane();
            if (mOnMovementListener != null) {
                mPosition.set(position);
                mPosition.y = mObjectPosition.y;
                mOnMovementListener.onMove(mMovable, mPosition);
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
