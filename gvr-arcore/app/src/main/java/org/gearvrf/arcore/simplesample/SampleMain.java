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
import android.view.MotionEvent;

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
import org.gearvrf.ITouchEvents;
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

/**
 * This sample illustrates how to load, place and move a 3D model
 * on a plane in the real world.
 */
public class SampleMain extends GVRMain {
    private static String TAG = "GVR_ARCORE";
    private static int MAX_VIRTUAL_OBJECTS = 20;

    private GVRContext mGVRContext;
    private GVRScene mainScene;
    private GVRMixedReality mixedReality;
    private SampleHelper helper;
    private DragHandler mTouchHandler;
    private List<GVRAnchor> mVirtualObjects;
    private int mVirtObjCount = 0;
    private GVRDirectLight mSceneLight;
    private SelectionHandler mSelector;

    /**
     * Initialize the MixedReality extension and
     * provide it with listeners for plane detection
     * and anchor tracking.
     *
     * A headlight is put in the scene to illuminate
     * objects the camera is pointed at.
     */
    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        helper = new SampleHelper();
        mTouchHandler = new DragHandler();
        mVirtualObjects = new ArrayList<>();
        mVirtObjCount = 0;
        mSceneLight = new GVRDirectLight(gvrContext);
        mainScene.getMainCameraRig().getHeadTransformObject().attachComponent(mSceneLight);
        mixedReality = new GVRMixedReality(mainScene);
        mixedReality.getEventReceiver().addListener(planeEventsListener);
        mixedReality.getEventReceiver().addListener(anchorEventsListener);
        mSelector = new SelectionHandler(gvrContext, mixedReality);
        mixedReality.resume();
    }


    /**
     * Loads a 3D model using the asset loaqder and attaches
     * a collider to it so it can be picked.
     * If you are using phone AR, the touch screen can
     * be used to drag, rotate or scale the object.
     * If you are using a headset, the controller
     * is used for picking and moving.
     */
    private GVRSceneObject load3dModel(final GVRContext gvrContext) throws IOException
    {
        final GVRSceneObject sceneObject = gvrContext.getAssetLoader().loadModel("objects/andy.obj");
        sceneObject.attachComponent(new GVRBoxCollider(gvrContext));
        sceneObject.getEventReceiver().addListener(mSelector);
        return sceneObject;
    }

    /**
     * The mixed reality extension runs in the background and does
     * light estimation. Each frame the intensity of the ambient
     * lighting is adjusted based on that estimate.
     */
    @Override
    public void onStep()
    {
        super.onStep();
        float lightEstimate = mixedReality.getLightEstimate().getPixelIntensity();
        mSceneLight.setAmbientIntensity(lightEstimate, lightEstimate, lightEstimate, 1);
        mSceneLight.setDiffuseIntensity(0.4f, 0.4f, 0.4f, 1);
        mSceneLight.setSpecularIntensity(0.2f, 0.2f, 0.2f, 1);
    }

    /**
     * The plane events listener handles plane detection events.
     * It also handles initialization and shutdown.
     */
    private IPlaneEvents planeEventsListener = new IPlaneEvents()
    {
        /**
         * Get the depth of the touch screen in the 3D world
         * and give it to the cursor controller so touch
         * events will be handled properly.
         */
        @Override
        public void onStartPlaneDetection(IMixedReality mr)
        {
            float screenDepth = mr.getScreenDepth();
            mr.getPassThroughObject().getEventReceiver().addListener(mTouchHandler);
            helper.initCursorController(mGVRContext, mTouchHandler, screenDepth);
        }

        @Override
        public void onStopPlaneDetection(IMixedReality mr) { }

        /**
         * Place a transparent quad in the 3D scene to indicate
         * vertically upward planes (floor, table top).
         * We don't need colliders on these since they are
         * not pickable.
          */
        @Override
        public void onPlaneDetected(GVRPlane gvrPlane)
        {
            if (gvrPlane.getPlaneType() == GVRPlane.Type.VERTICAL)
            {
                return;
            }
            GVRSceneObject planeMesh = helper.createQuadPlane(getGVRContext());

            planeMesh.attachComponent(gvrPlane);
            mainScene.addSceneObject(planeMesh);
        }

        /**
         * Show/hide the 3D plane node based on whether it
         * is being tracked or not.
         */
        @Override
        public void onPlaneStateChange(GVRPlane gvrPlane, GVRTrackingState state)
        {
            gvrPlane.setEnable(state == GVRTrackingState.TRACKING);
        }

        @Override
        public void onPlaneMerging(GVRPlane gvrPlane, GVRPlane gvrPlane1) { }
    };

    /**
     * Show/hide the 3D node associated with the anchor
     * based on whether it is being tracked or not.
     */
    private IAnchorEvents anchorEventsListener = new IAnchorEvents()
    {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState state)
        {
            gvrAnchor.setEnable(state == GVRTrackingState.TRACKING);
        }
    };

    /**
     * Handles selection hilighting, rotation and scaling
     * of currently selected 3D object.
     * A light attached to the parent of the
     * selected 3D object is used for hiliting it.
     * The root of the hierarchy can be rotated or scaled.
     */
    static public class SelectionHandler implements ITouchEvents
    {
        static final int DRAG = 1;
        static final int SCALE_ROTATE = -1;
        static final int UNTOUCHED = 0;
        static private GVRSceneObject mSelected = null;
        private int mSelectionMode = UNTOUCHED;
        private final float[] PICKED_COLOR = {0.4f, 0.6f, 0, 1.0f};
        private final float[] UPDATE_COLOR = {0.6f, 0, 0.4f, 1.0f};
        private final float[] DRAG_COLOR = {0, 0.6f, 0.4f, 1.0f};
        private GVRSceneObject mSelectionLight;
        private IMixedReality mMixedReality;
        private float mHitY;
        private float mHitX;

        public SelectionHandler(GVRContext ctx, IMixedReality mr)
        {
            super();
            mMixedReality = mr;
            mSelectionLight = new GVRSceneObject(ctx);
            mSelectionLight.setName("SelectionLight");
            GVRPointLight light = new GVRPointLight(ctx);
            light.setSpecularIntensity(0.1f, 0.1f, 0.1f, 0.1f);
            mSelectionLight.attachComponent(light);
            mSelectionLight.getTransform().setPositionZ(1.0f);
        }

        public static GVRSceneObject getSelected() { return mSelected; }

        /*
         * When entering an anchored object, it is hilited by
         * adding a point light under its parent.
         */
        public void onEnter(GVRSceneObject target, GVRPicker.GVRPickedObject pickInfo)
        {
            if (mSelected != null)
            {
                return;
            }
            GVRPointLight light =
                (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
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
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if ((mSelected == sceneObj) || (mSelected == null))
            {
                mSelectionLight.getComponent(GVRLight.getComponentType()).disable();
                mSelected = null;
            }
        }

        /*
         * The color of the selection light changes when the object is being dragged.
         * If another object is already selected, ignore the touch event.
         */
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if (pickInfo.motionEvent == null)
            {
                return;
            }
            if (mSelected == null)
            {
                startTouch(sceneObj,
                           pickInfo.motionEvent.getX(),
                           pickInfo.motionEvent.getY(),
                           SCALE_ROTATE);
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }

        public void onMotionOutside(GVRPicker picker, MotionEvent event) { }

        /*
         * Rotate and scale the object relative to its current state.
         * The node being rotated / scaled is a child
         * of the anchored object (which is being oriented and positioned
         * by MixedReality).
         */
        private void scaleRotate(float rotateDelta, float scaleDelta)
        {
            GVRSceneObject selected = getSelected();
            GVRTransform t = selected.getTransform();
            float scale = t.getScaleX();
            Quaternionf q = new Quaternionf();
            Vector3f ea = new Vector3f();
            float angle = rotateDelta / 10.0f;

            /*
             * rotate about Y axis
             */
            q.set(t.getRotationX(), t.getRotationY(), t.getRotationZ(), t.getRotationW());
            q.getEulerAnglesXYZ(ea);
            q.rotateAxis(angle, 0, 1, 0);

            /*
             * scale the model
             */
            scale += scaleDelta / 20.0f;
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

        private void drag(float x, float y)
        {
            GVRAnchor anchor = (GVRAnchor) mSelected.getParent().getComponent(GVRAnchor.getComponentType());

            if (anchor != null)
            {
                GVRHitResult hit = mMixedReality.hitTest(x, y);

                if (hit != null)
                {                           // move the object to a new position
                    Log.d("NOLA", "Update Anchor");
                    mMixedReality.updateAnchorPose(anchor, hit.getPose());
                }
            }
        }

        public void update(GVRPicker.GVRPickedObject pickInfo)
        {
            float x = pickInfo.motionEvent.getX();
            float y = pickInfo.motionEvent.getY();

            if (mSelectionMode == SCALE_ROTATE)
            {
                float dx = (x - mHitX) / 100.0f;
                float dy = (y - mHitY) / 100.0f;
                Log.d("NOLA", "ScaleRotate %f, %f", dx, dy);
                scaleRotate(dx, dy);
            }
            else if (mSelectionMode == DRAG)
            {
                Log.d("NOLA", "Drag %f, %f", x, y);
                drag(x, y);
            }
        }

        public void startTouch(GVRSceneObject sceneObj, float hitx, float hity, int mode)
        {
            GVRPointLight light =
                (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            mSelectionMode = mode;
            mSelected = sceneObj;
            if (mode == DRAG)
            {
                light.setDiffuseIntensity(DRAG_COLOR[0],
                                          DRAG_COLOR[1],
                                          DRAG_COLOR[1],
                                          DRAG_COLOR[2]);
            }
            else
            {
                light.setDiffuseIntensity(UPDATE_COLOR[0],
                                          UPDATE_COLOR[1],
                                          UPDATE_COLOR[1],
                                          UPDATE_COLOR[2]);
            }
            mHitX = hitx;
            mHitY = hity;
            Log.d("NOLA", "Start Touch");
        }

        public void endTouch()
        {
            GVRPointLight light =
                (GVRPointLight) mSelectionLight.getComponent(GVRLight.getComponentType());
            light.setDiffuseIntensity(PICKED_COLOR[0],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[1],
                                      PICKED_COLOR[2]);
            mSelected = null;
            mSelectionMode = UNTOUCHED;
            Log.d("NOLA", "End Touch");
        }
    }


    /**
     * Handles touch events for the screen
     * (those not inside 3D anchored objects).
     * If phone AR isbeing used with passthru video,
     * the object displaying the camera output also
     * has a collider and is touchable.
     * This is how picking is handled when using
     * the touch screen.
     *
     * Tapping the screen or clicking on a plane
     * will cause a 3D object to be placed there.
     * Dragging with the controller or your finger
     * inside the object will scale it (Y direction)
     * and rotate it (X direction). Dragging outside
     * a 3D object will drag the currently selected
     * object (the last one you added/manipulated).
     */
    public class DragHandler extends GVREventListeners.TouchEvents
    {

        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        { }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            if (SelectionHandler.getSelected() != null)
            {
                mSelector.endTouch();
            }
            else
            {
                GVRAnchor anchor = findAnchorNear(pickInfo.hitLocation[0],
                                                  pickInfo.hitLocation[1],
                                                  pickInfo.hitLocation[2],
                                                  500);
                if (anchor != null)
                {
                    return;
                }
                float x = pickInfo.motionEvent.getX();
                float y = pickInfo.motionEvent.getY();
                GVRHitResult hit = mixedReality.hitTest(x, y);
                if (hit != null)
                {
                    addVirtualObject(hit.getPose());
                }
            }
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            GVRSceneObject selected = mSelector.getSelected();

            if (pickInfo.motionEvent == null)
            {
                return;
            }
            if (pickInfo.touched)           // currently touching an object?
            {
                if (selected != null)       // is a 3D object selected?
                {
                    mSelector.update(pickInfo);
                }
                else
                {
                    GVRAnchor anchor = findAnchorNear(pickInfo.hitLocation[0],
                                                      pickInfo.hitLocation[1],
                                                      pickInfo.hitLocation[2],
                                                      200);
                    if (anchor != null)
                    {
                        selected = anchor.getOwnerObject();
                        mSelector.startTouch(selected.getChildByIndex(0),
                                             pickInfo.motionEvent.getX(),
                                             pickInfo.motionEvent.getY(),
                                             SelectionHandler.DRAG);
                    }
                }
            }
        }

        /**
         * Load a 3D model and place it in the virtual world
         * at the given position. The pose is a 4x4 matrix
         * giving the real world position/orientation of
         * the object. We create an anchor (and a corresponding
         * node) to link the real and virtual pose together.
         * The node attached to the anchor will be moved and
         * oriented by the framework, anything you do
         * to the transform of this node will be discarded
         * (which is why we scale/rotate the child instead).
         * @param pose
         */
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

        /**
         * Look for a 3D object in the scene near the given position.
         * Used ro prevent objects from being placed too close together.
         */
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