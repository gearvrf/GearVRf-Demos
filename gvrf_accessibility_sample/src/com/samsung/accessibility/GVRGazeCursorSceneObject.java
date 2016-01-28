
package com.samsung.accessibility;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class GVRGazeCursorSceneObject extends GVRSceneObject {

    private static final float NORMAL_CURSOR_SIZE = 0.014f;
    private static final float CURSOR_Z_POSITION = -.5f;

    private static final int CURSOR_RENDER_ORDER = 100000;

    public GVRGazeCursorSceneObject(GVRContext gvrContext) {
        super(gvrContext);

        createSimpleCursor(gvrContext, new GVRMaterial(gvrContext));
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

    private void createSimpleCursor(GVRContext gvrContext, GVRMaterial material) {
        GVRMesh mesh = gvrContext.createQuad(NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE);
        material.setMainTexture(gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.head_tracker)));
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        attachRenderData(renderData);
        renderData.setMesh(mesh);
        getTransform().setPositionZ(CURSOR_Z_POSITION);
        renderData.setDepthTest(false);
        renderData.setRenderingOrder(CURSOR_RENDER_ORDER);
    }

}
