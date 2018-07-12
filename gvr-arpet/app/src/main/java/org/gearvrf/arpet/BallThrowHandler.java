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

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRTouchPadGestureListener;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BallThrowHandler {
    private static final float defaultPositionX = 0f;
    private static final float defaultPositionY = 0f;
    private static final float defaultPositionZ = -2f;

    private static final float defaultScaleX = 0.25f;
    private static final float defaultScaleY = 0.25f;
    private static final float defaultScaleZ = 0.25f;

    private GVRContext mContext;
    private GVRScene mScene;
    private GVRSceneObject mBall;
    private GVRRigidBody mRigidBody;

    private GVREventListeners.ActivityEvents mEventListener;
    private boolean thrown = false;

    private float[] mForce = {0f, 50f, -50f};

    BallThrowHandler(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        createBall();
        initController();
    }

    public void setForceVector(float x, float y, float z) {
        mForce[0] = x;
        mForce[1] = y;
        mForce[2] = z;
    }

    public void enable() {
        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mScene.getMainCameraRig().addChildObject(mBall);
        mContext.getActivity().getEventReceiver().addListener(mEventListener);
    }

    public void disable() {
        thrown = false;
        resetRigidBody();
        mBall.getParent().removeChildObject(mBall);
        mContext.getActivity().getEventReceiver().removeListener(mEventListener);
    }

    private void createBall() {
        mBall = new GVRSphereSceneObject(mContext, true);

        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mBall.getTransform().setScale(defaultScaleX, defaultScaleY, defaultScaleZ);

        GVRMaterial red = new GVRMaterial(mContext, GVRMaterial.GVRShaderType.Phong.ID);
        red.setDiffuseColor(1f, 0f, 0f, 1f);
        mBall.getRenderData().setMaterial(red);
        mBall.getRenderData().setAlphaBlend(true);

        mBall.attachComponent(new GVRMeshCollider(mContext, false));

        mRigidBody = new GVRRigidBody(mContext, 1.0f);
        mBall.attachComponent(mRigidBody);
        mRigidBody.setEnable(false);
    }

    private void initController() {
        final GVRTouchPadGestureListener gestureListener = new GVRTouchPadGestureListener() {
            @Override
            public boolean onDown(MotionEvent arg0) {
                if (thrown) {
                    resetRigidBody();
                    mScene.removeSceneObject(mBall);
                    mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
                    mScene.getMainCameraRig().addChildObject(mBall);
                    thrown = false;
                } else {
                    Matrix4f ballMatrix = mBall.getTransform().getModelMatrix4f();
                    mBall.getParent().removeChildObject(mBall);

                    Matrix4f cameraMatrix = mScene.getMainCameraRig().getHeadTransform().getModelMatrix4f();
                    Quaternionf q = new Quaternionf();
                    q.setFromNormalized(cameraMatrix);
                    Vector3f force = new Vector3f(mForce[0], mForce[1], mForce[2]);
                    force.rotate(q);

                    mBall.getTransform().setModelMatrix(ballMatrix);
                    mScene.addSceneObject(mBall);
                    mRigidBody.setEnable(true);
                    mRigidBody.applyCentralForce(force.x(), force.y(), force.z());
                    thrown = true;
                }

                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return super.onSingleTapUp(e);
            }
        };

        final GestureDetector gestureDetector = new GestureDetector(mContext.getActivity(), gestureListener);
        mEventListener = new GVREventListeners.ActivityEvents() {
            @Override
            public void dispatchTouchEvent(MotionEvent event) {
                gestureDetector.onTouchEvent(event);
            }
        };
    }

    private void resetRigidBody() {
        mRigidBody.setLinearVelocity(0f, 0f, 0f);
        mRigidBody.setAngularVelocity(0f, 0f, 0f);
        mRigidBody.setEnable(false);
    }
}
