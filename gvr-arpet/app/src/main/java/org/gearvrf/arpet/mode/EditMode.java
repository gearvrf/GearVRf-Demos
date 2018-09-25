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

import android.gesture.Gesture;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
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
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.joml.Vector3f;

public class EditMode extends BasePetMode {
    private OnBackToHudModeListener mBackToHudModeListener;
    private final CharacterView mCharacterView;
    private final GVRMixedReality mMixedReality;
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
            mBackToHudModeListener.OnBackToHud();
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
            if (CharacterView.PET_COLLIDER.equals(sceneObject.getName()) && mDraggingOffset == null) {
                mDraggingOffset = pickedObject.hitLocation;
                mPetContext.runDelayedOnPetThread(this, ViewConfiguration.getLongPressTimeout());
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {
            Log.d(TAG, "onTouchEnd");
            if (mCharacterView.isDragging()) {
                Log.d(TAG, "onDrag stop");
                mCharacterView.stopDragging();
                mDraggingOffset = null;
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            if (mCharacterView.isDragging()) {
                collision = testDraggingOnPlane(mMixedReality.getPassThroughObject());
                if (collision != null) {
                    GVRHitResult gvrHitResult = mMixedReality.hitTest(mMixedReality.getPassThroughObject(),
                            collision);
                    if (gvrHitResult != null) {
                        mCharacterView.updatePose(gvrHitResult.getPose());
                    }
                }
            }
        }

        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {

        }

        private GVRPicker.GVRPickedObject testDraggingOnPlane(GVRSceneObject sceneObject) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            mCursorController.getPicker().getWorldPickRay(origin, direction);
            //direction.sub(mDraggingOffset[0], mDraggingOffset[1], mDraggingOffset[2]);
            direction.normalize();

            return GVRPicker.pickSceneObject(sceneObject, origin.x, origin.y, origin.z,
                    direction.x, direction.y, direction.z);
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
