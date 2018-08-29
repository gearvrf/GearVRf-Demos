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
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.arpet.animation.PetAnimationHelper;
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
import org.gearvrf.arpet.movement.targetwrapper.BallWrapper;
import org.gearvrf.arpet.petobjects.Bed;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
    private BallWrapper mBall;

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
    private GVRPlane mBoundaryPlane;
    private TouchHandler mTouchHandler;
    private GVRSceneObject mCursor;
    private GVRSceneObject mShadow;
    private GVRCursorController mCursorController;
    private final static String PET_NAME = "Pet";

    private PetAnimationHelper mPetAnimationHelper;

    // Movement handlers
    private Movement mLookAtObjectMovement;
    private Movement mGoToObjectMovement;
    // ...

    // Gesture detectors
    private List<GestureDetector> mGestureDetectors = new ArrayList<>();
    private GestureDetector mRotationDetector;
    private GestureDetector mScaleDetector;
    // ...

    private Vector3f mCurrentBallPosition = new Vector3f();
    private Vector3f mCurrentPetPosition = new Vector3f();

    private float[] mLastBallHitPose;

    private OnPetActionListener mOnPetActionListener;

    Character(
            @NonNull GVRContext gvrContext,
            @NonNull GVRMixedReality mixedReality,
            @NonNull float[] pose,
            @NonNull OnPetActionListener petListener,
            @NonNull PetAnimationHelper petAnimationHelper) {

        super(gvrContext, mixedReality, pose);

        mOnPetActionListener = petListener;
        mCurrentAction = PetAction.IDLE;
        mContext = gvrContext;
        mCurrentPose = pose;
        mMixedReality = mixedReality;
        mPetAnimationHelper = petAnimationHelper;
        createShadow();

        registerGestureDetectors();

        mTouchHandler = new TouchHandler();
        initController();

    }

    private void registerGestureDetectors() {

        mGestureDetectors.add(mRotationDetector = GestureDetectorFactory.INSTANCE.getSwipeRotationGestureDetector(mContext, this));
        mGestureDetectors.add(mScaleDetector = GestureDetectorFactory.INSTANCE.getScaleGestureDetector(mContext, this));

        mContext.getApplication().getEventReceiver().addListener(new GVREventListeners.ActivityEvents() {
            @Override
            public void dispatchTouchEvent(MotionEvent event) {
                mRotationDetector.onTouchEvent(event);
                mScaleDetector.onTouchEvent(event);
            }
        });
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

    public void goToBall(BallWrapper ballWrapper) {
        // FIXME: some cleanup might be needed
        mBall = ballWrapper;
        mCurrentAction = PetAction.TO_BALL;
        registerDrawFrameListener();
        mOnPetActionListener.onActionStart(PetAction.TO_BALL);
//        mPetAnimationHelper.play(PetAnimationHelper.PetAnimation.WALK, -1);
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
        mOnPetActionListener.onActionStart(PetAction.TO_SCREEN);
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
        ((DefaultMovement) mGoToObjectMovement).setBoundaryPlane(mBoundaryPlane);
        registerDrawFrameListener();
        mGoToObjectMovement.move();
    }

    public <Target extends TargetObject> void lookAt(@NonNull Target objectToLookAt) {
        disableGestureDetectors();
        mLookAtObjectMovement = new LookAtObjectMovement<>(this, objectToLookAt, new LookAtObjectListener<Target>());
        mCurrentAction = PetAction.LOOK_AT;
        registerDrawFrameListener();
    }

    private synchronized void lookAtObject() {
        try {
            mLookAtObjectMovement.move();
        } catch (Throwable throwable) {
            stopMovement();
            throwable.printStackTrace();
        }
    }

    private synchronized void moveToObject() {
        try {
            updatePose(mCurrentPose);
        } catch (Throwable throwable) {
            stopMovement();
            throwable.printStackTrace();
        }
    }

    public void set3DModel(GVRModelSceneObject petObject) {
            addChildObject(petObject);
            petObject.getTransform().setScale(0.001f, 0.001f, 0.001f);
            petObject.setName(PET_NAME);
            // FIXME: Set appropriate size for collider
            GVRBoxCollider boxCollider = new GVRBoxCollider(mContext);
            boxCollider.setHalfExtents(0.05f, 0.05f, 0.05f);

        mPetAnimationHelper.setAnimations(petObject.getAnimations(),
                (GVRAnimator) petObject.getComponent(GVRAnimator.getComponentType()));
    }

    private void createShadow() {
        GVRTexture tex = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.drawable.drag_shadow));
        GVRMaterial mat = new GVRMaterial(mContext);
        mat.setMainTexture(tex);
        mShadow = new GVRSceneObject(mContext, 0.05f, 0.1f);
        mShadow.getRenderData().setMaterial(mat);
        mShadow.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);
        mShadow.getTransform().setPosition(0f, 0.01f, -0.02f);
        mShadow.setName("shadow");
        mShadow.setEnable(false);
        addChildObject(mShadow);
    }

    @Override
    public void onDrawFrame(float v) {

        Log.d(TAG, "onDrawFrame: " + mCurrentAction);

        switch (mCurrentAction) {

            case PetAction.IDLE:
                unregisterDrawFrameListener();
                break;
            case PetAction.LOOK_AT:
                lookAtObject();
                break;
            case PetAction.TO_BALL:
                moveToBall();
                break;
            case PetAction.TO_BED:
            case PetAction.TO_FOOD:
            case PetAction.TO_SCREEN:
            case PetAction.TO_TOILET:
                moveToObject();
                break;
        }
    }

    private synchronized void moveToBall() {
        float[] m = mBall.getPoseMatrix();
        float[] t = getAnchor().getPose();
        t[12] = m[12] * 0.01f;
        t[14] = m[14] * 0.01f;

        mCurrentBallPosition.set(t[12], m[13] * 0.01f, t[14]);
        getPositionFromPose(mCurrentPetPosition, mCurrentPose);

        if (mCurrentPetPosition.distance(mCurrentBallPosition) < 0.06) {
            Log.d(TAG, "StopPetMovement");
            stopMovement();
            mOnPetActionListener.onActionEnd(PetAction.TO_BALL);
//            mPetAnimationHelper.stop(PetAnimationHelper.PetAnimation.WALK);
        }

        try {

            // Keeps the pet looking to the ball
            getTransform().setModelMatrix(LookAtObjectMovement.lookAt(this, mBall));
            // Gets interpolation and update pet position
            // FIXME: this can be optimized
            float[] p = mMixedReality.makeInterpolated(getAnchor().getPose(), t, 0.01f);
            if (mBoundaryPlane.isPoseInPolygon(p)) {
                mCurrentPose = p;
                updatePose(mCurrentPose);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getPositionFromPose(Vector3f out, float[] pose) {
        out.x = pose[12];
        out.y = pose[13];
        out.z = pose[14];
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
        mBoundaryPlane = boundary;
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

    private synchronized void stopMovement() {

        mCurrentAction = PetAction.IDLE;
        unregisterDrawFrameListener();

        if (mLookAtObjectMovement != null) {
            mLookAtObjectMovement.stop();
            mLookAtObjectMovement = null;
        }
        if (mGoToObjectMovement != null) {
            mGoToObjectMovement.stop();
            mGoToObjectMovement = null;
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
            mCurrentPose = getPoseMatrix();
            mCurrentPose[12] = position.x;
            mCurrentPose[13] = position.y;
            mCurrentPose[14] = position.z;
        }

        @Override
        public void onStopMove() {
            int action = mCurrentAction;
            stopMovement();
            if (action == PetAction.TO_SCREEN) {
                mOnPetActionListener.onActionEnd(PetAction.TO_SCREEN);
            }
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
                mShadow.setEnable(true);
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
                mShadow.setEnable(false);
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