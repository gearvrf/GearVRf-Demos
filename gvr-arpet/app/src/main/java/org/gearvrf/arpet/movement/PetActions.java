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

import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Represents a state of the Character.
 */
public class PetActions {
    private static final String TAG = "CharacterStates";

    private static abstract class PetAction implements IPetAction {
        protected final OnPetActionListener mListener;

        protected PetAction(OnPetActionListener listener) {
            mListener = listener;
        }
    }

    public static class IDLE extends PetAction {
        public static final int ID = 0;

        private final AnchoredObject mCharacter;
        private final GVRTransform mTargetTransform;
        private float mTurnSpeed = 0.05f;

        public IDLE(AnchoredObject character, GVRTransform target) {
            super(null);

            mCharacter = character;
            mTargetTransform = target;
        }

        @Override
        public int id() { return ID; }

        @Override
        public void entry() {
            Log.w(TAG, "entry => IDLE");
        }

        @Override
        public void exit() {
            Log.w(TAG, "exit => IDLE");
        }

        // p prefix for parent and c prefix for child
        private Quaternionf pRot = new Quaternionf();
        private Quaternionf cRot = new Quaternionf();

        private Vector3f mLookToward = new Vector3f();
        private Vector3f mLookAt = new Vector3f();

        @Override
        public void run() {
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
            float[] modelCam = mTargetTransform.getModelMatrix();
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
            mCharacter.getTransform().setRotation(cRot.w, cRot.x, cRot.y, cRot.z);
        }
    }

    public static class TO_CAMERA extends PetAction {
        public static final int ID = 1;
        private final AnchoredObject mCharacter;
        private final GVRTransform mTargetTransform;
        private float mTurnSpeed = 0.05f;
        private float mWalkSpeed = 0.005f;

        public TO_CAMERA(AnchoredObject character, GVRTransform target,
                         OnPetActionListener listener) {
            super(listener);

            mCharacter = character;
            mTargetTransform = target;
        }

        @Override
        public int id() { return ID; }

        @Override
        public void entry() {
            Log.w(TAG, "entry => MOVING_TO_CAMERA");
        }

        @Override
        public void exit() {
            Log.w(TAG, "exit => MOVING_TO_CAMERA");
        }

        // p prefix for parent and c prefix for child
        private Quaternionf pRot = new Quaternionf();
        private Quaternionf cRot = new Quaternionf();

        private Vector3f mLookToward = new Vector3f();
        private Vector3f mLookAt = new Vector3f();

        @Override
        public void run() {
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
            float[] modelCam = mTargetTransform.getModelMatrix();
            mLookAt.set(modelCam[12], modelCam[13], modelCam[14]);
            mLookAt.sub(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
            float y = mLookAt.y;
            // Remove y vector
            mLookAt.y = 0;

            // Keep a angle of 45 degree of distance
            boolean moveTowardToCam = mLookAt.length() > (y * Math.tan(Math.PI * 0.25));
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

            if (moveTowardToCam) {
                float[] pose = mCharacter.getAnchor().getPose();
                // TODO: Create pose
                mLookAt.mul(mWalkSpeed);

                pose[12] = pose[12] + mLookAt.x;
                pose[14] = pose[14] + mLookAt.z;

                mCharacter.updatePose(pose);
            } else {
                mListener.onActionEnd(this);
            }
        }
    }

    public static class TO_BALL extends PetAction {
        public static final int ID = 2;
        private final AnchoredObject mCharacter;
        private final GVRTransform mTargetTransform;
        private float mTurnSpeed = 0.05f;
        private float mWalkSpeed = 0.005f;

        public TO_BALL(AnchoredObject character, GVRTransform target,
                       OnPetActionListener listener) {
            super(listener);

            mCharacter = character;
            mTargetTransform = target;
        }

        @Override
        public int id() { return ID; }

        @Override
        public void entry() {
            Log.w(TAG, "entry => MOVING_TO_BALL");
        }

        @Override
        public void exit() {
            Log.w(TAG, "exit => MOVING_TO_BALL");
        }

        // p prefix for parent and c prefix for child
        private Quaternionf pRot = new Quaternionf();
        private Quaternionf cRot = new Quaternionf();

        private Vector3f mLookToward = new Vector3f();
        private Vector3f mLookAt = new Vector3f();

        @Override
        public void run() {
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
            float[] modelCam = mTargetTransform.getModelMatrix();
            mLookAt.set(modelCam[12], modelCam[13], modelCam[14]);
            mLookAt.sub(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
            float y = mLookAt.y;
            // Remove y vector
            mLookAt.y = 0;

            // Min distance to ball
            boolean moveTowardToBall = mLookAt.length() > 8.0f;
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

            if (moveTowardToBall) {
                float[] pose = mCharacter.getAnchor().getPose();
                // TODO: Create pose
                mLookAt.mul(mWalkSpeed);

                pose[12] = pose[12] + mLookAt.x;
                pose[14] = pose[14] + mLookAt.z;

                mCharacter.updatePose(pose);
            } else {
                mListener.onActionEnd(this);
            }
        }
    }
}
