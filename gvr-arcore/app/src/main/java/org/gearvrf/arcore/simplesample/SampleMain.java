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
import org.gearvrf.GVRBoxCollider;
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
import org.gearvrf.GVRTransform;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.joml.Matrix4f;
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
        gvrContext.runOnGlThreadPostRender(1, new Runnable()
        {
            public void run()
            {
                mixedReality.getPassThroughObject().setName("PassThru");
                mixedReality.getPassThroughObject().getEventReceiver().addListener(mTouchHandler);
            }
        });
    }


    private GVRSceneObject load3dModel(final GVRContext gvrContext) throws IOException
    {
        final GVRSceneObject sceneObject = gvrContext.getAssetLoader().loadModel("objects/andy.obj");
        sceneObject.attachComponent(new GVRBoxCollider(gvrContext));
        sceneObject.getEventReceiver().addListener(new SelectionHandler());
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
        for (GVRAnchor anchor : mVirtualObjects)
        {
            GVRSceneObject owner = anchor.getOwnerObject();
            if (owner != null)
            {
                owner.forAllDescendants(lightEstimator);
            }
        }
    }

    private IPlaneEventsListener planeEventsListener = new IPlaneEventsListener()
    {
        @Override
        public void onPlaneDetection(GVRPlane gvrPlane)
        {
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

    public class SelectionHandler extends GVREventListeners.TouchEvents
    {
        private float mHitX;
        private float mHitY;
        private float mYaw;
        private float mScale;
        private final float[] UNPICKED_COLOR = { 1, 1, 1, 1.0f };
        private final float[] PICKED_COLOR = { 1.0f, 0.0f, 0.0f, 1.0f };
        private final float[] CLICKED_COLOR = { 0.5f, 0.5f, 1.0f, 1.0f };
        private float[] current_color = UNPICKED_COLOR;

        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            current_color = PICKED_COLOR;
            sceneObj.forAllComponents(colorChanger, GVRRenderData.getComponentType());
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            current_color = UNPICKED_COLOR;
            sceneObj.forAllComponents(colorChanger, GVRRenderData.getComponentType());
        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            mYaw = sceneObj.getTransform().getRotationYaw();
            mScale = sceneObj.getTransform().getScaleX();
            if (pickInfo.motionEvent != null)
            {
                mHitX = pickInfo.motionEvent.getX();
                mHitY = pickInfo.motionEvent.getY();
            }
            Log.d(TAG, "onStartDragging");
            current_color = CLICKED_COLOR;
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            Log.d(TAG, "onStopDragging");
            current_color = PICKED_COLOR;
            sceneObj.forAllComponents(colorChanger, GVRRenderData.getComponentType());
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if (!pickInfo.touched || (pickInfo.motionEvent == null))
            {
                return;
            }
            // get the current x,y hit location
            float hitLocationX = pickInfo.motionEvent.getX();
            float hitLocationY = pickInfo.motionEvent.getY();

            // find the diff from when we first touched down
            float diffX = hitLocationX - mHitX;
            float diffY = (hitLocationY - mHitY) / 100.0f;

            // when we move along X, calculate an angle to rotate the model around the Y axis
            float angle = mYaw + diffX * 0.3f;

            // when we move along Y, calculate how much to scale the model
            float scale = mScale + (diffY);
            if (scale < 0.1f)
            {
                scale = 0.1f;
            }
            GVRTransform t = sceneObj.getTransform();
            Matrix4f mtx = t.getLocalModelMatrix4f();

            mtx.rotate(angle, 0, 1, 0);
            mtx.scale(scale, scale, scale);

            // set rotation and scale
            t.setModelMatrix(mtx);
            GVRHitResult gvrHitResult = mixedReality.hitTest(pickInfo);
            if (gvrHitResult != null)
            {
                GVRAnchor anchor = (GVRAnchor) sceneObj.getComponent(GVRAnchor.getComponentType());
                if (anchor != null)
                {
                    mixedReality.updateAnchorPose(anchor, gvrHitResult.getPose());
                }
            }
        }

        private GVRSceneObject.ComponentVisitor colorChanger = new GVRSceneObject.ComponentVisitor()
        {
            @Override
            public boolean visit(GVRComponent component)
            {
                GVRRenderData rdata = (GVRRenderData) component;
                GVRMaterial mtl = rdata.getMaterial();
                mtl.setDiffuseColor(current_color[0], current_color[1], current_color[2], 1);
                return false;
            }
        };
    };

    public class TouchHandler extends GVREventListeners.TouchEvents
    {
        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
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
                if (v.length() < 5)
                {
                    return anchor;
                }
            }
            return null;
        }
    };

}