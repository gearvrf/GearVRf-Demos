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
import android.util.Log;

import org.gearvrf.animation.GVRAccelerateDecelerateInterpolator;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.joml.Vector3f;

/**
 * Class the represents a movement of given anchored object to the screen position.
 *
 * @param <T> The object type will be moved to screen position.
 */
public class ToScreenMovement<T extends AnchoredObject> implements PetMovement {

    private static final String TAG = ToScreenMovement.class.getSimpleName();

    private static final float CAMERA_SHIFT_THRESHOLD = 0.05f;

    private static final int POSE_X = 12;
    private static final int POSE_Y = 13;
    private static final int POSE_Z = 14;

    private T mObjectToMove;
    private OnPetMovementListener mPetMovementListener;
    private CustomPositionAnimation mAnimation;
    private GVRMixedReality mMixedReality;

    private float mStartY; // Holds object's Y position fixed during animation
    private float mCameraDisplacement;
    private float mDistanceToTarget;
    private float mAnimationDuration;
    private float[] mCameraPose;

    private Vector3f mObjectPosition = new Vector3f();
    private Vector3f mPreviousCameraPosition = new Vector3f();
    private Vector3f mCameraPosition = new Vector3f();

    ToScreenMovement(@NonNull T objectToMove, @NonNull final GVRMixedReality mixedReality) {

        mObjectToMove = objectToMove;
        mMixedReality = mixedReality;

        updatePositionHolders();
        getPositionFromPose(mPreviousCameraPosition, mCameraPose);
    }

    public void setPetMovementListener(OnPetMovementListener listener) {
        this.mPetMovementListener = listener;
    }

    @Override
    public void move() {
        PetActivity.PetContext.INSTANCE.runOnPetThread(new Runnable() {
            @Override
            public void run() {
                if (mAnimation != null) {
                    mAnimation.stop();
                }
                initializeAnimation();
                Log.d(TAG, "run: Movement stopped!");
                mStartY = mObjectPosition.y;
                startMoveDelayed();
            }
        });
    }

    private void startMoveDelayed() {

        Log.d(TAG, "initialPosition: " + mCameraPosition);
        PetActivity.PetContext.INSTANCE.runDelayedOnPetThread(new Runnable() {
            @Override
            public void run() {
                mAnimation.start();
                if (mPetMovementListener != null) {
                    mPetMovementListener.onStartMove();
                }
                Log.d(TAG, "run: Movement started!");
            }
        }, 500);
    }

    public boolean isMoving() {
        return mAnimation.isRunning();
    }

    private void getPositionFromPose(Vector3f out, float[] pose) {
        out.x = pose[POSE_X];
        out.y = pose[POSE_Y];
        out.z = pose[POSE_Z];
    }

    private void updatePositionHolders() {

        mCameraPose = mMixedReality.getCameraPoseMatrix();

        getPositionFromPose(mCameraPosition, mCameraPose);
        getPositionFromPose(mObjectPosition, mObjectToMove.getPoseMatrix());

        mCameraDisplacement = mCameraPosition.distance(mPreviousCameraPosition);
        mDistanceToTarget = mCameraPosition.distance(mObjectPosition);
    }

    private void checkCameraDisplacementThreshold() {

        if (mCameraDisplacement > CAMERA_SHIFT_THRESHOLD) {
            Log.d(TAG, "checkCameraDisplacementThreshold: the camera's displacement threshold has been reached!");
            synchronized (this) {
                mPreviousCameraPosition.set(mCameraPosition);
                resetMovement();
            }
        }
    }

    private void resetMovement() {
        mAnimation.stop();
        initializeAnimation();
        mAnimation.start();
    }

    private void initializeAnimation() {
        mAnimation = new CustomPositionAnimation<>(mObjectToMove, mCameraPosition, mAnimationDuration = calculateDuration());
        mAnimation.setInterpolator(GVRAccelerateDecelerateInterpolator.getInstance());
        mAnimation.setOnAnimationListener(mAnimationListener);
    }

    private float calculateDuration() {
        return 6 * (mDistanceToTarget / 0.650f);
    }

    private void printStatus() {
        Log.d(TAG, String.format("onAnimate / ObjectPosition: %s / CameraPosition= %s / DistanceToTarget= %.3f / CameraDisplacement= %.3f / AnimDuration= %.3f",
                mObjectPosition, mCameraPosition, mDistanceToTarget, mCameraDisplacement, mAnimationDuration));
    }

    private OnPositionAnimationListener mAnimationListener = new OnPositionAnimationListener() {
        @Override
        public void onAnimationStart() {
            Log.d(TAG, "onAnimationStart: ");
            if (mPetMovementListener != null) {
                mPetMovementListener.onStartMove();
            }
        }

        @Override
        public void onAnimate(float x, float y, float z) {
            updatePositionHolders();
            checkCameraDisplacementThreshold();
            printStatus();
            if (mPetMovementListener != null) {
                // Move keeping Y value fixed in mStartY
                mPetMovementListener.onMove(x, mStartY, z);
            }
        }

        @Override
        public void onAnimationEnd() {
            Log.d(TAG, "onAnimationEnd: ");
            if (mPetMovementListener != null) {
                mPetMovementListener.onStopMove();
            }
        }
    };

}
