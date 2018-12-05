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

package org.gearvrf.arcore.aravatar;

import android.graphics.Color;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRGazeCursorController;
import org.gearvrf.io.GVRInputManager;
import org.joml.Vector4f;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public class SceneUtils
{
    private GVRSceneObject mCursor;
    private Vector4f[] mColors;
    private int mPlaneIndex = 0;

    SceneUtils()
    {
        mColors = new Vector4f[]
        {
            new Vector4f(1, 0, 0, 0.2f),
            new Vector4f(0, 1, 0, 0.2f),
            new Vector4f(0, 0, 1, 0.2f),
            new Vector4f(1, 0, 1, 0.2f),
            new Vector4f(0, 1, 1, 0.2f),
            new Vector4f(1, 1, 0, 0.2f),
            new Vector4f(1, 1, 1, 0.2f),

            new Vector4f(1, 0, 0.5f, 0.2f),
            new Vector4f(0, 0.5f, 0, 0.2f),
            new Vector4f(0, 0, 0.5f, 0.2f),
            new Vector4f(1, 0, 0.5f, 0.2f),
            new Vector4f(0, 1, 0.5f, 0.2f),
            new Vector4f( 1, 0.5f, 0,0.2f),
            new Vector4f( 1, 0.5f, 1,0.2f),

            new Vector4f(0.5f, 0, 1, 0.2f),
            new Vector4f(0.5f, 0, 1, 0.2f),
            new Vector4f(0, 0.5f, 1, 0.2f),
            new Vector4f( 0.5f, 1, 0,0.2f),
            new Vector4f( 0.5f, 1, 1,0.2f),
            new Vector4f( 1, 1, 0.5f, 0.2f),
            new Vector4f( 1, 0.5f, 0.5f, 0.2f),
            new Vector4f( 0.5f, 0.5f, 1, 0.2f),
            new Vector4f( 0.5f, 1, 0.5f, 0.2f),
       };
    }

    public GVRSceneObject createPlane(GVRContext gvrContext)
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
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getRenderData().disableLight();
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);
        plane.addChildObject(polygonObject);
        return plane;
    }

    public GVRDirectLight makeSceneLight(GVRContext ctx)
    {
        GVRSceneObject lightOwner = new GVRSceneObject(ctx);
        GVRDirectLight light = new GVRDirectLight(ctx);

        lightOwner.setName("SceneLight");
        light.setAmbientIntensity(0.2f, 0.2f, 0.2f, 1);
        light.setDiffuseIntensity(0.2f, 0.2f, 0.2f, 1);
        light.setSpecularIntensity(0.2f, 0.2f, 0.2f, 1);
        lightOwner.attachComponent(light);
        return light;
    }

    public void initCursorController(GVRContext gvrContext, final ITouchEvents handler, final float screenDepth)
    {
        final int cursorDepth = 10;
        GVRInputManager inputManager = gvrContext.getInputManager();
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS,
                GVRPicker.EventOptions.SEND_TO_HIT_OBJECT);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(handler);
                }
                newController.setCursor(mCursor);
                newController.addPickEventListener(handler);
                newController.setCursorDepth(cursorDepth);
                newController.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

}
