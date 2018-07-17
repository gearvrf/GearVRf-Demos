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

package org.gearvrf.arpet;

import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRTrackingState;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PetMain extends GVRMain {
    private static final String TAG = "GVR_ARPET";

    private GVRScene mScene;
    private GVRContext mContext;
    private PetActivity.PetContext mPetContext;
    private GVRMixedReality mMixedReality;

    private BallThrowHandler ballThrowHandler;
    private PlaneHandler planeHandler;

    private GVRSceneObject cube;
    private float cubeX = 0f;
    private float cubeZ = 0f;

    public PetMain(PetActivity.PetContext petContext) {
        mPetContext = petContext;
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        mMixedReality = new GVRMixedReality(gvrContext);
        mMixedReality.resume();

        GVRWorld world = new GVRWorld(gvrContext);
        world.setGravity(0f, -10f, 0f);
        mScene.getRoot().attachComponent(world);

        ballThrowHandler = new BallThrowHandler(gvrContext);
        ballThrowHandler.enable();

        planeHandler = new PlaneHandler(gvrContext);
        mMixedReality.registerPlaneListener(planeHandler);

//        cube = new GVRCubeSceneObject(gvrContext, true);
        cube = new GVRSceneObject(gvrContext);
//        GVRMaterial green = new GVRMaterial(mContext, GVRMaterial.GVRShaderType.Phong.ID);
//        green.setDiffuseColor(0f, 1f, 0f, 1f);
//        cube.getRenderData().setMaterial(green);
//        cube.getRenderData().setAlphaBlend(true);
        cube.getTransform().setPosition(0f, 0f, -10f);
        mScene.addSceneObject(cube);

        final Runnable planeSniffle = new Runnable() {
            @Override
            public void run() {
                if (planeHandler.firstPlane != null) {
                    Matrix4f mat = planeHandler.firstPlane.getSceneObject().getTransform().getModelMatrix4f();
                    Vector3f scale = new Vector3f();
                    mat.getScale(scale);
                    mat.normalize3x3();
                    Quaternionf q = new Quaternionf();
                    q.setFromNormalized(mat);

                    cube.getTransform().setPosition(mat.m30(), mat.m31(), mat.m32());
                    cube.getTransform().setRotation(q.w, q.x, q.y, q.z);

                    float dX = Math.abs(cubeX - planeHandler.firstPlane.getWidth());
                    float dZ = Math.abs(cubeZ - planeHandler.firstPlane.getHeight());
                    if (dX > 0.05f || dZ > 0.05f) {
                        cube.detachComponent(GVRRigidBody.getComponentType());
                        cube.detachComponent(GVRCollider.getComponentType());

                        cubeX = planeHandler.firstPlane.getWidth();
                        cubeZ = planeHandler.firstPlane.getHeight();

                        cube.getTransform().setScale(scale.x, scale.y, 1f);

                        GVRBoxCollider collider = new GVRBoxCollider(gvrContext);
                        collider.setHalfExtents(0.5f, 0.5f, 0.5f);
                        cube.attachComponent(collider);
                        GVRRigidBody board = new GVRRigidBody(gvrContext, 0f);
                        cube.attachComponent(board);
                    }
                }
                mPetContext.runDelayedOnPetThread(this, 30);
            }
        };

        mPetContext.runDelayedOnPetThread(planeSniffle, 30);
    }

    private IAnchorEventsListener mAnchorEventsListener = new IAnchorEventsListener() {
        @Override
        public void onAnchorStateChange(GVRAnchor gvrAnchor, GVRTrackingState gvrTrackingState) {

        }
    };

    public class TouchEvents implements ITouchEvents {
        @Override
        public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onExit(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onInside(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        }

        @Override
        public void onMotionOutside(GVRPicker picker, MotionEvent motionEvent) {

        }
    }

}
