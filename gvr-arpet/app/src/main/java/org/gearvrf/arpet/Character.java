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
import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.gesture.GestureDetector;
import org.gearvrf.arpet.gesture.OnGestureListener;
import org.gearvrf.arpet.gesture.OnScaleListener;
import org.gearvrf.arpet.gesture.ScalableObject;
import org.gearvrf.arpet.gesture.ScalableObjectManager;
import org.gearvrf.arpet.gesture.impl.GestureDetectorFactory;
import org.gearvrf.arpet.movement.MovableObject;
import org.gearvrf.arpet.movement.Movement;
import org.gearvrf.arpet.movement.SimpleMovementListener;
import org.gearvrf.arpet.movement.TargetObject;
import org.gearvrf.arpet.movement.impl.DefaultMovement;
import org.gearvrf.arpet.movement.impl.LookAtObjectMovement;
import org.gearvrf.arpet.movement.targetwrapper.ARCameraWrapper;
import org.gearvrf.arpet.petobjects.Bed;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class Character extends MovableObject implements
        GVRDrawFrameListener,
        OnGestureListener,
        ScalableObject {

    private final String TAG = getClass().getSimpleName();

    private GVRMixedReality mMixedReality;
    private List<OnScaleListener> mOnScaleListeners = new ArrayList<>();

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
    private GVRPlane mMovementBoundary;
    private TouchHandler mTouchHandler;
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;
    private final static String PET_NAME = "Pet";

    // Movement handlers
    private Movement mLookAtObjectMovement;
    private Movement mGoToObjectMovement;
    // ...

    // Gesture detectors
    private List<GestureDetector> mGestureDetectors = new ArrayList<>();
    private GestureDetector mRotationDetector;
    private GestureDetector mScaleDetector;
    // ...

    Character(@NonNull GVRContext gvrContext, @NonNull GVRMixedReality mixedReality, @NonNull float[] pose) {
        super(gvrContext, mixedReality, pose);

        mCurrentAction = PetAction.IDLE;
        mContext = gvrContext;
        mCurrentPose = pose;
        mMixedReality = mixedReality;
        load3DModel();

        mGestureDetectors.add(mRotationDetector = GestureDetectorFactory.INSTANCE.getSwipeRotationGestureDetector(mContext, this));
        mGestureDetectors.add(mScaleDetector = GestureDetectorFactory.INSTANCE.getScaleGestureDetector(mContext, this));

        mContext.getApplication().getEventReceiver().addListener(new GVREventListeners.ActivityEvents() {
            @Override
            public void dispatchTouchEvent(MotionEvent event) {
                mRotationDetector.onTouchEvent(event);
                mScaleDetector.onTouchEvent(event);
            }
        });

        mTouchHandler = new TouchHandler();
        initController();
    }

    private void initController() {
        final int cursorDepth = 100;
        GVRInputManager inputManager = mContext.getInputManager();
        mCursor = new GVRSceneObject(mContext,
                mContext.createQuad(0.2f * cursorDepth,
                        0.2f * cursorDepth),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.setCursorDepth(-cursorDepth);
                newController.setCursor(mCursor);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
            }
        });
    }

    public void goToBall() {
        mCurrentAction = PetAction.TO_BALL;
    }

    public void goToFood() {
        mCurrentAction = PetAction.TO_FOOD;
    }

    public void goToToilet() {
        mCurrentAction = PetAction.TO_TOILET;
    }

    public void goToBed() {
        TargetObject targetObject = (Bed) ScalableObjectManager.INSTANCE.getObjectByType(Bed.class);
        if (targetObject != null) {
            mCurrentAction = PetAction.TO_BED;
            goToObject(targetObject);
        }
    }

    public void goToScreen() {
        mCurrentAction = PetAction.TO_SCREEN;
        goToObject(new ARCameraWrapper(mMixedReality));
    }

    /**
     * Starts movement of this object to the given target object.
     *
     * @param targetObject The target object.
     * @param <Target>     Generic type of target object.
     */
    private <Target extends TargetObject> void goToObject(Target targetObject) {
        disableGestureDetectors();
        mGoToObjectMovement = new DefaultMovement<>(this, targetObject, new ToObjectMovementListener<Target>());
        // Sets some existing boundary
        ((DefaultMovement) mGoToObjectMovement).setBoundaryPlane(mMovementBoundary);
        registerDrawFrameListener();
        mGoToObjectMovement.move();
    }

    public <Target extends TargetObject> void lookAt(@NonNull Target objectToLookAt) {
        disableGestureDetectors();
        mLookAtObjectMovement = new LookAtObjectMovement<>(this, objectToLookAt, new LookAtObjectListener<Target>());
        mCurrentAction = PetAction.LOOK_AT;
        registerDrawFrameListener();
    }

    private void lookAtObject() {
        try {
            mLookAtObjectMovement.move();
        } catch (Throwable throwable) {
            stopMovement();
            throwable.printStackTrace();
        }
    }

    private void move() {
        try {
            updatePose(mCurrentPose);
        } catch (Throwable throwable) {
            stopMovement();
            throwable.printStackTrace();
        }
    }

    private void load3DModel() {
        GVRSceneObject sceneObject;
        try {
            sceneObject = mContext.getAssetLoader().loadModel("objects/Fox_Pokemon.obj");
            addChildObject(sceneObject);
            sceneObject.setName(PET_NAME);
            GVRBoxCollider boxCollider = new GVRBoxCollider(mContext);
            boxCollider.setHalfExtents(0.05f, 0.05f, 0.05f);
            sceneObject.attachCollider(boxCollider);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(float v) {

        switch (mCurrentAction) {

            case PetAction.IDLE:
                unregisterDrawFrameListener();
                break;
            case PetAction.LOOK_AT:
                lookAtObject();
            case PetAction.TO_BALL:
            case PetAction.TO_BED:
            case PetAction.TO_FOOD:
            case PetAction.TO_SCREEN:
            case PetAction.TO_TOILET:
                move();
                break;
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

    public void setBoundaryPlane(GVRPlane boundary) {
        mMovementBoundary = boundary;
    }

    @Override
    public void onGesture(GestureDetector detector) {
        if (detector == mRotationDetector) {
            getTransform().rotateByAxis(detector.getValue(), 0, 1, 0);
        } else if (detector == mScaleDetector) {
            scale(detector.getValue());
        }
    }

    public void setRotationEnabled(boolean enabled) {
        if (enabled) {
            stopMovement();
        }
        mRotationDetector.setEnabled(enabled);
    }

    public void setScaleEnabled(boolean enabled) {
        if (enabled) {
            stopMovement();
        }
        mScaleDetector.setEnabled(enabled);
    }

    public void setDraggingEnabled(boolean enabled) {
        if (enabled) {
            mCursorController.addPickEventListener(mTouchHandler);
        } else {
            mCursorController.removePickEventListener(mTouchHandler);
        }
    }

    private void disableGestureDetectors() {
        for (GestureDetector gestureDetector : mGestureDetectors) {
            gestureDetector.setEnabled(false);
        }
    }

    public void stopMovement() {
        mCurrentAction = PetAction.IDLE;
    }

    public void pauseMovement() {
        unregisterDrawFrameListener();
        if (mCurrentAction == PetAction.TO_SCREEN) {
            mGoToObjectMovement.stop();
        }
    }

    public void resumeMovement() {
        registerDrawFrameListener();
        if (mCurrentAction == PetAction.TO_SCREEN) {
            mGoToObjectMovement.move();
        }
    }

    @Override
    public void scale(float factor) {
        GVRTransform t = getTransform();
        t.setScale(factor, factor, factor);
        notifyScale(factor);
    }

    private synchronized void notifyScale(float factor) {
        for (OnScaleListener listener : mOnScaleListeners) {
            listener.onScale(factor);
        }
    }

    @Override
    public void addOnScaleListener(OnScaleListener listener) {
        if (!mOnScaleListeners.contains(listener)) {
            mOnScaleListeners.add(listener);
        }
    }

    private class LookAtObjectListener<Target extends TargetObject> extends SimpleMovementListener<Character, Target, Matrix4f> {
        @Override
        public void onMove(Character pet, Target target, Matrix4f position) {
            pet.getTransform().setModelMatrix(position);
        }
    }

    private class ToObjectMovementListener<Target extends TargetObject> extends SimpleMovementListener<Character, Target, Vector3f> {

        @Override
        public void onMove(Character pet, Target target, Vector3f position) {

            // Keeps looking at target object
            pet.getTransform().setModelMatrix(LookAtObjectMovement.lookAt(pet, target));

            // Update current position. The onDrawFrame() method uses this point to update
            // position of this object
            mCurrentPose[12] = position.x;
            mCurrentPose[13] = position.y;
            mCurrentPose[14] = position.z;
        }

        @Override
        public void onStopMove() {
            stopMovement();
        }
    }


    public class TouchHandler extends GVREventListeners.TouchEvents {
        private GVRSceneObject mDraggingObject = null;

        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            super.onTouchStart(sceneObject, pickedObject);
            if (sceneObject.getName().equals(PET_NAME)) {
                Log.d(TAG, "onTouchStart ");
                sceneObject.getTransform().setPositionY(0.02f);
                if (mDraggingObject == null) {
                    mDraggingObject = sceneObject;
                }
            }

        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            super.onTouchEnd(sceneObject, pickedObject);
            if (sceneObject.getName().equals(PET_NAME)) {
                Log.d(TAG, "onTouchEnd");
                sceneObject.getTransform().setPositionY(0f);
                if (mDraggingObject != null) {
                    mDraggingObject = null;
                }
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onInside(sceneObj, collision);

            if (mDraggingObject == null) {
                return;
            }

            if (sceneObj.getName().equals(PET_NAME)) {
                collision = pickSceneObject(mMixedReality.getPassThroughObject());
                if (collision != null) {
                    GVRHitResult gvrHitResult = mMixedReality.hitTest(mMixedReality.getPassThroughObject(), collision);
                    if (gvrHitResult != null) {
                        updatePose(gvrHitResult.getPose());
                    }
                }
            }
        }

        private GVRPicker.GVRPickedObject pickSceneObject(GVRSceneObject sceneObject) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            mCursorController.getPicker().getWorldPickRay(origin, direction);

            return GVRPicker.pickSceneObject(sceneObject, origin.x, origin.y, origin.z,
                    direction.x, direction.y, direction.z);
        }
    }
}
