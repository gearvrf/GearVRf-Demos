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

import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
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
    private GVRDirectLight mSceneLight;
    private SelectionHandler mSelector;
    private GVRRotationByAxisAnimation mOrbit = null;
    private float mOrbitDirection = 0;

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        helper = new SampleHelper();
        mTouchHandler = new TouchHandler();
        mVirtualObjects = new ArrayList<>();
        mVirtObjCount = 0;
        mSelector = new SelectionHandler();
        mSceneLight = new GVRDirectLight(gvrContext);
        mainScene.getMainCameraRig().getHeadTransformObject().attachComponent(mSceneLight);
        helper.initCursorController(gvrContext, mTouchHandler);
        mixedReality = new GVRMixedReality(mainScene);
        mixedReality.registerPlaneListener(planeEventsListener);
        mixedReality.registerAnchorListener(anchorEventsListener);
        mixedReality.resume();
    }


    private GVRSceneObject load3dModel(final GVRContext gvrContext) throws IOException
    {
        final GVRSceneObject sceneObject = gvrContext.getAssetLoader().loadModel("objects/andy.obj");
        sceneObject.attachComponent(new GVRBoxCollider(gvrContext));
        return sceneObject;
    }

    @Override
    public void onStep()
    {
        super.onStep();
        float lightEstimate = mixedReality.getLightEstimate().getPixelIntensity();
        mSceneLight.setAmbientIntensity(lightEstimate, lightEstimate, lightEstimate, 1);
        mSceneLight.setDiffuseIntensity(0.4f, 0.4f, 0.4f, 1);
        mSceneLight.setSpecularIntensity(0.2f, 0.2f, 0.2f, 1);
    }

    private IPlaneEventsListener planeEventsListener = new IPlaneEventsListener()
    {
        @Override
        public void onPlaneDetection(GVRPlane gvrPlane)
        {
            if (gvrPlane.getPlaneType() == GVRPlane.PlaneType.VERTICAL)
            {
                return;
            }
            GVRSceneObject planeMesh = helper.createQuadPlane(getGVRContext(), mixedReality.getARToVRScale());

            planeMesh.attachComponent(gvrPlane);
            mainScene.addSceneObject(planeMesh);
        }

        @Override
        public void onPlaneStateChange(GVRPlane gvrPlane, GVRTrackingState gvrTrackingState)
        {
            if (gvrTrackingState != GVRTrackingState.TRACKING)
            {
                gvrPlane.setEnable(false);
            }
            else
            {
                gvrPlane.setEnable(true);
            }
        }

        @Override
        public void onPlaneMerging(GVRPlane gvrPlane, GVRPlane gvrPlane1)
        {
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

    public class SelectionHandler
    {
        private final float[] PICKED_COLOR = { 0.4f, 0.6f, 0, 1.0f };
        private final float[] CLICKED_COLOR = { 0.6f, 0, 0.4f, 1.0f };
        private GVRSceneObject mSelectionLight;
        private GVRSceneObject mTarget = null;

        public SelectionHandler()
        {
            super();
            mSelectionLight = new GVRSceneObject(mGVRContext);
            mSelectionLight.setName("SelectionLight");
            GVRPointLight light = new GVRPointLight(mGVRContext);
            light.setSpecularIntensity(0.1f, 0.1f, 0.1f, 0.1f);
            mSelectionLight.attachComponent(light);
            mSelectionLight.getTransform().setPositionZ(1.0f);
        }

        public GVRSceneObject getSelected() { return mTarget; }

        public void onEnter(GVRSceneObject target)
        {
            mTarget = target;
            GVRPointLight light = (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
            GVRSceneObject lightParent = mSelectionLight.getParent();
            if (lightParent != null)
            {
                if (lightParent != target)
                {
                    lightParent.removeChildObject(mSelectionLight);
                    mTarget.addChildObject(mSelectionLight);
                    mSelectionLight.getComponent(GVRLight.getComponentType()).enable();
                }
                else
                {
                    mSelectionLight.getComponent(GVRLight.getComponentType()).enable();
                }
            }
            else
            {
                mTarget.addChildObject(mSelectionLight);
                mSelectionLight.getComponent(GVRLight.getComponentType()).enable();
            }
        }

        public void onExit()
        {
            mSelectionLight.getComponent(GVRLight.getComponentType()).disable();
            mTarget = null;
        }

        public void onTouch()
        {
            GVRPointLight light = (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(CLICKED_COLOR[0],
                                      CLICKED_COLOR[1],
                                      CLICKED_COLOR[1],
                                      CLICKED_COLOR[2]);
        }

        public void onTouchEnd()
        {
            GVRPointLight light = (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
        }

    };

    public class TouchHandler extends GVREventListeners.TouchEvents
    {
        private float[] mHitLoc;
        private float mHitY;
        private float mHitX;
        private boolean mIsDragging = false;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            GVRSceneObject parent = sceneObj.getParent();
            if ((parent == null) || (mSelector.getSelected() != null))
            {
                return;
            }
            GVRAnchor anchor = (GVRAnchor) parent.getComponent(GVRAnchor.getComponentType());

            if (anchor != null)
            {
                mSelector.onEnter(sceneObj);
            }
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if (!pickInfo.touched && (mSelector.getSelected() == sceneObj))
            {
                mSelector.onExit();
            }
        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if ((mSelector.getSelected() != null) && (pickInfo.motionEvent != null))
            {
                mHitX = pickInfo.motionEvent.getX();
                mHitY = pickInfo.motionEvent.getY();
                mHitLoc = pickInfo.getHitLocation();
                mSelector.onTouch();
                mIsDragging = true;
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if (mSelector.getSelected() != null)
            {
                if (mSelector.getSelected() == sceneObj)
                {
                    mSelector.onTouchEnd();
                }
                else
                {
                    mSelector.onExit();
                }
                mIsDragging = false;
                return;
            }
            GVRHitResult gvrHitResult = mixedReality.hitTest(pickInfo);

            if (gvrHitResult == null)
            {
                return;
            }
            GVRAnchor anchor = findAnchorNear(pickInfo.hitLocation[0],
                                              pickInfo.hitLocation[1],
                                              pickInfo.hitLocation[2]);
            if (anchor != null)
            {
                return;
            }
            if (mVirtObjCount >= MAX_VIRTUAL_OBJECTS)
            {
                anchor = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
                mixedReality.updateAnchorPose(anchor, gvrHitResult.getPose());
                return;
            }
            try
            {
                GVRSceneObject andy = load3dModel(getGVRContext());
                addVirtualObject(gvrHitResult.getPose(), andy);
                mSelector.onEnter(andy);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                Log.e(TAG, ex.getMessage());
            }
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            GVRSceneObject selected = mSelector.getSelected();
            if ((selected == null) || (pickInfo.motionEvent == null))
            {
                return;
            }
            selected = selected.getParent();
            GVRAnchor anchor = (GVRAnchor) selected.getComponent(GVRAnchor.getComponentType());
            if (pickInfo.touched && mIsDragging)
            {
                float x = pickInfo.motionEvent.getX();
                float y = pickInfo.motionEvent.getY();
                GVRHitResult hit = mixedReality.hitTest(x, y);
                if (hit != null)
                {
                    mixedReality.updateAnchorPose(anchor, hit.getPose());
                }
            }
        }

        private GVRSceneObject addVirtualObject(float[] pose, GVRSceneObject andy)
        {
            GVRAnchor anchor;
            GVRSceneObject anchorObj = new GVRSceneObject(getGVRContext());
            anchorObj.addChildObject(andy);
            anchor = mixedReality.createAnchor(pose);
            anchorObj.attachComponent(anchor);
            mVirtualObjects.add(anchor);
            mainScene.addSceneObject(anchorObj);
            mVirtObjCount++;
            return anchorObj;
        }

        private GVRAnchor findAnchorNear(float x, float y, float z)
        {
            Matrix4f anchorMtx = new Matrix4f();
            Vector3f v = new Vector3f();
            for (GVRAnchor anchor : mVirtualObjects)
            {
                float[] anchorPose = anchor.getPose();
                anchorMtx.set(anchorPose);
                anchorMtx.getTranslation(v);
                v.x -= x;
                v.y -= y;
                v.z -= z;
                if (v.length() < 100)
                {
                    return anchor;
                }
            }
            return null;
        }
    };

}