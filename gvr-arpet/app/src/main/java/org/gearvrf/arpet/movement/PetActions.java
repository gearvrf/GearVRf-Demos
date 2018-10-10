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
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
        protected final OnPetActionListener mListener;
        protected final float mCharacterHalfSize = 25.0f;
        protected final float mTurnSpeed = 0.1f;
        protected final float mWalkingSpeed = 0.02f;
        protected final float mRunningSpeed = 0.04f;
        protected long mElapsedTime = 0;
        protected GVRAnimation mAnimation;
        protected GVRSceneObject mTarget;

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

        // p prefix for parent and c prefix for child
        private Quaternionf pRot = new Quaternionf();
        private Quaternionf cRot = new Quaternionf();

        private Vector3f mLookToward = new Vector3f();
        private Vector3f mLookAt = new Vector3f();

        @Override
        public void onRun(float frameTime) {
            GVRTransform pTrans = mCharacter.getParent().getTransform();
            GVRTransform cTrans = mCharacter.getTransform();
            pRot.set(pTrans.getRotationX(), pTrans.getRotationY(),
                    pTrans.getRotationZ(), pTrans.getRotationW());
            cRot.set(cTrans.getRotationX(), cTrans.getRotationY(),
                    cTrans.getRotationZ(), cTrans.getRotationW());

            // Character toward to
            mLookToward.set(0, 0, 1);
            // Set the global rotation
            mLookToward.rotate(pRot);
            mLookToward.rotate(cRot);
            mLookToward.normalize();
            // Remove y vector
            mLookToward.y = 0;

            // Vector of Character toward to Camera
            float[] modelCharacter = mCharacter.getTransform().getModelMatrix();
            float[] modelCam = mTarget.getTransform().getModelMatrix();
            mLookAt.set(modelCam[12], modelCam[13], modelCam[14]);
            mLookAt.sub(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
            float y = mLookAt.y;
            // Remove y vector
            mLookAt.y = 0;

            // Keep a angle of 45 degree of distance
            //boolean moveTowardToCam = mLookAt.length() > (y * Math.tan(Math.PI * 0.25));
            // Normalize after calc the distance
            mLookAt.normalize();

            // Speed vector to create a smooth rotation
            mLookAt.mul(mTurnSpeed);
            mLookAt.add(mLookToward);
            mLookAt.normalize();
            // Calc the rotation toward the camera and put it in pRot
            mLookToward.rotationTo(mLookAt, pRot);
            // Multiply by character rotation.
            cRot.mul(pRot);
            // Set the new rotation to the Character
            cTrans.setRotation(cRot.w, cRot.x, cRot.y, cRot.z);

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

        // p prefix for parent and c prefix for child
        private Quaternionf pRot = new Quaternionf();
        private Quaternionf cRot = new Quaternionf();

        private Vector3f mLookToward = new Vector3f();
        private Vector3f mLookAt = new Vector3f();

        @Override
        public void onRun(float frameTime) {
            GVRTransform pTrans = mCharacter.getParent().getTransform();
            GVRTransform cTrans = mCharacter.getTransform();
            pRot.set(pTrans.getRotationX(), pTrans.getRotationY(),
                    pTrans.getRotationZ(), pTrans.getRotationW());
            cRot.set(cTrans.getRotationX(), cTrans.getRotationY(),
                    cTrans.getRotationZ(), cTrans.getRotationW());

            // Character toward to
            mLookToward.set(0, 0, 1);
            // Set the global rotation
            mLookToward.rotate(pRot);
            mLookToward.rotate(cRot);
            mLookToward.normalize();
            // Remove y vector
            mLookToward.y = 0;

            // Vector of Character toward to Camera
            float[] modelCharacter = mCharacter.getTransform().getModelMatrix();
            float[] modelCam = mTarget.getTransform().getModelMatrix();
            mLookAt.set(modelCam[12], modelCam[13], modelCam[14]);
            mLookAt.sub(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
            float y = mLookAt.y;
            // Remove y vector
            mLookAt.y = 0;

            // Keep a angle of 45 degree of distance
            boolean moveTowardToCam = mLookAt.length() > (y * Math.tan(Math.PI * 0.25));

            if (moveTowardToCam) {
                // Total angle difference
                mLookToward.rotationTo(mLookAt, pRot);
                float angle = pRot.angle();
                // Normalize after calc the distance
                mLookAt.normalize();

                // Speed vector to create a smooth rotation
                mLookAt.mul(mTurnSpeed);
                mLookAt.add(mLookToward);
                mLookAt.normalize();
                // Calc the rotation toward the camera and put it in pRot
                mLookToward.rotationTo(mLookAt, pRot);
                // Multiply by character rotation.
                cRot.mul(pRot);
                // Set the new rotation to the Character
                mCharacter.getTransform().setRotation(cRot.w, cRot.x, cRot.y, cRot.z);

                if (angle < Math.PI / 4.0f) {
                    // acceleration logic
                    float[] pose = mCharacter.getAnchor().getPose();
                    mLookAt.mul(mWalkingSpeed);

                    pose[12] = pose[12] + mLookAt.x;
                    pose[14] = pose[14] + mLookAt.z;

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

        // p prefix for parent and c prefix for child
        private Quaternionf pRot = new Quaternionf();
        private Quaternionf cRot = new Quaternionf();

        private Vector3f mLookToward = new Vector3f();
        private Vector3f mLookAt = new Vector3f();

        @Override
        public void onRun(float frameTime) {
            GVRTransform pTrans = mCharacter.getParent().getTransform();
            GVRTransform cTrans = mCharacter.getTransform();
            pRot.set(pTrans.getRotationX(), pTrans.getRotationY(),
                    pTrans.getRotationZ(), pTrans.getRotationW());
            cRot.set(cTrans.getRotationX(), cTrans.getRotationY(),
                    cTrans.getRotationZ(), cTrans.getRotationW());

            // Character toward to
            mLookToward.set(0, 0, 1);
            // Set the global rotation
            mLookToward.rotate(pRot);
            mLookToward.rotate(cRot);
            mLookToward.normalize();
            // Remove y vector
            mLookToward.y = 0;

            // Vector of Character toward to ball
            float[] modelCharacter = mCharacter.getTransform().getModelMatrix();
            float[] modelCam = mTarget.getTransform().getModelMatrix();
            mLookAt.set(modelCam[12], modelCam[13], modelCam[14]);
            mLookAt.sub(modelCharacter[12], modelCharacter[13] + mCharacterHalfSize, // Center
                    modelCharacter[14]);

            // Min distance to ball
            boolean moveTowardToBall = mLookAt.length() > mCharacterHalfSize * 1.1f;

            if (moveTowardToBall) {
                // Remove y vector
                mLookAt.y = 0;
                // Total angle difference
                mLookToward.rotationTo(mLookAt, pRot);
                float angle = pRot.angle();
                // Normalize after calc the distance
                mLookAt.normalize();

                // Speed vector to create a smooth rotation
                mLookAt.mul(mTurnSpeed);
                mLookAt.add(mLookToward);
                mLookAt.normalize();
                // Calc the rotation toward the camera and put it in pRot
                mLookToward.rotationTo(mLookAt, pRot);
                // Multiply by character rotation.
                cRot.mul(pRot);
                // Set the new rotation to the Character
                mCharacter.getTransform().setRotation(cRot.w, cRot.x, cRot.y, cRot.z);

                if (angle < Math.PI / 4.0f) {
                    float[] pose = mCharacter.getAnchor().getPose();
                    // TODO: Create pose
                    mLookAt.mul(mRunningSpeed);

                    pose[12] = pose[12] + mLookAt.x;
                    pose[14] = pose[14] + mLookAt.z;

                    mCharacter.updatePose(pose);
                }
            } else {
                mListener.onActionEnd(this);
            }
        }
    }
}
