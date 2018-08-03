package org.gearvrf.arpet;

import android.graphics.Color;
import android.util.Log;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.gearvrf.physics.GVRRigidBody;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.greenrobot.eventbus.EventBus;
import org.joml.Vector3f;

import java.util.LinkedList;

public final class PlaneHandler implements IPlaneEventsListener, GVRDrawFrameListener {
    private final static float PLANE_GROWTH_LIMIT = 0.3f;

    private GVRContext mContext;
    private GVRScene mScene;
    private PetActivity.PetContext mPetContext;

    private int hsvHUE = 0;

    // FIXME: is this really necessary? This plane is stored at the head of PlaneBoard list anyway
    private GVRPlane firstPlane = null;

    // This will create an invisible board in which the static body will be attached. This board
    // will "follow" the referenced A.R. plane so that it will work as if this plane has physics
    // attached to it.
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

            physicsRoot.addChildObject(box);
        }

        private void setBoxTransform() {
            Matrix4f targetMtx = plane.getSceneObject().getTransform().getModelMatrix4f();
            rootInvMat.mul(targetMtx, targetMtx);

            // This should work, but it seems to have a problem at setModelMatrix method...
            //box.getTransform().setModelMatrix(targetMtx);

            // ... That's why I'm using the solution below
            Vector3f scale = new Vector3f();
            Vector3f pos = new Vector3f();
            Quaternionf rot = new Quaternionf();
            targetMtx.getScale(scale);
            targetMtx.getTranslation(pos);
            targetMtx.normalize3x3();
            rot.setFromNormalized(targetMtx);
            box.getTransform().setScale(scale.x, scale.y, 1f);
            box.getTransform().setPosition(pos.x, pos.y, pos.z);
            box.getTransform().setRotation(rot.w, rot.x, rot.y, rot.z);
        }

        void update() {
            // TODO: check if this should also be done only when plane grows too much
            setBoxTransform();

            float dX = Math.abs(oldHeight - plane.getHeight());
            float dY = Math.abs(oldWidth - plane.getWidth());
            if (dX > PLANE_GROWTH_LIMIT || dY > PLANE_GROWTH_LIMIT) {
                // A.R. plane had grown too much, static body must be replaced

                box.detachComponent(GVRRigidBody.getComponentType());
                oldHeight = plane.getHeight();
                oldWidth = plane.getWidth();
                GVRRigidBody board = new GVRRigidBody(mContext, 0f);
                box.attachComponent(board);
            }
        }
    }

    private LinkedList<PlaneBoard> mBoards = new LinkedList<>();

    private GVRMixedReality mixedReality;

    // All objects that have physics attached to it will be children of this object
    private GVRSceneObject physicsRoot;

    PlaneHandler(GVRContext gvrContext, PetActivity.PetContext petContext, GVRMixedReality mixedReality) {
        mContext = gvrContext;
        mScene = mContext.getMainScene();
        mPetContext = petContext;

        this.mixedReality = mixedReality;

        physicsRoot = new GVRSceneObject(mContext);
        physicsRoot.getTransform().setScale(0.01f, 0.01f, 0.01f);
    }

    private GVRSceneObject createQuadPlane() {
        GVRMesh mesh = GVRMesh.createQuad(mContext, "float3 a_position", 1.0f, 1.0f);

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

    private boolean updatePlanes = true;

    @Override
    public void onPlaneDetection(GVRPlane plane) {
        plane.setSceneObject(createQuadPlane());
        mScene.addSceneObject(plane);

        if (firstPlane == null) {
            firstPlane = plane;
            EventBus.getDefault().post(firstPlane);

            // Physics root will be anchored to A.R. world so that all physics simulation will
            // work as if it was running at the A.R. world
            GVRAnchor anchor = mixedReality.createAnchor(plane.getCenterPose(), physicsRoot);
            mScene.addSceneObject(anchor);

            EventBus.getDefault().post(physicsRoot);

            // Now physics starts working and then boards must be continuously updated
            mContext.registerDrawFrameListener(this);
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


    private Matrix4f rootInvMat = new Matrix4f();

    @Override
    public void onDrawFrame(float t) {
        updatePlanes = !updatePlanes;

        // Updates on the boards must be synchronized with A.R. updates but can be postponed to
        // the next cycle
        if (!updatePlanes) return;

        rootInvMat.set(physicsRoot.getTransform().getModelMatrix());
        rootInvMat.invert();
        for (PlaneBoard b : mBoards) {
            b.update();
        }
    }
}
