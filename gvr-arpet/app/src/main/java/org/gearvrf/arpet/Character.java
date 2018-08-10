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
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.gesture.GestureDetector;
import org.gearvrf.arpet.gesture.RotationGestureDetector;
import org.gearvrf.arpet.gesture.ScaleGestureDetector;
import org.gearvrf.arpet.movement.BasicMovement;
import org.gearvrf.arpet.movement.MovableObject;
import org.gearvrf.arpet.movement.SimpleMovementListener;
import org.gearvrf.arpet.movement.lookatobject.LookAtObjectMovement;
import org.gearvrf.arpet.movement.lookatobject.LookAtObjectMovementPosition;
import org.gearvrf.arpet.movement.lookatobject.ObjectToLookAt;
import org.gearvrf.arpet.movement.toscreen.ToScreenMovement;
import org.gearvrf.arpet.movement.toscreen.ToScreenMovementPosition;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class Character extends MovableObject implements GVRDrawFrameListener,
        RotationGestureDetector.OnRotationGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {

    private GVRMixedReality mMixedReality;

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
    private GVRPlane mPetMovementBoundary;

    // Movement handlers
    private BasicMovement mToScreenMovement;
    private BasicMovement mLookAtObjectMovement;
    // ...

    // Gesture detectors
    private List<GestureDetector> mGestureDetectors = new ArrayList<>();
    private RotationGestureDetector mRotationDetector;
    private ScaleGestureDetector mScaleDetector;
    // ...

    Character(@NonNull GVRContext gvrContext, @NonNull GVRMixedReality mixedReality, @NonNull float[] pose) {
        super(gvrContext, mixedReality, pose);

        mCurrentAction = PetAction.IDLE;
        mContext = gvrContext;
        mMixedReality = mixedReality;
        mCurrentPose = pose;
        load3DModel();

        mGestureDetectors.add(mRotationDetector = new RotationGestureDetector(this));
        mGestureDetectors.add(mScaleDetector = new ScaleGestureDetector(mContext, this));

        mContext.getApplication().getEventReceiver().addListener(new GVREventListeners.ActivityEvents() {
            @Override
            public void dispatchTouchEvent(MotionEvent event) {
                mRotationDetector.onTouchEvent(event);
                mScaleDetector.onTouchEvent(event);
            }
        });
    }

    public void goToBall() {
        mCurrentAction = PetAction.TO_BALL;
    }

    public void goToScreen() {
        disableGestureDetectors();
        mToScreenMovement = new ToScreenMovement<>(this, mMixedReality, new TooScreenMovementListener(mContext));
        ((ToScreenMovement) mToScreenMovement).setBoundaryPlane(mPetMovementBoundary);
        mCurrentAction = PetAction.TO_SCREEN;
        registerDrawFrameListener();
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

    public void lookAt(@NonNull ObjectToLookAt objectToLookAt) {
        disableGestureDetectors();
        mLookAtObjectMovement = new LookAtObjectMovement<>(this, objectToLookAt, new LookAtObjectListener());
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

    private void moveToBall() {

    }

    private void moveToScreen() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(float v) {

        if (mCurrentAction == PetAction.IDLE) {

            unregisterDrawFrameListener();

        } else if (mCurrentAction == PetAction.LOOK_AT) {

            lookAtObject();

        } else if (mCurrentAction == PetAction.TO_SCREEN) {

            moveToScreen();
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
        mPetMovementBoundary = boundary;
    }

    @Override
    public void onRotate(RotationGestureDetector detector) {
        getTransform().rotateByAxis(mRotationDetector.getAngle() * 0.05f, 0, 1, 0);
    }

    @Override
    public void onScale(ScaleGestureDetector detector) {
        GVRTransform t = getTransform();
        float factor = detector.getFactor();
        t.setScale(factor, factor, factor);
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
            mToScreenMovement.stop();
        }
    }

    public void resumeMovement() {
        registerDrawFrameListener();
        if (mCurrentAction == PetAction.TO_SCREEN) {
            mToScreenMovement.move();
        }
    }

    private class TooScreenMovementListener extends SimpleMovementListener<Character, ToScreenMovementPosition> {

        private ObjectToLookAt mCamera;

        TooScreenMovementListener(@NonNull GVRContext context) {
            mCamera = new ObjectToLookAt(context.getMainScene().getMainCameraRig());
        }

        @Override
        public void onMove(Character pet, ToScreenMovementPosition position) {

            // Keep the pet looking at GVR camera
            pet.getTransform().setModelMatrix(LookAtObjectMovement.lookAt(Character.this, mCamera));

            // Update current position. The onDrawFrame() method uses this point to update
            // position of this character
            mCurrentPose[12] = position.getValue().x;
            mCurrentPose[13] = position.getValue().y;
            mCurrentPose[14] = position.getValue().z;
        }

        @Override
        public void onStopMove() {
            stopMovement();
        }
    }

    private class LookAtObjectListener extends SimpleMovementListener<Character, LookAtObjectMovementPosition> {
        @Override
        public void onMove(Character pet, LookAtObjectMovementPosition position) {
            pet.getTransform().setModelMatrix(position.getValue());
        }
    }
}
