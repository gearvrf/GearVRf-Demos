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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;

public class Character extends GVRSceneObject implements GVRDrawFrameListener {

    private enum PetAction {
        IDLE,
        TO_BALL,
        TO_SCREEN,
        TO_FOOD,
        TO_TOILET,
        TO_BED
    }

    private PetAction mCurrentAction;
    private GVRContext mContext;

    public Character(GVRContext gvrContext) {
        super(gvrContext);
        mCurrentAction = PetAction.IDLE;
        mContext = gvrContext;

        load3DModel();

    }

    public void goToBall() {
        mCurrentAction = PetAction.TO_BALL;
    }

    public void goToScreen() {
        mCurrentAction = PetAction.TO_SCREEN;
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
        if (object == null)
            return;

        Vector3f vectorDest = new Vector3f();
        Vector3f vectorOrig = new Vector3f();
        Vector3f direction = new Vector3f();

        float[] modelMatrix = object.getTransform().getModelMatrix();
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

    private void moveToBall() {

    }

    private void moveToScreen() {

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

    }
}
