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

import android.util.DisplayMetrics;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEvents;
import org.gearvrf.mixedreality.IMixedReality;
import org.gearvrf.mixedreality.IPlaneEvents;
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
        mixedReality = new GVRMixedReality(mainScene);
        mixedReality.getEventReceiver().addListener(planeEventsListener);
        mixedReality.getEventReceiver().addListener(anchorEventsListener);
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

    private IPlaneEvents planeEventsListener = new IPlaneEvents()
    {
        @Override
        public void onStartPlaneDetection(IMixedReality mr)
        {
            float screenDepth = mr.getScreenDepth();
            helper.initCursorController(mGVRContext, mTouchHandler, screenDepth);
        }

        @Override
        public void onStopPlaneDetection(IMixedReality mr) { }

        @Override
        public void onPlaneDetected(GVRPlane gvrPlane)
        {
            if (gvrPlane.getPlaneType() == GVRPlane.Type.VERTICAL)
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

    private IAnchorEvents anchorEventsListener = new IAnchorEvents() {
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
        private boolean mIsTouched = false;

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

        public boolean isTouched() { return mIsTouched; }

        /*
         * When entering an anchored object, it is hilited by
         * adding a point light under its parent.
         */
        public void onEnter(GVRSceneObject target)
        {
            mTarget = target;
            GVRPointLight light = (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
            GVRSceneObject lightParent = mSelectionLight.getParent();
            GVRSceneObject targetParent = target.getParent();

            if (lightParent != null)
            {
                if (lightParent != targetParent)
                {
                    lightParent.removeChildObject(mSelectionLight);
                    targetParent.addChildObject(mSelectionLight);
                    mSelectionLight.getComponent(GVRLight.getComponentType()).enable();
                }
                else
                {
                    mSelectionLight.getComponent(GVRLight.getComponentType()).enable();
                }
            }
            else
            {
                targetParent.addChildObject(mSelectionLight);
                mSelectionLight.getComponent(GVRLight.getComponentType()).enable();
            }
        }

        /*
         * When the object is no longer selected, its selection light is disabled.
         */
        public void onExit()
        {
            mSelectionLight.getComponent(GVRLight.getComponentType()).disable();
            mTarget = null;
            mIsTouched = false;
        }

        /*
         * The color of the selection light changes when the object is being dragged
         */
        public void onTouch()
        {
            GVRPointLight light = (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(CLICKED_COLOR[0],
                                      CLICKED_COLOR[1],
                                      CLICKED_COLOR[1],
                                      CLICKED_COLOR[2]);
            mIsTouched = true;
        }

        public void onTouchEnd()
        {
            GVRPointLight light = (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
            mIsTouched = false;
        }

        /*
         * When the object is responding to input, movement along the X axis
         * will rotate the object about Y and movement along the Y axis
         * will scale the object. The object being rotated / scaled is a child
         * of the anchored object (which is being oriented and positioned
         * by MixedReality).
         */
        public void onUpdate(float dx, float dy)
        {
            GVRSceneObject selected = getSelected();
            GVRTransform t = selected.getTransform();
            float scale = t.getScaleX();
            Quaternionf q = new Quaternionf();
            Vector3f ea = new Vector3f();
            float angle = dx * 4.0f;

            //
            // movement in X rotates about Y axis
            //
            q.set(t.getRotationX(), t.getRotationY(), t.getRotationZ(), t.getRotationW());
            q.getEulerAnglesXYZ(ea);
            q.rotateAxis(angle, 0, 1, 0);

            //
            // movement in Y scales the model
            //
            scale += dy / 10.0f;
            if (scale < 0.1f)
            {
                scale = 0.1f;
            }
            else if (scale > 50.0f)
            {
                scale = 50.0f;
            }
            t.setRotation(q.w, q.x, q.y, q.z);
            t.setScale(scale, scale, scale);
        }
    };

    public class TouchHandler extends GVREventListeners.TouchEvents
    {
        static final int DRAG = 1;
        static final int SCALE_ROTATE = -1;
        static final int UNTOUCHED = 0;
        private float mHitY;
        private float mHitX;
        private int mSelectionMode = UNTOUCHED; // 1 = dragging, -1 = moving, 0 = idle

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            GVRSceneObject parent = sceneObj.getParent();
            if (parent == null)
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
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            GVRSceneObject selected = mSelector.getSelected();
            GVRAnchor anchor = null;

            if (pickInfo.motionEvent == null)
            {
                return;
            }
            if (pickInfo.touched)
            {
                float x = pickInfo.motionEvent.getX();
                float y = pickInfo.motionEvent.getY();

                if (selected != null)
                {
                    if (mSelectionMode == DRAG)     // dragging the selected object?
                    {
                        anchor = (GVRAnchor) selected.getParent()
                                                     .getComponent(GVRAnchor.getComponentType());
                        GVRHitResult hit = mixedReality.hitTest(x, y);
                        if (hit != null)
                        {
                            mixedReality.updateAnchorPose(anchor, hit.getPose());
                            return;
                        }
                    }
                    else if (mSelectionMode == SCALE_ROTATE)    // rotating/scaling the selected object
                    {
                        final DisplayMetrics metrics = new DisplayMetrics();
                        getGVRContext().getActivity().getWindowManager().getDefaultDisplay()
                                       .getRealMetrics(metrics);
                        float dx = (x - mHitX) / metrics.widthPixels;
                        float dy = (y - mHitY) / metrics.heightPixels;
                        mSelector.onUpdate(dx, dy);
                        return;
                    }
                }
                GVRSceneObject par = sceneObj.getParent();
                if (par != null)
                {
                    anchor = (GVRAnchor) par.getComponent(GVRAnchor.getComponentType());
                    if (anchor != null)
                    {
                        mHitX = pickInfo.motionEvent.getX();
                        mHitY = pickInfo.motionEvent.getY();
                        if (mSelectionMode == 0)
                        {
                            mSelectionMode = SCALE_ROTATE;
                        }
                        mSelector.onEnter(sceneObj);
                        mSelector.onTouch();
                        return;
                    }
                }
                mSelectionMode = DRAG;
            }
            else
            {
                if (mSelector.isTouched())
                {
                    mSelector.onTouchEnd();
                }
                else if (mSelectionMode == DRAG)
                {
                    GVRHitResult hit = mixedReality.hitTest(pickInfo);
                    if (hit != null)
                    {
                        addVirtualObject(hit.getPose());
                    }
                }
                mSelectionMode = UNTOUCHED;
            }
        }

        private void addVirtualObject(float[] pose)
        {
            if (mVirtObjCount >= MAX_VIRTUAL_OBJECTS)
            {
                return;
            }
            try
            {
                GVRSceneObject andy = load3dModel(getGVRContext());
                GVRSceneObject anchorObj = mixedReality.createAnchorNode(pose);
                anchorObj.addChildObject(andy);
                GVRAnchor anchor = (GVRAnchor) anchorObj.getComponent(GVRAnchor.getComponentType());
                mVirtualObjects.add(anchor);
                mainScene.addSceneObject(anchorObj);
                mVirtObjCount++;
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                Log.e(TAG, ex.getMessage());
            }
        }

        private GVRAnchor findAnchorNear(float x, float y, float z, float maxdist)
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
                float d = v.length();
                if (d < maxdist)
                {
                    return anchor;
                }
            }
            return null;
        }
    };

}