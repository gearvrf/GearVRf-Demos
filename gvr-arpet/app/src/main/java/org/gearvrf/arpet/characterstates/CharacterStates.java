package org.gearvrf.arpet.characterstates;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.movement.MovableObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Represents a state of the Character.
 */
public class CharacterStates {
    private static final String TAG = "CharacterStates";

    public static class IDLE implements IState {
        public static int ID = 0;

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

        @Override
        public void run() {

        }
    }

    public static class LOOKING_AT_CAM implements IState {
        public static int ID = 1;
        private final MovableObject mCharacter;
        private final GVRCameraRig mCameraRig;
        private float mTurnSpeed = 0.05f;
        private float mWalkSpeed = 0.005f;

        public LOOKING_AT_CAM(MovableObject character, GVRCameraRig cameraRig) {
            mCharacter = character;
            mCameraRig = cameraRig;
        }

        @Override
        public int id() { return ID; }

        @Override
        public void entry() {
            Log.w(TAG, "entry => LOOKING_AT_CAM");
        }

        @Override
        public void exit() {
            Log.w(TAG, "exit => LOOKING_AT_CAM");
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
            float[] modelCam = mCameraRig.getTransform().getModelMatrix();
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
            }
        }
    }

    private static class MOVING_TO_CAM implements IState {
        public static int ID = 2;

        @Override
        public int id() { return ID; }

        @Override
        public void entry() {
            Log.w(TAG, "entry => MOVING_TO_CAM");
        }

        @Override
        public void exit() {
            Log.w(TAG, "exit => MOVING_TO_CAM");
        }

        @Override
        public void run() {

        }
    }

    private static class MOVING_TO_BALL implements IState {
        public static int ID = 3;

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

        @Override
        public void run() {

        }
    }
}
