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
import org.gearvrf.arpet.events.CollisionEvent;
import org.gearvrf.io.GVRTouchPadGestureListener;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.ICollisionEvents;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BallThrowHandler implements ICollisionEvents {
    private static final float defaultPositionX = 0f;
    private static final float defaultPositionY = 0f;
    private static final float defaultPositionZ = -20f;

    private static final float defaultScaleX = 2f;
    private static final float defaultScaleY = 2f;
    private static final float defaultScaleZ = 2f;

    private static final float MIN_Y_OFFSET = -100;

    private GVRContext mContext;
    private GVRScene mScene;
    private GVRSceneObject mBall;
    private GVRRigidBody mRigidBody;

    private GVREventListeners.ActivityEvents mEventListener;
    private boolean thrown = false;

    private float[] mForce = {0f, 50f, -50f};

    GVRSceneObject physicsRoot = null;

    BallThrowHandler(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        createBall();
        initController();

        EventBus.getDefault().register(this);
    }

    public void setForceVector(float x, float y, float z) {
        mForce[0] = x;
        mForce[1] = y;
        mForce[2] = z;
    }

    public void enable() {
        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mScene.getMainCameraRig().addChildObject(mBall);
        mContext.getApplication().getEventReceiver().addListener(mEventListener);
    }

    public void disable() {
        thrown = false;
        resetRigidBody();
        mBall.getParent().removeChildObject(mBall);
        mContext.getApplication().getEventReceiver().removeListener(mEventListener);
    }

    private void createBall() {
        mBall = new GVRSphereSceneObject(mContext, true);

        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mBall.getTransform().setScale(defaultScaleX, defaultScaleY, defaultScaleZ);

//        try {
//            GVRTexture tex = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, "TX_tennisball_DIF.png"));
//            GVRMaterial mat = new GVRMaterial(mContext);
//            mat.setMainTexture(tex);
//            mBall.getRenderData().setMaterial(mat);
//            mBall.getRenderData().setAlphaBlend(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        GVRMaterial red = new GVRMaterial(mContext, GVRMaterial.GVRShaderType.Phong.ID);
        red.setDiffuseColor(1f, 0f, 0f, 1f);
        mBall.getRenderData().setMaterial(red);
        mBall.getRenderData().setAlphaBlend(true);

        mBall.attachComponent(new GVRMeshCollider(mContext, false));

        mRigidBody = new GVRRigidBody(mContext, 1.0f);
        mBall.attachComponent(mRigidBody);
        mRigidBody.setEnable(false);
        mBall.getEventReceiver().addListener(this);
    }

    @Subscribe
    public void onGVRWorldReady(GVRSceneObject physicsRoot) {
        this.physicsRoot = physicsRoot;
    }

    private void initController() {
        final GVRTouchPadGestureListener gestureListener = new GVRTouchPadGestureListener() {
            @Override
            public boolean onDown(MotionEvent arg0) {
                if (physicsRoot == null) {
                    return true; // or false?
                }

                if (thrown) {
                    reset();
                } else {
                    Matrix4f rootMatrix = physicsRoot.getTransform().getModelMatrix4f();
                    rootMatrix.invert();

                    // Calculating the new model matrix (T') for the ball: T' = iP x T
                    Matrix4f ballMatrix = mBall.getTransform().getModelMatrix4f();
                    rootMatrix.mul(ballMatrix, ballMatrix);

                    // Add the ball as physics root child...
                    mBall.getParent().removeChildObject(mBall);
                    physicsRoot.addChildObject(mBall);

                    // ... And set its model matrix to keep the same world matrix
                    mBall.getTransform().setModelMatrix(ballMatrix);

                    Vector3f force = new Vector3f(mForce[0], mForce[1], mForce[2]);

                    // Force vector will be based on camera head rotation...
                    Matrix4f cameraMatrix = mScene.getMainCameraRig().getHeadTransform().getModelMatrix4f();

                    // ... And same transformation is required
                    rootMatrix.mul(cameraMatrix, cameraMatrix);
                    Quaternionf q = new Quaternionf();
                    q.setFromNormalized(cameraMatrix);
                    force.rotate(q);

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

    public GVRSceneObject getBall() {
        return mBall;
    }

    public void reset() {
        resetRigidBody();
        mBall.getParent().removeChildObject(mBall);
        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mBall.getTransform().setScale(defaultScaleX, defaultScaleY, defaultScaleZ);
        mScene.getMainCameraRig().addChildObject(mBall);
        thrown = false;
    }

    public boolean canBeReseted() {
        return mBall.getTransform().getPositionY() < MIN_Y_OFFSET;
    }

    @Override
    public void onEnter(GVRSceneObject gvrSceneObject, GVRSceneObject gvrSceneObject1, float[] floats, float v) {
        CollisionEvent event = new CollisionEvent(gvrSceneObject1, CollisionEvent.Type.ENTER);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onExit(GVRSceneObject gvrSceneObject, GVRSceneObject gvrSceneObject1, float[] floats, float v) {
        CollisionEvent event = new CollisionEvent(gvrSceneObject1, CollisionEvent.Type.EXIT);
        EventBus.getDefault().post(event);
    }

}
