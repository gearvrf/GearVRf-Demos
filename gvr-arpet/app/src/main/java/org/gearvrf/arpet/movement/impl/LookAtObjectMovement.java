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

import org.gearvrf.GVRSceneObject;
import org.gearvrf.arpet.movement.Movement;
import org.gearvrf.arpet.movement.TargetObject;
import org.gearvrf.arpet.movement.MovableObject;
import org.gearvrf.arpet.movement.OnMovementListener;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LookAtObjectMovement<Movable extends MovableObject, Target extends TargetObject>
        extends Movement<Movable, Target, OnMovementListener<Movable, Matrix4f>> {

    private Matrix4f mPosition = new Matrix4f();

    public LookAtObjectMovement(@NonNull Movable objectToMove,
                                @NonNull Target objectToLookAt,
                                @NonNull OnMovementListener<Movable, Matrix4f> listener) {
        super(objectToMove, objectToLookAt, listener);
    }

    @Override
    public void move() {
        mPosition.set(lookAt(mMovable, mTarget));
        mOnMovementListener.onMove(mMovable, mPosition);
    }

    public static <T extends TargetObject> Matrix4f lookAt(@NonNull GVRSceneObject objectToMove,
                                                           @NonNull T target) {

        float[] modelMatrix = target.getTransform().getModelMatrix();

        Vector3f vectorDest = new Vector3f();
        Vector3f vectorOrig = new Vector3f();
        Vector3f direction = new Vector3f();

        vectorDest.set(modelMatrix[12], modelMatrix[13], modelMatrix[14]);

        float[] originModelMatrix = objectToMove.getTransform().getModelMatrix();
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

        if (objectToMove.getParent() != null) {
            float rotationX = objectToMove.getParent().getTransform().getRotationX();
            float rotationY = objectToMove.getParent().getTransform().getRotationY();
            float rotationZ = objectToMove.getParent().getTransform().getRotationZ();
            float rotationW = objectToMove.getParent().getTransform().getRotationW();
            Quaternionf parentQuaternion = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
            parentQuaternion = parentQuaternion.invert();
            matrix = matrix.rotate(parentQuaternion);
        }

        return matrix;
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean isMoving() {
        return false;
    }
}
