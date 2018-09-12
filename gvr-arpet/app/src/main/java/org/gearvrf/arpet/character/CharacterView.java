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

package org.gearvrf.arpet.character;

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
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.gesture.GestureDetector;
import org.gearvrf.arpet.gesture.OnGestureListener;
import org.gearvrf.arpet.gesture.OnScaleListener;
import org.gearvrf.arpet.gesture.ScalableObject;
import org.gearvrf.arpet.gesture.impl.GestureDetectorFactory;
import org.gearvrf.arpet.mode.IPetView;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.util.LoadModelHelper;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterView extends AnchoredObject implements
        IPetView,
        OnGestureListener,
        ScalableObject {

    private final String TAG = getClass().getSimpleName();

    private GVRMixedReality mMixedReality;
    private List<OnScaleListener> mOnScaleListeners = new ArrayList<>();


    private GVRContext mContext;
    private GVRPlane mBoundaryPlane;
    private TouchHandler mTouchHandler;
    private GVRSceneObject mCursor;
    private GVRSceneObject mShadow;
    private GVRCursorController mCursorController;
    private final static String PET_NAME = "Pet";

    // Gesture detectors
    private List<GestureDetector> mGestureDetectors = new ArrayList<>();
    private GestureDetector mRotationDetector;
    private GestureDetector mScaleDetector;

    private GVRModelSceneObject m3DModel;

    CharacterView(
            @NonNull PetContext petContext,
            @NonNull float[] pose) {

        super(petContext.getGVRContext(), petContext.getMixedReality(), pose, ObjectType.CHARACTER);

        mContext = petContext.getGVRContext();
        mMixedReality = petContext.getMixedReality();

        registerGestureDetectors();

        mTouchHandler = new TouchHandler();

        createShadow();

        // TODO: Load at thread
        load3DModel();

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
        GVRInputManager inputManager = mContext.getInputManager();
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
            }
        });
    }

    private void load3DModel() {
        m3DModel = LoadModelHelper.loadModelSceneObject(mContext, LoadModelHelper.PET_MODEL_PATH);

        addChildObject(m3DModel);
        m3DModel.getTransform().setScale(0.001f, 0.001f, 0.001f);
        m3DModel.setName(PET_NAME);
            // FIXME: Set appropriate size for collider
        GVRBoxCollider boxCollider = new GVRBoxCollider(mContext);
        boxCollider.setHalfExtents(0.05f, 0.05f, 0.05f);
    }

    public GVRAnimation getAnimation(int i) {
        return m3DModel.getAnimations().get(i);
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
    public void updatePose(float[] poseMatrix) {
        if (mBoundaryPlane == null || mBoundaryPlane.isPoseInPolygon(poseMatrix)) {
            super.updatePose(poseMatrix);
        }
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
        mRotationDetector.setEnabled(enabled);
    }

    public void setScaleEnabled(boolean enabled) {
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

    @Override
    public void show(GVRScene mainScene) {
        mainScene.addSceneObject(getAnchor());
    }

    @Override
    public void hide(GVRScene mainScene) {
        mainScene.removeSceneObject(getAnchor());
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
