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

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
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
import org.joml.Vector4f;

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


    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        helper = new SampleHelper();
        mTouchHandler = new TouchHandler();
        mVirtualObjects = new ArrayList<>() ;
        mVirtObjCount = 0;

        helper.initCursorController(gvrContext, mTouchHandler);


        mixedReality = new GVRMixedReality(mainScene);
        mixedReality.registerPlaneListener(planeEventsListener);
        mixedReality.registerAnchorListener(anchorEventsListener);
        mixedReality.resume();

    }


    private GVRSceneObject load3dModel(final GVRContext gvrContext) throws IOException
    {
        final GVRSceneObject sceneObject = gvrContext.getAssetLoader().loadModel("objects/andy.obj");
        sceneObject.forAllComponents(new GVRSceneObject.ComponentVisitor()
        {
            @Override
            public boolean visit(GVRComponent component)
            {
                component.getOwnerObject().attachComponent(new GVRMeshCollider(component.getGVRContext(), false));
                return false;
            }
        }, GVRRenderData.getComponentType());
        return sceneObject;
    }

    GVRSceneObject.SceneVisitor lightEstimator = new GVRSceneObject.SceneVisitor()
    {
        @Override
        public boolean visit(GVRSceneObject obj)
        {
            GVRRenderData rdata = obj.getRenderData();
            if (rdata != null)
            {
                GVRMaterial mtl = rdata.getMaterial();
                float[] color = mtl.getDiffuseColor();
                float lightEstimate = mixedReality.getLightEstimate().getPixelIntensity();

                mtl.setDiffuseColor(
                    color[0] * lightEstimate, color[1] * lightEstimate,
                    color[2] * lightEstimate, color[3]);
            }
            return false;
        }
    };

    @Override
    public void onStep()
    {
        super.onStep();
        for (GVRAnchor anchor: mVirtualObjects)
        {
            GVRSceneObject owner = anchor.getOwnerObject();
            if (owner != null)
            {
                owner.forAllDescendants(lightEstimator);
            }
        }
    }

    private IPlaneEventsListener planeEventsListener = new IPlaneEventsListener() {
        @Override
        public void onPlaneDetection(GVRPlane gvrPlane) {
            GVRSceneObject planeMesh = helper.createQuadPlane(getGVRContext());

            planeMesh.attachComponent(gvrPlane);
            mainScene.addSceneObject(planeMesh);
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

            if (sceneObj == mixedReality.getPassThroughObject() || mDraggingObject != null)
            {
                return;
            }
            Selector selector = (Selector) sceneObj.getComponent(Selector.getComponentType());
            if (selector == null)
            {
                selector = new Selector(sceneObj.getGVRContext());
                sceneObj.attachComponent(selector);
            }
            selector.onPickEnter();
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onExit(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject())
            {
                Selector selector = (Selector) mDraggingObject.getComponent(Selector.getComponentType());
                if (selector != null)
                {
                    selector.onPickExit();
                }
                mDraggingObject = null;
            }
        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onTouchStart(sceneObj, pickInfo);

            if (sceneObj == mixedReality.getPassThroughObject()) {
                return;
            }

            if (mDraggingObject == null) {
                mDraggingObject = sceneObj;

                mYaw = sceneObj.getTransform().getRotationYaw();
                mScale = sceneObj.getTransform().getScaleX();

                mHitX = pickInfo.motionEvent.getX();
                mHitY = pickInfo.motionEvent.getY();

                Log.d(TAG, "onStartDragging");
                Selector selector = (Selector) mDraggingObject.getComponent(Selector.getComponentType());
                if (selector != null)
                {
                    selector.onTouchStart();
                }
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onTouchEnd(sceneObj, pickInfo);
            if (mDraggingObject != null)
            {
                Log.d(TAG, "onStopDragging");
                Selector selector = (Selector) mDraggingObject.getComponent(Selector.getComponentType());

                if (selector != null)
                {
                    if(pickSceneObject(mDraggingObject) == null)
                    {
                        selector.onPickExit();
                    }
                    else
                    {
                        selector.onTouchEnd();
                    }
                    mDraggingObject = null;
                }
            }
            else if (sceneObj == mixedReality.getPassThroughObject())
            {
                onSingleTap(sceneObj, pickInfo);
            }
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onInside(sceneObj, pickInfo);

            if (mDraggingObject == null) {
                return;
            } else {
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
            if (pickInfo != null)
            {
                GVRHitResult gvrHitResult = mixedReality.hitTest(mixedReality.getPassThroughObject(), pickInfo);
                if (gvrHitResult != null)
                {
                    GVRAnchor anchor = (GVRAnchor) mDraggingObject.getComponent(GVRAnchor.getComponentType());
                    if (anchor != null)
                    {
                        mixedReality.updateAnchorPose(anchor, gvrHitResult.getPose());
                    }
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

            if (gvrHitResult == null)
            {
                return;
            }
            try
            {
                GVRSceneObject andy = load3dModel(getGVRContext());
                addVirtualObject(gvrHitResult.getPose(), andy);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                Log.e(TAG, ex.getMessage());
            }

        }
    }

    private void addVirtualObject(float[] pose, GVRSceneObject andy)
    {
        GVRAnchor anchor;

        if (mVirtObjCount < MAX_VIRTUAL_OBJECTS)
        {
            anchor = mixedReality.createAnchor(pose, andy);
            mVirtualObjects.add(anchor);
            mainScene.addSceneObject(andy);
        }
        else
        {
            anchor = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
            mixedReality.updateAnchorPose(anchor, pose);
        }
        mVirtObjCount++;
    }

    static class Selector extends GVRBehavior
    {
        private static long TYPE_SELECTIONHILITE = newComponentType(Selector.class);
        private static final float[] UNPICKED_COLOR = {0.7f, 0.7f, 0.7f, 1.0f};
        private static final float[] PICKED_COLOR = {1.0f, 0.0f, 0.0f, 1.0f};
        private static final float[] CLICKED_COLOR = {0.5f, 0.5f, 1.0f, 1.0f};
        private float[] current_color = UNPICKED_COLOR;

        public Selector(GVRContext gvrContext)
        {
            super(gvrContext);
        }

        static public long getComponentType() { return TYPE_SELECTIONHILITE; }

        public void onPickEnter()
        {
            current_color = PICKED_COLOR;
        }

        public void onPickExit() {
            current_color = UNPICKED_COLOR;
        }

        public void onTouchStart() {
            current_color = CLICKED_COLOR;
        }

        public void onTouchEnd() {
            current_color = PICKED_COLOR;
        }
    }
}