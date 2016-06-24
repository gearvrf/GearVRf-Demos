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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.io.cursor.AssetHolder.AssetObjectTuple;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorEvent;
import org.gearvrf.io.cursor3d.CursorType;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.util.concurrent.Future;

class SpaceObject {
    private static final String TAG = SpaceObject.class.getSimpleName();
    protected GVRSceneObject mainObject;
    private GVRRenderData renderData;
    private boolean previousActive;
    private boolean previousOver;

    static final int INIT = 0;
    static final int WIRE = 1;
    static final int OVER = 2;
    static final int CLICKED = 3;

    private SparseArray<Integer> states;
    private final AssetHolder holder;
    protected int currentState = INIT;

    private final float rotationX;
    private final float rotationY;
    private final float x, y, z;
    private final float orientationX;
    private final float orientationY;
    private final float orientationZ;

    SpaceObject(GVRContext gvrContext, AssetHolder holder, String name, Vector3f position, float
            scale, float rotationX, float rotationY) {
        this(gvrContext, holder, name, position, scale, rotationX, rotationY, 0.0f, 0.0f, 0.0f);
    }

    SpaceObject(GVRContext gvrContext, AssetHolder holder, String name, Vector3f position, float
            scale, float rotationX, float rotationY, float orientationX, float orientationY,
                float orientationZ) {
        this.holder = holder;
        states = new SparseArray<Integer>();
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.orientationX = orientationX;
        this.orientationY = orientationY;
        this.orientationZ = orientationZ;

        mainObject = new GVRSceneObject(gvrContext);
        mainObject.setName(name);
        mainObject.getTransform().setScale(scale, scale, scale);
        reset();
        renderData = new GVRRenderData(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Texture.ID);
        renderData.setMaterial(material);
        mainObject.attachRenderData(renderData);
        AssetObjectTuple tuple = holder.getTuple(INIT);
        mainObject.getRenderData().setMesh(tuple.mesh);
        mainObject.getRenderData().getMaterial().setMainTexture(tuple.texture);
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

    private boolean isHigherOrEqualStatePresent(int targetState) {
        for (int i = 0; i < states.size(); i++) {
            if (targetState <= states.valueAt(i)) {
                return true;
            }
        }
        return false;
    }

    private void setButtonPress(int cursorId) {
        states.remove(cursorId);
        if (!isHigherOrEqualStatePresent(CLICKED)) {
            currentState = CLICKED;
            setAsset(currentState);
        }
        states.put(cursorId, CLICKED);
    }

    private void setIntersect(int cursorId) {
        states.remove(cursorId);
        if (!isHigherOrEqualStatePresent(CLICKED)) {
            currentState = OVER;
            setAsset(currentState);
        }
        states.put(cursorId, OVER);
    }

    private void setWireFrame(int cursorId) {
        states.remove(cursorId);
        if (!isHigherOrEqualStatePresent(WIRE)) {
            currentState = WIRE;
            setAsset(currentState);
        }
        states.put(cursorId, WIRE);
    }

    private int getHighestPriorityState() {
        int highestPriority = INIT;
        for (int i = 0; i < states.size(); i++) {
            int state = states.valueAt(i);

            if (state > highestPriority) {
                highestPriority = state;
            }
        }
        return highestPriority;
    }

    private void setDefault(int cursorId) {
        states.remove(cursorId);
        int highestPriority = getHighestPriorityState();
        if (currentState != highestPriority) {
            currentState = highestPriority;
            setAsset(currentState);
        }
        states.put(cursorId, INIT);
    }

    private void setAsset(int state) {

        AssetObjectTuple tuple = holder.getTuple(state);

        Future<GVRMesh> mesh = tuple.mesh;
        Future<GVRTexture> texture = tuple.texture;

        if (renderData != mesh) {
            renderData.setMesh(mesh);
        }
        if (renderData.getMaterial() != texture) {
            renderData.getMaterial().setMainTexture(texture);
        }
    }

    void handleCursorEvent(CursorEvent event) {
        Cursor cursor = event.getCursor();
        float cursorDistance = getDistance(cursor.getPositionX(), cursor.getPositionY(), cursor
                .getPositionZ());
        float soDistance = getDistance(getSceneObject());
        boolean isOver = event.isOver();
        boolean isActive = event.isActive();
        boolean isColliding = event.isColliding();
        int cursorId = event.getCursor().getId();
        KeyEvent keyEvent = event.getKeyEvent();

        Integer state = states.get(cursorId);
        if (state == null) {
            return;
        }
        switch (state) {
            case INIT:
                if (!isOver) {
                    break;
                }
                if (isColliding) {
                    if (isActive && previousOver && !previousActive) {
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            setButtonPress(cursorId);
                            handleClickEvent(event);
                        }
                    } else if (!isActive) {
                        setIntersect(cursorId);
                    }
                } else if (cursorDistance > soDistance) {
                    setWireFrame(cursorId);
                }
                break;
            case CLICKED:
                if (isOver && isColliding) {
                    if (isActive) {
                        handleDragEvent(event);
                    } else {
                        setIntersect(cursorId);
                        handleClickReleased(event);
                    }
                } else {
                    if (isActive) {
                        if (event.getCursor().getCursorType() == CursorType.OBJECT) {
                            setDefault(cursorId);
                        }
                        handleCursorLeave(event);
                    } else {
                        setDefault(cursorId);
                        handleClickReleased(event);
                    }
                }
                break;
            case OVER:
                if (!isOver) {
                    setDefault(cursorId);
                    break;
                }
                if (isColliding) {
                    if (isActive) {
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            setButtonPress(cursorId);
                            handleClickEvent(event);
                        }
                    }
                } else {
                    if (cursorDistance > soDistance) {
                        setWireFrame(cursorId);
                    } else if (cursorDistance < soDistance) {
                        setDefault(cursorId);
                    }
                }
                break;
            case WIRE:
                if (!isOver) {
                    setDefault(cursorId);
                    break;
                }
                if (isColliding) {
                    if (isActive) {
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                            setButtonPress(cursorId);
                            handleClickEvent(event);
                        }
                    } else {
                        setIntersect(cursorId);
                    }
                } else if (cursorDistance < soDistance) {
                    setDefault(cursorId);
                }
                break;
        }
        previousOver = event.isOver();
        previousActive = event.isActive();
    }

    void handleClickEvent(CursorEvent event) {
    }

    void handleClickReleased(CursorEvent event) {
    }

    void handleCursorLeave(CursorEvent event) {
    }

    void handleDragEvent(CursorEvent event) {
    }

    float getDistance(GVRSceneObject object) {
        GVRTransform transform = object.getTransform();
        float x = transform.getPositionX();
        float y = transform.getPositionY();
        float z = transform.getPositionZ();
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    float getDistance(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    void onCursorDeactivated(Cursor cursor) {
        int cursorId = cursor.getId();
        Integer state = states.get(cursorId);
        if (state != null) {
            states.remove(cursorId);
            if (currentState == state) {
                int highestPrioritState = getHighestPriorityState();
                setAsset(highestPrioritState);
            }
        }

        if (mainObject.getParent() == cursor.getSceneObject()) {
            cursor.getSceneObject().removeChildObject(mainObject);
        }
    }

    void onCursorActivated(Cursor cursor) {
        states.put(cursor.getId(), INIT);
    }
}