package org.gearvrf.arpet;

import android.graphics.Color;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.gearvrf.physics.GVRRigidBody;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;

public final class PlaneHandler implements IPlaneEventsListener, GVRDrawFrameListener {
    private final static float PLANE_GROWTH_LIMIT = 0.5f;

    private GVRContext mContext;
    private GVRScene mScene;
    private PetActivity.PetContext mPetContext;

    private int hsvHUE = 0;

    GVRPlane firstPlane = null;

    private final class PlaneBoard {
        private GVRPlane plane;
        private float oldHeight = 0f;
        private float oldWidth = 0f;
        private GVRSceneObject box;

        PlaneBoard(GVRPlane plane) {
            this.plane = plane;
            box = new GVRSceneObject(mContext);
            GVRBoxCollider collider = new GVRBoxCollider(mContext);
            collider.setHalfExtents(0.5f, 0.5f, 0.5f);
            box.attachComponent(collider);
            mScene.addSceneObject(box);
        }

        void update() {
            if (plane.getTrackingState() != GVRTrackingState.TRACKING) {
                return;
            }

            Matrix4f mat = plane.getSceneObject().getTransform().getModelMatrix4f();
            float scale_x = (float)Math.sqrt(mat.m00() * mat.m00() + mat.m01() * mat.m01() + mat.m02() * mat.m02());
            float scale_y = (float)Math.sqrt(mat.m10() * mat.m10() + mat.m11() * mat.m11() + mat.m12() * mat.m12());
            mat.normalize3x3();

            Quaternionf q = new Quaternionf();
            q.setFromNormalized(mat);

            box.getTransform().setPosition(mat.m30(), mat.m31(), mat.m32());
            box.getTransform().setRotation(q.w, q.x, q.y, q.z);

            float dX = Math.abs(oldHeight - plane.getHeight());
            float dY = Math.abs(oldWidth - plane.getWidth());
            if (dX > PLANE_GROWTH_LIMIT || dY > PLANE_GROWTH_LIMIT) {
                box.detachComponent(GVRRigidBody.getComponentType());
                oldHeight = plane.getHeight();
                oldWidth = plane.getWidth();
                box.getTransform().setScale(scale_x, scale_y, 1f);
                GVRRigidBody board = new GVRRigidBody(mContext, 0f);
                box.attachComponent(board);
            }
        }
    }

    private LinkedList<PlaneBoard> mBoards = new LinkedList<>();

    PlaneHandler(GVRContext gvrContext, PetActivity.PetContext petContext) {
        mContext = gvrContext;
        mScene = mContext.getMainScene();
        mPetContext = petContext;

        mContext.registerDrawFrameListener(this);
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

        if (firstPlane == null) {
            firstPlane = plane;
            EventBus.getDefault().post(firstPlane);
        }

        PlaneBoard b = new PlaneBoard(plane);
        mBoards.add(b);
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


    private Runnable planeUpdater = new Runnable() {
        @Override
        public void run() {
            for (PlaneBoard b : mBoards) {
                b.update();
            }
        }
    };

    @Override
    public void onDrawFrame(float t) {
        mPetContext.runOnPetThread(planeUpdater);
    }
}
