
package com.samsung.accessibility;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class GVRGazeCursorSceneObject extends GVRSceneObject {

    private static final float NEAR_CLIPPING_OFFSET = 0.00001f;
    private static final float NORMAL_CURSOR_SIZE = 0.0028f;
    private static final float CURSOR_Z_POSITION = -.5f;
    private static final int CURSOR_RENDER_ORDER = 100000;

    private GVRSceneObject rightCursor;
    private GVRSceneObject leftCursor;

    public GVRGazeCursorSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        rightCursor = new GVRSceneObject(gvrContext);
        rightCursor.attachRenderData(createRenderData(gvrContext));
        rightCursor.getRenderData().setRenderMask(GVRRenderMaskBit.Right);
        rightCursor
                .getTransform()
                .setPosition(
                        gvrContext.getMainScene().getMainCameraRig().getRightCamera().getTransform().getPositionX(),
                        0,
                        -(((GVRPerspectiveCamera) gvrContext.getMainScene().getMainCameraRig().getRightCamera()).getNearClippingDistance() + NEAR_CLIPPING_OFFSET));
        addChildObject(rightCursor);

        leftCursor = new GVRSceneObject(gvrContext);
        leftCursor.attachRenderData(createRenderData(gvrContext));
        leftCursor.getRenderData().setRenderMask(GVRRenderMaskBit.Left);
        leftCursor
                .getTransform()
                .setPosition(
                        gvrContext.getMainScene().getMainCameraRig().getLeftCamera().getTransform().getPositionX(),
                        0,
                        -(((GVRPerspectiveCamera) gvrContext.getMainScene().getMainCameraRig().getLeftCamera()).getNearClippingDistance() + NEAR_CLIPPING_OFFSET));
        addChildObject(leftCursor);
    }

    public GVRGazeCursorSceneObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
        getTransform().setPositionZ(CURSOR_Z_POSITION);
        getRenderData().setDepthTest(false);
        getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);
    }

    public GVRGazeCursorSceneObject(GVRContext gvrContext, float width, float height, GVRTexture texture) {
        super(gvrContext, width, height, texture);
        getTransform().setPositionZ(CURSOR_Z_POSITION);
        getRenderData().setDepthTest(false);
        getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);
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
