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

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;

import java.io.IOException;
import java.util.EnumSet;

public class SampleMain extends GVRMain implements IPlaneEventsListener, IAnchorEventsListener {
    GVRMixedReality mixedReality;
    GVRContext mGVRContext;
    GVRScene mainScene;
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;
    private TouchHandler mTouchHandler;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        mTouchHandler = new TouchHandler();
        initCursorController(gvrContext);


        mixedReality = new GVRMixedReality(gvrContext, mainScene);
        mixedReality.registerPlaneListener(this);
        mixedReality.registerAnchorListener(this);
    }

    @Override
    public void onPlaneDetection(GVRPlane gvrPlane) {
        gvrPlane.setSceneObject(createQuadPlane(getGVRContext()));
    }

    @Override
    public void onPlaneStateChange(GVRPlane gvrPlane, GVRTrackingState gvrTrackingState) {
        if (gvrTrackingState != GVRTrackingState.TRACKING) {
            gvrPlane.getSceneObject().setEnable(false);
        }
        else {
            gvrPlane.getSceneObject().setEnable(true);
        }
    }

    @Override
    public void onPlaneMerging(GVRPlane childPlane, GVRPlane parentPlane) {
    }

    @Override
    public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {
        if (gvrTrackingState != GVRTrackingState.TRACKING) {
            gvrAnchor.getSceneObject().setEnable(false);
        }
        else {
            gvrAnchor.getSceneObject().setEnable(true);
        }
    }


    private class TouchHandler extends GVREventListeners.TouchEvents {
        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);

            GVRHitResult gvrHitResult = mixedReality.hitTest(sceneObj, collision);
            GVRSceneObject andy = null;

            if (gvrHitResult == null) {
                return;
            }

            try {
                andy = mGVRContext.getAssetLoader().loadModel("objects/andy.obj");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            mixedReality.createAnchor(gvrHitResult.getPose(), andy);
        }
    }


    private void initCursorController(GVRContext gvrContext) {
        mainScene.getEventReceiver().addListener(mTouchHandler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * 100,
                        0.2f * 100),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mTouchHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-100f);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }
    
    /**
     * Set ARCore session
     * @param session ARCore Session instance.
     */
    public void setARCoreSession(Session session) {
        mARCoreSession = session;
    }

    /**
     * GL Thread to handle ARCore's updates.
     */
    public class ARCoreHandler extends GVREventListeners.TouchEvents
            implements GVRDrawFrameListener {
        private GVRSceneObject mDraggingObject = null;
        private float mHitX;
        private float mHitY;
        private float mYaw;
        private float mScale;

        @Override
        public void onDrawFrame(float v) {
            Frame arFrame;
            try {
                arFrame = mARCoreSession.update();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
                getGVRContext().unregisterDrawFrameListener(this);
                return;
            }

            Camera arCamera = arFrame.getCamera();

            if (arFrame.getTimestamp() == mLastARFrame.getTimestamp()) {
                // FIXME: ARCore works at 30fps.
                return;
            }

            if (arCamera.getTrackingState() != TrackingState.TRACKING) {
                // Put passthrough object in from of current VR cam at paused states.
                updateAR2GVRMatrices(arCamera, mVRScene.getMainCameraRig());
                updatePassThroughObject(mARPassThroughObject);

                mARCoreHelper.removeAllVirtualPlanes();
                mARCoreHelper.removeAllVirtualObjects();
                return;
            }

            // Update current AR cam's view matrix.
            arCamera.getViewMatrix(mARViewMatrix, 0);

            // Update passthrough object with last VR cam matrix
            updatePassThroughObject(mARPassThroughObject);

            mARCoreHelper.updateVirtualObjects(mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

            mARCoreHelper.updateVirtualPlanes(mARCoreSession.getAllTrackables(Plane.class),
                    mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

            mLastARFrame = arFrame;

            // Update current VR cam's matrix to next update of passtrhough and virtual objects.
            // AR/30fps vs VR/60fps
            mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();
        }

        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onEnter(sceneObj, pickInfo);

			if (sceneObj == mARPassThroughObject || mDraggingObject != null)
				return;

            ((VirtualObject)sceneObj).onPickEnter();
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            super.onExit(sceneObj, pickInfo);

			if (sceneObj == mARPassThroughObject) {
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

			if (sceneObj == mARPassThroughObject)
				return;

			if (mDraggingObject == null) {
                mDraggingObject = sceneObj;

                // save off the current yaw and scale
                mYaw = ((VirtualObject)sceneObj).getRotationYaw();
                mScale = ((VirtualObject)sceneObj).getScaleX();

                // save off the x,y hit location for use in onInside()
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
            } else if (sceneObj == mARPassThroughObject) {
                final Vector2f singleTapPos = convertToDisplayGeometrySpace(pickInfo.getHitLocation());
                onSingleTap(singleTapPos);
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
                ((VirtualObject)mDraggingObject).setRotation(angle, 0.0f, 1.0f, 0.0f);
                ((VirtualObject)mDraggingObject).setScale(scale, scale, scale);
            }

            pickInfo = pickSceneObject(mARPassThroughObject);
            if (pickInfo != null) {
                final Vector2f singleTapPos = convertToDisplayGeometrySpace(pickInfo.getHitLocation());
                final Anchor anchor = mARCoreHelper.createARCoreAnchor(
                        mLastARFrame.hitTest(singleTapPos.x, singleTapPos.y));

                if (anchor != null) {
                    ((VirtualObject)mDraggingObject).setARAnchor(anchor);
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

        private void onSingleTap(Vector2f singleTapPos) {
            if (mLastARFrame == null)
                return;

            mARCoreHelper.addVirtualObject(
                    mARCoreHelper.createARCoreAnchor(
                            mLastARFrame.hitTest(singleTapPos.x, singleTapPos.y)
                    )
            );
        }

        public Vector2f convertToDisplayGeometrySpace(float[] hitPoint) {
            final float hitX = hitPoint[0] + 0.5f * mDisplayGeometry.x;
            final float hitY = mDisplayGeometry.y - hitPoint[1] - 0.5f * mDisplayGeometry.y;

            return new Vector2f(hitX, hitY);
        }
    }

    private void updateAR2GVRMatrices(Camera arCamera, GVRCameraRig cameraRig) {
        arCamera.getViewMatrix(mARViewMatrix, 0);
        mGVRCamMatrix = cameraRig.getHeadTransform().getModelMatrix();
    }

    private void updatePassThroughObject(GVRSceneObject object) {
        Matrix.setIdentityM(mModelViewMatrix, 0);
        Matrix.translateM(mModelViewMatrix, 0, 0, 0, mDisplayGeometry.z);

        Matrix.multiplyMM(mGVRModelMatrix, 0, mGVRCamMatrix, 0, mModelViewMatrix, 0);

        object.getTransform().setModelMatrix(mGVRModelMatrix);
    }

    private int hsvHUE = 0;
    private GVRSceneObject createQuadPlane(GVRContext gvrContext) {
        GVRMesh mesh = GVRMesh.createQuad(gvrContext,
                "float3 a_position", 1.0f, 1.0f);

        GVRMaterial mat = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);

        GVRSceneObject polygonObject = new GVRSceneObject(gvrContext, mesh, mat);

        hsvHUE += 35;
        float[] hsv = new float[3];
        hsv[0] = hsvHUE % 360;
        hsv[1] = 1f; hsv[2] = 1f;

        int c =  Color.HSVToColor(50, hsv);
        mat.setDiffuseColor(Color.red(c) / 255f,Color.green(c) / 255f,
                Color.blue(c) / 255f, 0.2f);

        polygonObject.getRenderData().setMaterial(mat);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);

        return polygonObject;
    }
}