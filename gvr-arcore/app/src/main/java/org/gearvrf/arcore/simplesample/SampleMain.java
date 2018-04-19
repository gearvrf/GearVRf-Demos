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

import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;

public class SampleMain extends GVRMain {
    private static String TAG = "GVR_ARCORE";

    private static float PASSTHROUGH_DISTANCE = 100.0f;
    private static float AR2VR_SCALE = 100;

    private ARCoreHelper mARCoreHelper;
    private Vector3f mDisplayGeometry;

    private GVRScene mVRScene;
    private Session mARCoreSession;
    private GVRSceneObject mARPassThroughObject;
    private Frame mLastARFrame;
    private ARCoreHandler mARCoreHandler;
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;

    /* From AR to GVR space matrices */
    private float[] mGVRModelMatrix = new float[16];
    private float[] mARViewMatrix = new float[16];
    private float[] mGVRCamMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];

    public SampleMain() {}

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);
        if (mARCoreSession == null) {
            Log.e(TAG, "Invalid ARCore session!");
            return;
        }

        mVRScene = gvrContext.getMainScene();

        mARCoreHelper = new ARCoreHelper(gvrContext, mVRScene);

        gvrContext.registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float v) {
                gvrContext.unregisterDrawFrameListener(this);
                try {
                    onInitARCoreSession(gvrContext);
                     /* Cursor controller */
                    initCursorController(gvrContext);
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onInitARCoreSession(GVRContext gvrContext) throws CameraNotAvailableException {
        GVRTexture passThroughTexture = new GVRExternalTexture(gvrContext);

        mARCoreSession.setCameraTextureName(passThroughTexture.getId());

        // FIXME: detect VR screen aspect ratio. Using empirical 16:9 aspect ratio
        /* Try other aspect ration whether virtual objects looks jumping ou sliding
        during camera's rotation.
         */
        mARCoreSession.setDisplayGeometry(Surface.ROTATION_90 , 160, 90);

        mLastARFrame = mARCoreSession.update();
        mDisplayGeometry = configDisplayGeometry(mLastARFrame.getCamera());

        mARCoreSession.setDisplayGeometry(Surface.ROTATION_90 ,
                    (int)mDisplayGeometry.x, (int)mDisplayGeometry.y);

        /* To render texture from phone's camera */
        mARPassThroughObject = new GVRSceneObject(gvrContext, mDisplayGeometry.x, mDisplayGeometry.y,
                passThroughTexture, GVRMaterial.GVRShaderType.OES.ID);

        mARPassThroughObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        mARPassThroughObject.getRenderData().setDepthTest(false);
        mARPassThroughObject.getTransform().setPosition(0, 0, mDisplayGeometry.z);
        mARPassThroughObject.attachComponent(new GVRMeshCollider(gvrContext, true));

        mVRScene.addSceneObject(mARPassThroughObject);

        /* AR main loop */
        mARCoreHandler = new ARCoreHandler();
        gvrContext.registerDrawFrameListener(mARCoreHandler);

        mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();

        updateAR2GVRMatrices(mLastARFrame.getCamera(), mVRScene.getMainCameraRig());
    }

    /**
     * Initialize GearVR controller handler.
     *
     * @param gvrContext GVRf context.
     */
    private void initCursorController(GVRContext gvrContext) {
        mVRScene.getEventReceiver().addListener(mARCoreHandler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * PASSTHROUGH_DISTANCE,
                        0.2f * PASSTHROUGH_DISTANCE),
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
                    oldController.removePickEventListener(mARCoreHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mARCoreHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-PASSTHROUGH_DISTANCE);
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
        private Vector2f mSingleTapPos = null;

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

            List<HitResult> hitResult = null;

            if (mSingleTapPos != null) {
                hitResult = arFrame.hitTest(mSingleTapPos.x, mSingleTapPos.y);
                mSingleTapPos = null;
            }

            mARCoreHelper.updateVirtualObjects(hitResult,
                    mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

            mARCoreHelper.updateVirtualPlanes(mARCoreSession.getAllTrackables(Plane.class),
                    mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

            mLastARFrame = arFrame;

            // Update current VR cam's matrix to next update of passtrhough and virtual objects.
            // AR/30fps vs VR/60fps
            mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            mSingleTapPos = convertToDisplayGeometrySpace(collision.getHitLocation());
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

    /**
     * Calc the with and height for the passthrough object according to the distance
     * and aspect ratio of ARCore's camera.
     *
     * @param arCamera
     * @return
     */
    private static Vector3f configDisplayGeometry(Camera arCamera) {
        float near = 0.1f;
        float far = 100.0f;

        // Get phones' cam projection matrix.
        float[] m = new float[16];
        arCamera.getProjectionMatrix(m, 0, near, far);
        Matrix4f projmtx = new Matrix4f();
        projmtx.set(m);

        float aspectRatio = projmtx.m11()/projmtx.m00();
        float arCamFOV = projmtx.perspectiveFov();

        float quadDistance = PASSTHROUGH_DISTANCE;
        float quadHeight = new Float(2 * quadDistance * Math.tan(arCamFOV * 0.5f));
        float quadWidth = quadHeight * aspectRatio;

        Log.d(TAG, "ARCore configured to: passthrough[w: "
                + quadWidth + ", h: " + quadHeight +", z: " + quadDistance
                + "], cam fov: " +Math.toDegrees(arCamFOV) + ", aspect ratio: " + aspectRatio);

        return new Vector3f(quadWidth, quadHeight, -PASSTHROUGH_DISTANCE);
    }
}
