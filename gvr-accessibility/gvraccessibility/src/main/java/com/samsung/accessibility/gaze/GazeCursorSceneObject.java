/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.gaze;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRSceneObject;

import com.samsung.accessibility.R;

public class GazeCursorSceneObject extends GVRSceneObject {

    private static final float NEAR_CLIPPING_OFFSET = 0.00001f;
    private static final float NORMAL_CURSOR_SIZE = 0.0028f;
    private static final int CURSOR_RENDER_ORDER = 100000;

    private GVRSceneObject rightCursor;
    private GVRSceneObject leftCursor;
    private static GazeCursorSceneObject sInstance;

    public static GazeCursorSceneObject getInstance(GVRContext gvrContext) {
        if (sInstance == null) {
            sInstance = new GazeCursorSceneObject(gvrContext);
        }
        return sInstance;
    }

    private GazeCursorSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        float xRightCursor = gvrContext.getMainScene().getMainCameraRig().getRightCamera().getTransform().getPositionX();
        float xLeftCursor = gvrContext.getMainScene().getMainCameraRig().getLeftCamera().getTransform().getPositionX();
        float zRightCursor = -(((GVRPerspectiveCamera) gvrContext.getMainScene().getMainCameraRig().getRightCamera()).getNearClippingDistance() + NEAR_CLIPPING_OFFSET);
        float zLeftCursor = -(((GVRPerspectiveCamera) gvrContext.getMainScene().getMainCameraRig().getLeftCamera()).getNearClippingDistance() + NEAR_CLIPPING_OFFSET);

        rightCursor = new GVRSceneObject(gvrContext);
        rightCursor.attachRenderData(createRenderData(gvrContext));
        rightCursor.getRenderData().setRenderMask(GVRRenderMaskBit.Right);
        rightCursor.getTransform().setPosition(xRightCursor, 0, zRightCursor);
        addChildObject(rightCursor);

        leftCursor = new GVRSceneObject(gvrContext);
        leftCursor.attachRenderData(createRenderData(gvrContext));
        leftCursor.getRenderData().setRenderMask(GVRRenderMaskBit.Left);
        leftCursor.getTransform().setPosition(xLeftCursor, 0, zLeftCursor);
        addChildObject(leftCursor);
    }

    private GVRRenderData createRenderData(GVRContext gvrContext) {
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRMesh mesh = gvrContext.createQuad(NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE);
        material.setMainTexture(gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.head_tracker)));
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        renderData.setDepthTest(false);
        renderData.setRenderingOrder(CURSOR_RENDER_ORDER);

        return renderData;
    }

}
