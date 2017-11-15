/*
 * Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor;

import android.util.SparseArray;
import android.view.KeyEvent;


import org.gearvrf.GVRPicker;

import org.gearvrf.GVRSceneObject;

import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.CursorType;
import org.gearvrf.io.cursor3d.ICursorEvents;
import org.gearvrf.io.cursor3d.SelectableBehavior;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.util.concurrent.Future;

class SpaceObject implements ICursorEvents {
    private static final String TAG = SpaceObject.class.getSimpleName();
    protected GVRSceneObject mainObject;
    private final float rotationX;
    private final float rotationY;
    private final float x, y, z;
    private final float orientationX;
    private final float orientationY;
    private final float orientationZ;

    SpaceObject(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f position, float
            scale, float rotationX, float rotationY) {
        this(cursorMgr, asset, name, position, scale, rotationX, rotationY, 0.0f, 0.0f, 0.0f);
    }

    SpaceObject(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f position, float
            scale, float rotationX, float rotationY, float orientationX, float orientationY,
                float orientationZ) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.orientationX = orientationX;
        this.orientationY = orientationY;
        this.orientationZ = orientationZ;

        mainObject = asset;
        mainObject.setName(name);
        mainObject.getTransform().setScale(scale, scale, scale);
        reset();
        mainObject.getEventReceiver().addListener(this);
        mainObject.attachComponent(new SelectableBehavior(cursorMgr, true));
    }

    void reset() {
        mainObject.getTransform().setPosition(x, y, z);
        mainObject.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        if (orientationX != 0.0f) {
            mainObject.getTransform().rotateByAxis(orientationX, 1.0f, 0.0f, 0.0f);
        }
        if (orientationY != 0.0f) {
            mainObject.getTransform().rotateByAxis(orientationY, 0.0f, 1.0f, 0.0f);
        }
        if (orientationZ != 0.0f) {
            mainObject.getTransform().rotateByAxis(orientationZ, 0.0f, 0.0f, 1.0f);
        }

        if (rotationX != 0.0f) {
            mainObject.getTransform().rotateByAxisWithPivot(rotationX, 0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f);
        }
        if (rotationY != 0.0f) {
            mainObject.getTransform().rotateByAxisWithPivot(rotationY, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f);
        }
    }

    GVRSceneObject getSceneObject() {
        return mainObject;
    }


    public void onCursorScale(Cursor cursor) { }
    public void onTouchStart(Cursor cursor, GVRPicker.GVRPickedObject hit) { }
    public void onTouchEnd(Cursor cursor, GVRPicker.GVRPickedObject hit) { }
    public void onEnter(Cursor cursor, GVRPicker.GVRPickedObject hit) { }
    public void onExit(Cursor cursor, GVRPicker.GVRPickedObject hit) { }
    public void onDrag(Cursor cursor, GVRPicker.GVRPickedObject hit) { }
}