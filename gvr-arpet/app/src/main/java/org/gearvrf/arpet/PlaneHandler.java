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
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IMixedReality;
import org.gearvrf.mixedreality.IMixedReality;
import org.gearvrf.mixedreality.IPlaneEvents;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.utility.Log;
import org.greenrobot.eventbus.EventBus;
import org.joml.Vector3f;

import java.util.LinkedList;

public final class PlaneHandler implements IPlaneEvents, GVRDrawFrameListener {

    private GVRContext mContext;
    private GVRScene mScene;
    private PetMain mPetMain;
    private int hsvHUE = 0;

    private boolean planeDetected = false;
    private GVRSceneObject selectedPlaneObject = null;
    private PlaneBoard physicsPlane = null;
    public final static String PLANE_NAME = "Plane";
    public final static String PLANE_PHYSICS = "Plane Physics";
    public final static String PLANE_COLLIDER = "Plane Collider";

    // FIXME: move this to a utils or helper class
    private static long newComponentType(Class<? extends GVRComponent> clazz) {
        long hash = (long) clazz.hashCode() << 32;
        long t = System.currentTimeMillis() & 0xfffffff;
        return t | hash;
    }

    private static long PLANEBOARD_COMP_TYPE = newComponentType(PlaneBoard.class);

    // This will create an invisible board in which the static body will be attached. This board
    // will "follow" the A.R. plane that owns it so that it will work as if this plane has physics
    // attached to it.
    private final class PlaneBoard extends GVRComponent {
        private GVRSceneObject physicsObject;

        PlaneBoard(GVRContext gvrContext) {
            super(gvrContext, 0);

            mType = PLANEBOARD_COMP_TYPE;

            physicsObject = new GVRSceneObject(gvrContext);

            GVRBoxCollider collider = new GVRBoxCollider(gvrContext);
            collider.setHalfExtents(0.5f, 0.5f, 0.5f);
            physicsObject.attachComponent(collider);
            // To touch debug
            physicsObject.setName(PLANE_PHYSICS);

            // Uncomment if you want a green box to appear at the center of the invisible board.
            // Notice this green box is smaller than the board
            final boolean debugPhysics = false;
            if (debugPhysics) {
                GVRMaterial green = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
                green.setDiffuseColor(0f, 1f, 0f, 1f);
                GVRSceneObject mark = new GVRCubeSceneObject(gvrContext, true);
                mark.getRenderData().setMaterial(green);
                mark.getRenderData().setAlphaBlend(true);
                physicsObject.addChildObject(mark);
            }
        }

        @Override
        public void onAttach(GVRSceneObject newOwner) {
            super.onAttach(newOwner);
            mScene.addSceneObject(physicsObject);
        }

        @Override
        public void onDetach(GVRSceneObject oldOwner) {
            super.onDetach(oldOwner);
            mScene.removeSceneObject(physicsObject);
        }

        private void setBoxTransform() {
            float[] targetMtx = owner.getTransform().getModelMatrix();
            physicsObject.getTransform().setModelMatrix(targetMtx);
            physicsObject.getTransform().setScaleZ(1f);
        }

        void update() {
            if (!isEnabled()) {
                return;
            }

            setBoxTransform();

            GVRRigidBody board = (GVRRigidBody) physicsObject.getComponent(GVRRigidBody.getComponentType());
            if (board == null) {
                board = new GVRRigidBody(mContext, 0f);
                board.setRestitution(0.5f);
                board.setFriction(1.0f);
                board.setCcdMotionThreshold(0.001f);
                board.setCcdSweptSphereRadius(2f);
                physicsObject.attachComponent(board);
            }

            // This will update rigid body according to owner's transform
            board.reset(false);
        }
    }

    private LinkedList<GVRPlane> mPlanes = new LinkedList<>();

    private IMixedReality mixedReality;

    PlaneHandler(PetMain petMain) {
        mContext = petMain.getGVRContext();
        mScene = mContext.getMainScene();
        mPetMain = petMain;
    }

    private GVRSceneObject createQuadPlane() {
        GVRMesh mesh = GVRMesh.createQuad(mContext, "float3 a_position", 1, 1);
        GVRMaterial mat = new GVRMaterial(mContext, GVRMaterial.GVRShaderType.Phong.ID);
        GVRSceneObject polygonObject = new GVRSceneObject(mContext, mesh, mat);
        polygonObject.setName(PLANE_COLLIDER);

        hsvHUE += 35;
        float[] hsv = new float[3];
        hsv[0] = hsvHUE % 360;
        hsv[1] = 1f;
        hsv[2] = 1f;

        int c = Color.HSVToColor(50, hsv);
        mat.setDiffuseColor(Color.red(c) / 255f, Color.green(c) / 255f,
                Color.blue(c) / 255f, 0.5f);
        polygonObject.getRenderData().setMaterial(mat);
        polygonObject.getRenderData().disableLight();
        polygonObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);
        GVRSceneObject transformNode = new GVRSceneObject(mContext);
        transformNode.attachCollider(new GVRBoxCollider(mContext));
        transformNode.addChildObject(polygonObject);
        return transformNode;
    }

    private boolean updatePlanes = true;

    /*
     * ARCore session guaranteed to be initialized here.
     */
    @Override
    public void onStartPlaneDetection(IMixedReality mr)
    {
        mixedReality = mr;
        mPetMain.onARInit(mContext, mr);
    }

    @Override
    public void onStopPlaneDetection(IMixedReality mr) { }

    @Override
    public void onPlaneDetected(GVRPlane plane) {
        GVRPlane.Type planeType = plane.getPlaneType();

        // Don't use planes that are downward facing, e.g ceiling
        if (planeType == GVRPlane.Type.HORIZONTAL_DOWNWARD_FACING || selectedPlaneObject != null) {
            return;
        }
        GVRSceneObject planeGeo = createQuadPlane();

        planeGeo.attachComponent(plane);
//        mScene.addSceneObject(planeGeo);
        plane.getGVRContext().getMainScene().addSceneObject(planeGeo);


        mPlanes.add(plane);

        if (!planeDetected && planeType == GVRPlane.Type.HORIZONTAL_UPWARD_FACING) {
            planeDetected = true;

            // Now physics starts working and then boards must be continuously updated

        }
    }

    @Override
    public void onPlaneStateChange(GVRPlane plane, GVRTrackingState trackingState) {
        if (trackingState != GVRTrackingState.TRACKING) {
            plane.setEnable(false);
        } else {
            plane.setEnable(true);
        }
    }

    @Override
    public void onPlaneMerging(GVRPlane childPlane, GVRPlane parentPlane) {
        // Will remove PlaneBoard from childPlane because this plane is not needed anymore now
        // that parentPlane "contains" childPlane
        childPlane.getOwnerObject().detachComponent(PLANEBOARD_COMP_TYPE);
        mPlanes.remove(childPlane);
    }

    public void setSelectedPlane(GVRPlane mainPlane) {
        for (GVRPlane plane: mPlanes) {
            if (plane != mainPlane) {
                plane.setEnable(mainPlane == null);
            }
        }

        if (selectedPlaneObject != null) {
            selectedPlaneObject.detachComponent(PLANEBOARD_COMP_TYPE);
        }

        if (mainPlane != null) {
            selectedPlaneObject = mainPlane.getOwnerObject();
            selectedPlaneObject.attachComponent(physicsPlane);

            selectedPlaneObject.setName(PLANE_NAME);
            EventBus.getDefault().post(new PlaneDetectedEvent(mainPlane));
            mContext.registerDrawFrameListener(this);
        } else {
            mContext.unregisterDrawFrameListener(this);
            selectedPlaneObject = null;
        }
    }

    @Override
    public void onDrawFrame(float t) {
        if (selectedPlaneObject != null) {
            ((PlaneBoard) selectedPlaneObject.getComponent(PLANEBOARD_COMP_TYPE)).update();
        }
    }
}
