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

package org.gearvrf.arpet.movement;

import android.support.annotation.IntDef;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.arpet.character.CharacterView;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Represents a state of the Character.
 */
public class PetActions {
    private static final String TAG = "CharacterStates";

    @IntDef({IDLE.ID, TO_BALL.ID, TO_PLAYER.ID})
    public @interface Action{
    }

    private static abstract class PetAction implements IPetAction {
        protected final CharacterView mCharacter;
        protected final GVRSceneObject mTarget;
        protected final OnPetActionListener mListener;

        protected Matrix4f mPetMtx = null;
        protected Matrix4f mTargetMtx = null;

        protected final Quaternionf mRotation = new Quaternionf();
        protected final Vector4f mPetDirection = new Vector4f();
        protected final Vector3f mTargetDirection = new Vector3f();
        protected final Vector3f mMoveTo = new Vector3f();

        protected final float mCharacterHalfSize = 25.0f;
        protected final float mTurnSpeed = 0.1f;
        protected final float mWalkingSpeed = 0.02f;
        protected final float mRunningSpeed = 0.04f;
        protected long mElapsedTime = 0;
        protected GVRAnimation mAnimation;

        protected PetAction(CharacterView character, GVRSceneObject target,
                            OnPetActionListener listener) {
            mCharacter = character;
            mTarget = target;
            mListener = listener;
            mAnimation = null;
        }

        public void setAnimation(GVRAnimation animation) {
            mAnimation = animation;
            if (animation != null) {
                mAnimation.reset();
            }
        }

        protected void animate(float frameTime) {
            int duration = (int) mAnimation.getDuration() * 1000;
            mElapsedTime += (frameTime * 1000);
            mElapsedTime = mElapsedTime % duration;
            // mAnimation.animate((float)mElapsedTime / (float)duration);
        }

        @Override
        public void entry() {
            onEntry();
        }

        @Override
        public void exit() {
            onExit();
        }

        @Override
        public void run(float frameTime) {
            GVRTransform petTrans = mCharacter.getTransform();
            // Vector of Character toward to Camera
            mPetMtx = petTrans.getModelMatrix4f();
            mTargetMtx = mTarget.getTransform().getModelMatrix4f();

            /* Pet world space vector */
            mPetDirection.set(0, 0, 1, 0);
            mPetDirection.mul(mPetMtx);
            mPetDirection.normalize();

            mTargetDirection.set(mTargetMtx.m30(), mTargetMtx.m31(), mTargetMtx.m32());
            mTargetDirection.sub(mPetMtx.m30(), mPetMtx.m31(), mPetMtx.m32());

            /* Target direction to look at */
            mMoveTo.set(mTargetDirection);
            mMoveTo.normalize();
            // Speed vector to create a smooth rotation
            mMoveTo.mul(mTurnSpeed);
            mMoveTo.add(mPetDirection.x, 0, mPetDirection.z);
            mMoveTo.normalize();

            // Calc the rotation toward the camera and put it in pRot
            mRotation.rotationTo(mPetDirection.x, 0, mPetDirection.z,
                    mMoveTo.x, 0, mMoveTo.z);

            petTrans.rotate(mRotation.w, mRotation.x, mRotation.y, mRotation.z);

            onRun(frameTime);
        }

        protected abstract void onEntry();

        protected abstract void onExit();

        protected abstract void onRun(float fimeTime);
    }

    public static class IDLE extends PetAction {
        public static final int ID = 0;

        public IDLE(CharacterView character, GVRSceneObject player) {
            super(character, player, null);
            setAnimation(character.getAnimation(1));
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => IDLE");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => IDLE");
        }

        @Override
        public void onRun(float frameTime) {
            if (mAnimation != null) {
                //animate(frameTime);
                //cTrans.setPosition(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
            }
        }
    }

    public static class TO_PLAYER extends PetAction {
        public static final int ID = 1;

        public TO_PLAYER(CharacterView character, GVRSceneObject player,
                         OnPetActionListener listener) {
            super(character, player, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => MOVING_TO_CAMERA");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => MOVING_TO_CAMERA");
        }

        @Override
        public void onRun(float frameTime) {
            mTargetDirection.y = 0;
            // Keep a angle of 45 degree of distance
            boolean moveTowardToCam = mTargetDirection.length() > 2 * mCharacterHalfSize;

            if (moveTowardToCam) {
                mRotation.rotationTo(mPetDirection.x, 0, mPetDirection.z,
                        mTargetDirection.x, 0, mTargetDirection.z);

                if (mRotation.angle() < Math.PI * 0.25f) {
                    // acceleration logic
                    float[] pose = mCharacter.getAnchor().getPose();
                    mMoveTo.mul(mWalkingSpeed);

                    pose[12] = pose[12] + mMoveTo.x;
                    pose[14] = pose[14] + mMoveTo.z;

                    mCharacter.updatePose(pose);
                }
            } else {
                mListener.onActionEnd(this);
            }
        }
    }

    public static class TO_BALL extends PetAction {
        public static final int ID = 2;

        public TO_BALL(CharacterView character, GVRSceneObject target,
                       OnPetActionListener listener) {
            super(character, target, listener);
        }

        @Override
        public int id() {
            return ID;
        }

        @Override
        public void onEntry() {
            Log.w(TAG, "entry => MOVING_TO_BALL");
        }

        @Override
        public void onExit() {
            Log.w(TAG, "exit => MOVING_TO_BALL");
        }

        @Override
        public void onRun(float frameTime) {

            // Min distance to ball
            boolean moveTowardToBall = mTargetDirection.length() > mCharacterHalfSize * 1.1f;

            if (moveTowardToBall) {
                mRotation.rotationTo(mPetDirection.x, 0, mPetDirection.z,
                        mTargetDirection.x, 0, mTargetDirection.z);

                if (mRotation.angle() < Math.PI * 0.25f) {
                    float[] pose = mCharacter.getAnchor().getPose();
                    // TODO: Create pose
                    mMoveTo.mul(mRunningSpeed);

                    pose[12] = pose[12] + mMoveTo.x;
                    pose[14] = pose[14] + mMoveTo.z;

                    mCharacter.updatePose(pose);
                }
            } else {
                mListener.onActionEnd(this);
            }
        }
    }
}
