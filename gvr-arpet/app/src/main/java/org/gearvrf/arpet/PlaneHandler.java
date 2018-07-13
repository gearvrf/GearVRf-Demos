package org.gearvrf.arpet;

import android.graphics.Color;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IPlaneEventsListener;

public final class PlaneHandler implements IPlaneEventsListener {

    private GVRContext mContext;
    private GVRScene mScene;

    private int hsvHUE = 0;

    PlaneHandler(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = mContext.getMainScene();
    }

    private GVRSceneObject createQuadPlane() {
        GVRMesh mesh = GVRMesh.createQuad(mContext,
                "float3 a_position", 1.0f, 1.0f);

        GVRMaterial mat = new GVRMaterial(mContext, GVRMaterial.GVRShaderType.Phong.ID);

        GVRSceneObject polygonObject = new GVRSceneObject(mContext, mesh, mat);

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

    @Override
    public void onPlaneDetection(GVRPlane plane) {
        plane.setSceneObject(createQuadPlane());
        mScene.addSceneObject(plane);
    }

    @Override
    public void onPlaneStateChange(GVRPlane plane, GVRTrackingState trackingState) {
        if (trackingState != GVRTrackingState.TRACKING) {
            plane.setEnable(false);
        }
        else {
            plane.setEnable(true);
        }
    }

    @Override
    public void onPlaneMerging(GVRPlane childPlane, GVRPlane parentPlane) {

    }

}
