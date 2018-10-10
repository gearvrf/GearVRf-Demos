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
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.GVRTouchPadGestureListener;
import org.joml.Vector4f;

import java.util.EnumSet;


public class SampleHelper {
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;

    private Vector4f[] mColors;
    private int mPlaneIndex = 0;

    SampleHelper()
    {
        mColors = new Vector4f[]
        {
            new Vector4f(1, 0, 0, 0.3f),
            new Vector4f(0, 1, 0, 0.3f),
            new Vector4f(0, 0, 1, 0.3f),
            new Vector4f(1, 0, 1, 0.3f),
            new Vector4f(0, 1, 1, 0.3f),
            new Vector4f(1, 1, 0, 0.3f),
            new Vector4f(1, 1, 1, 0.3f),

            new Vector4f(1, 0, 0.5f, 0.3f),
            new Vector4f(0, 0.5f, 0, 0.3f),
            new Vector4f(0, 0, 0.5f, 0.3f),
            new Vector4f(1, 0, 0.5f, 0.3f),
            new Vector4f(0, 1, 0.5f, 0.3f),
            new Vector4f( 1, 0.5f, 0,0.3f),
            new Vector4f( 1, 0.5f, 1,0.3f),

            new Vector4f(0.5f, 0, 1, 0.3f),
            new Vector4f(0.5f, 0, 1, 0.3f),
            new Vector4f(0, 0.5f, 1, 0.3f),
            new Vector4f( 0.5f, 1, 0,0.3f),
            new Vector4f( 0.5f, 1, 1,0.3f),
            new Vector4f( 1, 1, 0.5f, 0.3f),
            new Vector4f( 1, 0.5f, 0.5f, 0.3f),
            new Vector4f( 0.5f, 0.5f, 1, 0.3f),
            new Vector4f( 0.5f, 1, 0.5f, 0.3f),
       };
    }

    public GVRSceneObject createQuadPlane(GVRContext gvrContext, float scale)
    {
        GVRSceneObject plane = new GVRSceneObject(gvrContext);
        GVRMesh mesh = GVRMesh.createQuad(gvrContext,
                "float3 a_position", 1.0f, 1.0f);
        GVRMaterial mat = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
        GVRSceneObject polygonObject = new GVRSceneObject(gvrContext, mesh, mat);
        Vector4f color = mColors[mPlaneIndex % mColors.length];

        plane.setName("Plane" + mPlaneIndex);
        polygonObject.setName("PlaneGeometry" + mPlaneIndex);
        mPlaneIndex++;
        mat.setDiffuseColor(color.x, color.y, color.x, color.w);
        polygonObject.getRenderData().disableLight();
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);
        polygonObject.getTransform().setScale(scale, scale, scale);
        plane.addChildObject(polygonObject);
        return plane;
    }

    public void initCursorController(GVRContext gvrContext, final SampleMain.TouchHandler handler) {
        final int cursorDepth = 100;
        gvrContext.getMainScene().getEventReceiver().addListener(handler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * cursorDepth,
                        0.2f * cursorDepth),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().disableLight();
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
                    oldController.removePickEventListener(handler);
                }
                mCursorController = newController;
                newController.setCursor(mCursor);
                newController.setCursorDepth(-cursorDepth);
                newController.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
                newController.getPicker().setEventOptions(eventOptions);
                newController.addPickEventListener(handler);
            }
        });
    }

    GVRCursorController getCursorController() {
        return this.mCursorController;
    }
}
