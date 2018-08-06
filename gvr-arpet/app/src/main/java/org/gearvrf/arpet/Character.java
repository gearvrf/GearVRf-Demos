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

package org.gearvrf.arpet;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Character extends AnchoredObject implements GVRDrawFrameListener {

    @IntDef({PetAction.IDLE, PetAction.TO_BALL,
            PetAction.TO_SCREEN, PetAction.TO_FOOD,
            PetAction.TO_TOILET, PetAction.TO_BED,
            PetAction.LOOK_AT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PetAction {
        int IDLE = 0;
        int TO_BALL = 1;
        int TO_SCREEN = 2;
        int TO_FOOD = 3;
        int TO_TOILET = 4;
        int TO_BED = 5;
        int LOOK_AT = 6;
    }

    @PetAction
    private int mCurrentAction; // default action IDLE
    private GVRContext mContext;
    private float[] mCurrentPose;
    private ToScreenMovement mToScreenMovement;

    Character(@NonNull GVRContext gvrContext, @NonNull GVRMixedReality mixedReality, @NonNull float[] pose) {
        super(gvrContext, mixedReality, pose);

        mCurrentAction = PetAction.IDLE;
        mContext = gvrContext;
        mCurrentPose = pose;
        load3DModel();

        mToScreenMovement = new ToScreenMovement<>(this, mixedReality);
        mToScreenMovement.setPetMovementListener(mOnPetMovementListener);
    }

    public void goToBall() {
        mCurrentAction = PetAction.TO_BALL;
    }

    public void goToScreen() {
        mCurrentAction = PetAction.TO_SCREEN;
        setMovementEnabled(true);
        mToScreenMovement.move();
    }

    public void goToFood() {
        mCurrentAction = PetAction.TO_FOOD;
    }

    public void goToToilet() {
        mCurrentAction = PetAction.TO_TOILET;
    }

    public void goToBed() {
        mCurrentAction = PetAction.TO_BED;
    }

    public void lookAt(GVRSceneObject object) {

        //mCurrentAction = PetAction.LOOK_AT;

        if (object == null)
            return;

        lookAt(object.getTransform().getModelMatrix());
    }

    private void moveToBall() {

    }

    private void moveToScreen() {

    }

    private void lookAt(float[] modelMatrix) {

        Vector3f vectorDest = new Vector3f();
        Vector3f vectorOrig = new Vector3f();
        Vector3f direction = new Vector3f();

        vectorDest.set(modelMatrix[12], modelMatrix[13], modelMatrix[14]);

        float[] originModelMatrix = getTransform().getModelMatrix();
        vectorOrig.set(originModelMatrix[12], originModelMatrix[13], originModelMatrix[14]);

        vectorDest.sub(vectorOrig, direction);
        // Make the object be rotated in Y axis only
        direction.y = 0;
        direction.normalize();

        // From: http://mmmovania.blogspot.com/2014/03/making-opengl-object-look-at-another.html
        Vector3f up;
        if (Math.abs(direction.x) < 0.00001 && Math.abs(direction.z) < 0.00001) {
            if (direction.y > 0) {
                up = new Vector3f(0.0f, 0.0f, -1.0f); // if direction points in +y
            } else {
                up = new Vector3f(0.0f, 0.0f, 1.0f); // if direction points in -y
            }
        } else {
            up = new Vector3f(0.0f, 1.0f, 0.0f); // y-axis is the general up
        }

        up.normalize();
        Vector3f right = new Vector3f();
        up.cross(direction, right);
        right.normalize();
        direction.cross(right, up);
        up.normalize();

        Matrix4f matrix = new Matrix4f();
        matrix.set(
                right.x, right.y, right.z, 0.0f,
                up.x, up.y, up.z, 0.0f,
                direction.x, direction.y, direction.z, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);

        if (getParent() != null) {
            float rotationX = getParent().getTransform().getRotationX();
            float rotationY = getParent().getTransform().getRotationY();
            float rotationZ = getParent().getTransform().getRotationZ();
            float rotationW = getParent().getTransform().getRotationW();
            Quaternionf parentQuaternion = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
            parentQuaternion = parentQuaternion.invert();
            matrix = matrix.rotate(parentQuaternion);
        }

        getTransform().setModelMatrix(matrix);

    }

    private void load3DModel() {
        GVRSceneObject sceneObject;
        try {
            sceneObject = mContext.getAssetLoader().loadModel("objects/Fox_Pokemon.obj");
            addChildObject(sceneObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(float v) {
        if (mCurrentAction == PetAction.TO_SCREEN) {
            Log.d("", "onDrawFrame: " + mCurrentPose[12] + ", " + mCurrentPose[13] + ", " + mCurrentPose[14]);
            try {
                updatePose(mCurrentPose);
            } catch (Throwable throwable) {
                setMovementEnabled(false);
                throwable.printStackTrace();
            }
        }
    }

    /**
     * Enable calling for {@link #onDrawFrame(float)}
     */
    private void registerDrawFrameListener() {
        getGVRContext().registerDrawFrameListener(this);
    }

    /**
     * Disable calling for {@link #onDrawFrame(float)}
     */
    private void unregisterDrawFrameListener() {
        getGVRContext().unregisterDrawFrameListener(this);
    }

    /**
     * Whether the pet should move or not
     *
     * @param enabled whether the pet should move or not
     */
    private void setMovementEnabled(boolean enabled) {
        if (enabled) {
            registerDrawFrameListener();
        } else {
            unregisterDrawFrameListener();
        }
    }

    @PetAction
    public int getCurrentAction() {
        return mCurrentAction;
    }

    public void setBoundaryPlane(GVRPlane boundary) {
        mToScreenMovement.setBoundaryPlane(boundary);
    }

    private OnPetMovementListener mOnPetMovementListener = new OnPetMovementListener() {
        @Override
        public void onStartMove() {
            Log.d(TAG, "onStartMove: ");
        }

        @Override
        public void onMove(float x, float y, float z) {
            Log.d(TAG, "onMove: ");

            // Keep the pet looking for the GVR camera
            lookAt(mContext.getMainScene().getMainCameraRig().getHeadTransform().getModelMatrix());

            // Update current position. The onDrawFrame() method uses this point to update
            // position of this character
            mCurrentPose[12] = x;
            mCurrentPose[13] = y;
            mCurrentPose[14] = z;
        }

        @Override
        public void onStopMove() {
            Log.d(TAG, "onStopMove: ");
            mCurrentAction = PetAction.IDLE;
            setMovementEnabled(false);
        }
    };
}
