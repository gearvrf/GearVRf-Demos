/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet;

import android.graphics.Color;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IMRCommon;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.gearvrf.physics.GVRRigidBody;
import org.greenrobot.eventbus.EventBus;
import org.joml.Matrix4f;

import java.util.LinkedList;

public final class PlaneHandler implements IPlaneEventsListener, GVRDrawFrameListener {

    private GVRContext mContext;
    private GVRScene mScene;

    private int hsvHUE = 0;

    private GVRPlane firstPlane = null;
    public final static String PLANE_NAME = "Plane";

    // FIXME: move this to a utils or helper class
    private static long newComponentType(Class<? extends GVRComponent> clazz) {
        long hash = (long)clazz.hashCode() << 32;
        long t = System.currentTimeMillis() & 0xfffffff;
        return t | hash;
    }

    private static long PLANEBOARD_COMP_TYPE = newComponentType(PlaneBoard.class);

    // This will create an invisible board in which the static body will be attached. This board
    // will "follow" the A.R. plane that owns it so that it will work as if this plane has physics
    // attached to it.
    private final class PlaneBoard extends GVRComponent {
        private GVRPlane plane;
        private GVRSceneObject box;

        PlaneBoard(GVRContext gvrContext) {
            super(gvrContext, 0);

            mType = PLANEBOARD_COMP_TYPE;

            box = new GVRSceneObject(gvrContext);

            GVRBoxCollider collider = new GVRBoxCollider(gvrContext);
            collider.setHalfExtents(0.5f, 0.5f, 0.5f);
            box.attachComponent(collider);
            // To touch debug
            box.setName("Plane collider");

            // Uncomment if you want a green box to appear at the center of the invisible board.
            // Notice this green box is smaller than the board
//            GVRMaterial green = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
//            green.setDiffuseColor(0f, 1f, 0f, 1f);
//            GVRSceneObject mark = new GVRCubeSceneObject(gvrContext, true);
//            mark.getRenderData().setMaterial(green);
//            mark.getRenderData().setAlphaBlend(true);
//            mark.getTransform().setScale(0.3f, 0.3f, 1.1f);
//            box.addChildObject(mark);
        }

        @Override
        public void onAttach(GVRSceneObject newOwner) {
            if (!GVRPlane.class.isInstance(newOwner)) {
                throw new RuntimeException("PlaneBoard can only be attached to a GVRPlane object");
            }

            super.onAttach(newOwner);
            plane = (GVRPlane)newOwner;
            physicsRoot.addChildObject(box);
        }

        @Override
        public void onDetach(GVRSceneObject oldOwner) {
            super.onDetach(oldOwner);

            plane = null;
            physicsRoot.removeChildObject(box);
        }

        private void setBoxTransform() {
            Matrix4f targetMtx = plane.getSceneObject().getTransform().getModelMatrix4f();
            rootInvMat.mul(targetMtx, targetMtx);

            box.getTransform().setModelMatrix(targetMtx);
            box.getTransform().setScaleZ(1f);
        }

        void update() {
            if (!isEnabled()) {
                return;
            }

            setBoxTransform();

            GVRRigidBody board = (GVRRigidBody)box.getComponent(GVRRigidBody.getComponentType());
            if (board == null) {
                board = new GVRRigidBody(mContext, 0f);
                board.setRestitution(0.5f);
                board.setFriction(1.0f);
                box.attachComponent(board);
            }

            // This will update rigid body according to owner's transform
            board.disable();
            board.enable();
        }
    }

    private LinkedList<GVRPlane> mPlanes = new LinkedList<>();

    private IMRCommon mixedReality;

    // All objects that have physics attached to it will be children of this object
    private GVRSceneObject physicsRoot;

    PlaneHandler(PetContext petContext) {
        mContext = petContext.getGVRContext();
        mScene = petContext.getMainScene();
        mixedReality = petContext.getMixedReality();
        petContext.registerPlaneListener(this);

        physicsRoot = new GVRSceneObject(mContext);
        physicsRoot.getTransform().setScale(0.01f, 0.01f, 0.01f);

        // Uncomment if you want a black box to visually mark where is the physics root
//        GVRMaterial black = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
//        black.setDiffuseColor(0f, 0f, 0f, 1f);
//        GVRSceneObject cube = new GVRCubeSceneObject(gvrContext, true);
//        cube.getRenderData().setMaterial(black);
//        cube.getRenderData().setAlphaBlend(true);
//        cube.getTransform().setScale(5f, 4f, 8f);
//        physicsRoot.addChildObject(cube);
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
        GVRPlane.Type planeType = plane.getPlaneType();

        // Don't use planes that are downward facing, e.g ceiling
        if (planeType == GVRPlane.Type.HORIZONTAL_DOWNWARD_FACING) {
            return;
        }

        plane.setSceneObject(createQuadPlane());
        mScene.addSceneObject(plane);

        if (firstPlane == null && planeType == GVRPlane.Type.HORIZONTAL_UPWARD_FACING) {
            firstPlane = plane;
            firstPlane.setName(PLANE_NAME);
            EventBus.getDefault().post(firstPlane);

            // Physics root will be anchored to A.R. world so that all physics simulation will
            // work as if it was running at the A.R. world
            GVRAnchor anchor = mixedReality.createAnchor(plane.getCenterPose());
            anchor.attachSceneObject(physicsRoot);
            mScene.addSceneObject(anchor);

            EventBus.getDefault().post(physicsRoot);

            // Now physics starts working and then boards must be continuously updated
            mContext.registerDrawFrameListener(this);
        }

        PlaneBoard board = new PlaneBoard(mContext);
        plane.attachComponent(board);
        mPlanes.add(plane);
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
        // Will remove PlaneBoard from childPlane because this plane is not needed anymore now
        // that parentPlane "contains" childPlane
        childPlane.detachComponent(PLANEBOARD_COMP_TYPE);

        mPlanes.remove(childPlane);
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
        for (GVRPlane plane : mPlanes) {
            ((PlaneBoard)plane.getComponent(PLANEBOARD_COMP_TYPE)).update();
        }
    }
}
