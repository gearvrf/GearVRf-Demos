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

package org.gearvrf.arpet.mode;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.character.CharacterView;
import org.gearvrf.arpet.gesture.GestureDetector;
import org.gearvrf.arpet.gesture.OnGestureListener;
import org.gearvrf.arpet.gesture.impl.GestureDetectorFactory;
import org.gearvrf.arpet.gesture.impl.ScaleGestureDetector;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.mixedreality.IMixedReality;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class EditMode extends BasePetMode {
    private OnBackToHudModeListener mBackToHudModeListener;
    private final CharacterView mCharacterView;
    private final IMixedReality mMixedReality;
    private GestureDetector mRotationDetector;
    private GestureDetector mScaleDetector;
    private GVRCursorController mCursorController = null;
    private final GestureHandler mGestureHandler;

    public EditMode(PetContext petContext, OnBackToHudModeListener listener, CharacterController controller) {
        super(petContext, new EditView(petContext));
        mBackToHudModeListener = listener;
        ((EditView) mModeScene).setListenerEditMode(new OnEditModeClickedListenerHandler());
        mCharacterView = controller.getView();
        mMixedReality = petContext.getMixedReality();

        mGestureHandler = new GestureHandler();
        // FIXME: remove listener from constructor
        mRotationDetector = GestureDetectorFactory.INSTANCE.getSwipeRotationGestureDetector(
                mPetContext.getGVRContext(), mGestureHandler);
        mScaleDetector = GestureDetectorFactory.INSTANCE.getScaleGestureDetector(
                mPetContext.getGVRContext(), mGestureHandler);
    }

    @Override
    protected void onEnter() {
        ((ScaleGestureDetector)mScaleDetector).setScale(mCharacterView.getTransform().getScaleX());
    }

    @Override
    protected void onExit() {
        onDisableGesture();
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {

    }

    public void onEnableGesture(GVRCursorController cursorController) {
        if (mCursorController == null) {
            mCursorController = cursorController;

            mCursorController.addPickEventListener(mGestureHandler);
            mPetContext.getGVRContext().getApplication().getEventReceiver().addListener(mGestureHandler);

            mScaleDetector.setEnabled(true);
            mRotationDetector.setEnabled(true);
        }
    }

    public void onDisableGesture() {
        if (mCursorController != null) {
            mCursorController.removePickEventListener(mGestureHandler);
            mPetContext.getGVRContext().getApplication().getEventReceiver().removeListener(mGestureHandler);

            mScaleDetector.setEnabled(false);
            mRotationDetector.setEnabled(false);

            mCursorController = null;
        }
    }

    private class OnEditModeClickedListenerHandler implements OnEditModeClickedListener {

        @Override
        public void OnBack() {
            mPetContext.getGVRContext().runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mBackToHudModeListener.OnBackToHud();
                }
            });
            Log.d(TAG, "On Back");
        }

        @Override
        public void OnSave() {

        }
    }

    private class GestureHandler extends GVREventListeners.ActivityEvents
            implements OnGestureListener, ITouchEvents, Runnable {

        private float[] mDraggingOffset = null;

        @Override
        public void dispatchTouchEvent(MotionEvent event) {
            mRotationDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
        }

        @Override
        public void onGesture(GestureDetector detector) {
            if (!mCharacterView.isDragging()) {
                Log.d(TAG, "onGesture detected");
                mDraggingOffset = null;
                if (detector == mRotationDetector) {
                    mCharacterView.rotate(detector.getValue());
                } else if (detector == mScaleDetector) {
                    mCharacterView.scale(detector.getValue());
                }
            }
        }

        @Override
        public void onEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            Log.d(TAG, "onTouchStart " + sceneObject.getName());
            if (mCharacterView.isDragging()) {
                return;
            }

            if (CharacterView.PET_COLLIDER.equals(sceneObject.getName()) && mDraggingOffset == null) {
                mDraggingOffset = pickedObject.hitLocation;
                mPetContext.runDelayedOnPetThread(this, ViewConfiguration.getLongPressTimeout());
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            Log.d(TAG, "onTouchEnd");
            if (mCharacterView.isDragging() && sceneObject == mCharacterView.getBoundaryPlane()) {
                Log.d(TAG, "onDrag stop");
                mCharacterView.stopDragging();
                mDraggingOffset = null;
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            if (mCharacterView.isDragging()) {
                // Use hitlocation from plane only
                if (sceneObj == mCharacterView.getBoundaryPlane()) {
                    float[] hit = collision.getHitLocation();

                    Matrix4f mat = sceneObj.getTransform().getModelMatrix4f();
                    Vector4f hitVector = new Vector4f(hit[0], hit[1], hit[2], 0);
                    hitVector.mul(mat);

                    // FIXME: make the pet be put inside the plane only
                    // Set the pet's position according to plane and hit location
                    mCharacterView.getTransform().setPosition(mat.m30() + hitVector.x,
                            mat.m31(), mat.m32() + hitVector.z);
                }
            }
        }

        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {

        }

        @Override
        public void run() {
            if (mDraggingOffset != null) {
                Log.d(TAG, "onDrag start");
                mCharacterView.startDragging();
                mDraggingOffset = null;
            }
        }
    }
}
