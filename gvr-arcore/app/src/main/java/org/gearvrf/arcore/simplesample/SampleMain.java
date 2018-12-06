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

package org.gearvrf.arcore.simplesample;

import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


public class SampleMain extends GVRMain {
    private static String TAG = "GVR_ARCORE";
    private static int MAX_VIRTUAL_OBJECTS = 20;

    private GVRContext mGVRContext;
    private GVRScene mainScene;

    private GVRMixedReality mixedReality;
    private SampleHelper helper;
    private TouchHandler mTouchHandler;



    private List<GVRAnchor> mVirtualObjects;
    private int mVirtObjCount = 0;


    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        helper = new SampleHelper();
        mTouchHandler = new TouchHandler();
        mVirtualObjects = new ArrayList<>() ;
        mVirtObjCount = 0;

        helper.initCursorController(gvrContext, mTouchHandler);


        mixedReality = new GVRMixedReality(gvrContext, mainScene);
        mixedReality.registerPlaneListener(planeEventsListener);
        mixedReality.registerAnchorListener(anchorEventsListener);
        mixedReality.resume();

    }

    @Override
    public void onStep() {
        super.onStep();
        for (GVRAnchor anchor: mVirtualObjects) {
            for (GVRSceneObject obj: anchor.getChildren()) {
                ((VirtualObject)obj).reactToLightEnvironment(
                        mixedReality.getLightEstimate().getPixelIntensity());
            }
        }
    }

    private IPlaneEventsListener planeEventsListener = new IPlaneEventsListener() {
        @Override
        public void onPlaneDetection(GVRPlane gvrPlane) {
            gvrPlane.setSceneObject(helper.createQuadPlane(getGVRContext()));
            mainScene.addSceneObject(gvrPlane);
        }

        @Override
        public void onPlaneStateChange(GVRPlane gvrPlane, GVRTrackingState gvrTrackingState) {
            if (gvrTrackingState != GVRTrackingState.TRACKING) {
                gvrPlane.setEnable(false);
            }
            else {
                gvrPlane.setEnable(true);
            }
        }

        @Override
        public void onPlaneMerging(GVRPlane gvrPlane, GVRPlane gvrPlane1) {
        }
    };

    private IAnchorEventsListener anchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {
            if (gvrTrackingState != GVRTrackingState.TRACKING) {
                gvrAnchor.setEnable(false);
            }
            else {
                gvrAnchor.setEnable(true);
            }
        }
    };

    public class TouchHandler extends GVREventListeners.TouchEvents {
        private GVRSceneObject mDraggingObject = null;
        private float mHitX;
        private float mHitY;
        private float mYaw;
        private float mScale;


        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onEnter(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject() || mDraggingObject != null) {
                return;
            }

            ((VirtualObject)sceneObj).onPickEnter();
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onExit(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                if (mDraggingObject != null) {
                    ((VirtualObject) mDraggingObject).onPickExit();
                    mDraggingObject = null;
                }
                return;
            }

            if (mDraggingObject == null) {
                ((VirtualObject) sceneObj).onPickExit();
            }
        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onTouchStart(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                return;
            }

            if (mDraggingObject == null && pickInfo.motionEvent != null) {
                mDraggingObject = sceneObj;

                mYaw = sceneObj.getTransform().getRotationYaw();
                mScale = sceneObj.getTransform().getScaleX();

                mHitX = pickInfo.motionEvent.getX();
                mHitY = pickInfo.motionEvent.getY();

                Log.d(TAG, "onStartDragging");
                ((VirtualObject)sceneObj).onTouchStart();
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onTouchEnd(sceneObj, pickInfo);


            if (mDraggingObject != null) {
                Log.d(TAG, "onStopDragging");

                if (pickSceneObject(mDraggingObject) == null) {
                    ((VirtualObject) mDraggingObject).onPickExit();
                } else {
                    ((VirtualObject)mDraggingObject).onTouchEnd();
                }
                mDraggingObject = null;
            } else if (sceneObj == mixedReality.getPassThroughObject()) {
                onSingleTap(sceneObj, pickInfo);
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onInside(sceneObj, pickInfo);

            if (mDraggingObject == null) {
                return;
            } else if (pickInfo.motionEvent != null){
                // get the current x,y hit location
                float hitLocationX = pickInfo.motionEvent.getX();
                float hitLocationY = pickInfo.motionEvent.getY();

                // find the diff from when we first touched down
                float diffX = hitLocationX - mHitX;
                float diffY = (hitLocationY - mHitY) / 100.0f;

                // when we move along X, calculate an angle to rotate the model around the Y axis
                float angle = mYaw + (diffX * 2);

                // when we move along Y, calculate how much to scale the model
                float scale = mScale + (diffY);
                if(scale < 0.1f) {
                    scale = 0.1f;
                }

                // set rotation and scale
                mDraggingObject.getTransform().setRotationByAxis(angle, 0.0f, 1.0f, 0.0f);
                mDraggingObject.getTransform().setScale(scale, scale, scale);
            }


            pickInfo = pickSceneObject(mixedReality.getPassThroughObject());
            if (pickInfo != null) {
                GVRHitResult gvrHitResult = mixedReality.hitTest(
                        mixedReality.getPassThroughObject(), pickInfo);

                if (gvrHitResult != null) {
                    mixedReality.updateAnchorPose((GVRAnchor)mDraggingObject.getParent(),
                            gvrHitResult.getPose());
                }
            }
        }

        private GVRPicker.GVRPickedObject pickSceneObject(GVRSceneObject sceneObject) {
            Vector3f origin = new Vector3f();
            Vector3f direction = new Vector3f();

            helper.getCursorController().getPicker().getWorldPickRay(origin, direction);

            return GVRPicker.pickSceneObject(sceneObject, origin.x, origin.y, origin.z,
                    direction.x, direction.y, direction.z);
        }

        private void onSingleTap(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            GVRHitResult gvrHitResult = mixedReality.hitTest(sceneObj, collision);
            VirtualObject andy = new VirtualObject(mGVRContext);

            if (gvrHitResult == null) {
                return;
            }

            addVirtualObject(gvrHitResult.getPose(), andy);
        }
    }

    private void addVirtualObject(float[] pose, VirtualObject andy) {
        GVRAnchor anchor;

        if (mVirtObjCount < MAX_VIRTUAL_OBJECTS) {
             anchor = mixedReality.createAnchor(pose);
             anchor.attachSceneObject(andy);

            mainScene.addSceneObject(anchor);
            mVirtualObjects.add(anchor);
        }
        else {
            anchor = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
            mixedReality.updateAnchorPose(anchor, pose);
        }

        anchor.setName("id: " + mVirtObjCount);
        Log.d(TAG, "New virtual object " + anchor.getName());

        mVirtObjCount++;
    }
}