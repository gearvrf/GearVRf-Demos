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

import org.gearvrf.GVRCollider;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageCallback;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.arpet.service.data.BallCommand;
import org.gearvrf.arpet.service.share.PlayerSceneObject;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.util.LoadModelHelper;
import org.gearvrf.io.GVRTouchPadGestureListener;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.utility.Log;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BallThrowHandler {

    private static final String TAG = BallThrowHandler.class.getSimpleName();

    private static final float defaultPositionX = 0f;
    private static final float defaultPositionY = 0f;
    private static final float defaultPositionZ = -40f;

    private static final float defaultScaleX = 40f;
    private static final float defaultScaleY = 40f;
    private static final float defaultScaleZ = 40f;

    private static final float MIN_Y_OFFSET = 3 * 100;

    private PlayerSceneObject mPlayer;
    private final PetContext mPetContext;
    private GVRSceneObject mBall;
    private GVRRigidBody mRigidBody;

    private GVREventListeners.ActivityEvents mEventListener;
    private boolean thrown = false;

    private GVRPlane firstPlane = null; // Maybe this could be replaced by a boolean
    private static BallThrowHandler sInstance;
    private boolean mResetOnTouchEnabled = true;

    private final float mDirTan;
    private float mForce;
    private final Vector3f mForceVector;

    private IMessageService mMessageService;

    private BallThrowHandler(PetContext petContext) {
        mPetContext = petContext;
        mPlayer = petContext.getPlayer();

        createBall();
        initController();

        // Throw the ball at 45 degrees
        mDirTan = (float) Math.tan(Math.PI / 4.0);
        mForce = 1f;
        mForceVector = new Vector3f(mDirTan, mDirTan, -1.0f);

        EventBus.getDefault().register(this);

        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new SimpleMessageReceiver(TAG) {
            @Override
            public void onReceiveBallCommand(BallCommand command) {
                if (BallCommand.THROW.equals(command.getType())) {
                    throwLocalBall(command.getForceVector());
                }
            }
        });
    }

    // FIXME: look for a different approach for this
    public static BallThrowHandler getInstance(PetContext petContext) {
        if (sInstance == null) {
            sInstance = new BallThrowHandler(petContext);
        }
        return sInstance;
    }

    public void enable() {

        final GVRSceneObject parent = mBall.getParent();
        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);

        if (parent != null) {
            parent.removeChildObject(mBall);
        }

        mPlayer.addChildObject(mBall);
        mPetContext.getGVRContext().getApplication().getEventReceiver().addListener(mEventListener);
    }

    public void disable() {
        final GVRSceneObject parent = mBall.getParent();
        thrown = false;
        resetRigidBody();
        if (parent != null) {
            parent.removeChildObject(mBall);
        }
        mPetContext.getGVRContext().getApplication().getEventReceiver().removeListener(mEventListener);
    }

    private void createBall() {
        load3DModel();

        createBoneCollider();

        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mBall.getTransform().setScale(defaultScaleX, defaultScaleY, defaultScaleZ);

        mRigidBody = new GVRRigidBody(mPetContext.getGVRContext(), 5.0f);
        mRigidBody.setRestitution(0.5f);
        mRigidBody.setFriction(0.5f);
        mRigidBody.setCcdMotionThreshold(0.001f);
        mRigidBody.setCcdSweptSphereRadius(5f);

        mBall.attachComponent(mRigidBody);
        mRigidBody.setEnable(false);
    }

    private void createBoneCollider() {
        mBall.forAllComponents(new GVRSceneObject.ComponentVisitor() {
            @Override
            public boolean visit(GVRComponent gvrComponent) {
                if (mBall.getCollider() == null) {
                    GVRCollider collider = new GVRMeshCollider(mPetContext.getGVRContext(),
                            ((GVRRenderData)gvrComponent).getMesh().getBoundingBox());
                    mBall.attachCollider(collider);
                }
                return false;
            }
        }, GVRRenderData.getComponentType());
    }

    @Subscribe
    public void onGVRWorldReady(PlaneDetectedEvent event) {
        this.firstPlane = event.getPlane();
    }

    private void initController() {
        final GVRTouchPadGestureListener gestureListener = new GVRTouchPadGestureListener() {
            @Override
            public boolean onDown(MotionEvent arg0) {
                if (firstPlane != null && mResetOnTouchEnabled && thrown) {
                    reset();
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                if (e1 == null || e2 == null) {
                    return false;
                }

                if (mPetContext.getMode() != SharedMixedReality.GUEST
                        && firstPlane != null) {
                    final float vlen = (float) Math.sqrt((vx * vx) + (vy * vy));
                    final float vz = vlen / mDirTan;

                    mForce = 50 * vlen / (float) (e2.getEventTime() - e1.getDownTime());
                    mForceVector.set(mForce * -vx, mForce * vy, mForce * -vz);

                    throwRemoteBall(mForceVector);

                    throwLocalBall(mForceVector);
                    return true;
                }

                return false;
            }
        };

        final GestureDetector gestureDetector = new GestureDetector(mPetContext.getActivity(), gestureListener);
        mEventListener = new GVREventListeners.ActivityEvents() {
            @Override
            public void dispatchTouchEvent(MotionEvent event) {
                gestureDetector.onTouchEvent(event);
            }
        };
    }

    private void throwRemoteBall(Vector3f forceVector) {
        BallCommand throwCommand = new BallCommand(BallCommand.THROW);
        throwCommand.setForceVector(forceVector);
        mMessageService.sendBallCommand(throwCommand, new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Success executing throw command on guests");
            }

            @Override
            public void onFailure(Exception error) {
                Log.d(TAG, "Failure executing throw command on guests: " + error.getMessage());
            }
        });
    }

    // FIXME: Why multiply by root matrix?
    private void throwLocalBall(Vector3f forceVector) {
        Matrix4f rootMatrix = mPetContext.getMainScene().getRoot().getTransform().getModelMatrix4f();
        rootMatrix.invert();

        // Calculating the new model matrix (T') for the ball: T' = iP x T
        Matrix4f ballMatrix = mBall.getTransform().getModelMatrix4f();
        rootMatrix.mul(ballMatrix, ballMatrix);

        // Add the ball as physics root child...
        mBall.getParent().removeChildObject(mBall);
        mPetContext.getMainScene().addSceneObject(mBall);

        // ... And set its model matrix to keep the same world matrix
        mBall.getTransform().setModelMatrix(ballMatrix);

        // Force vector will be based on player rotation...
        Matrix4f playerMatrix = mPlayer.getTransform().getModelMatrix4f();

        // ... And same transformation is required
        rootMatrix.mul(playerMatrix, playerMatrix);
        Quaternionf q = new Quaternionf();
        q.setFromNormalized(playerMatrix);
        forceVector.rotate(q);

        mRigidBody.setEnable(true);
        mRigidBody.applyCentralForce(forceVector.x(), forceVector.y(), forceVector.z());
        thrown = true;
        EventBus.getDefault().post(new BallThrowHandlerEvent(BallThrowHandlerEvent.THROWN));
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
        GVRSceneObject parent = mBall.getParent();
        if (parent != null) {
            parent.removeChildObject(mBall);
        }
        mBall.getTransform().setPosition(defaultPositionX, defaultPositionY, defaultPositionZ);
        mBall.getTransform().setScale(defaultScaleX, defaultScaleY, defaultScaleZ);
        mBall.getTransform().setRotation(1, 0, 0, 0);

        mPlayer.addChildObject(mBall);
        mResetOnTouchEnabled = true;
        thrown = false;
        EventBus.getDefault().post(new BallThrowHandlerEvent(BallThrowHandlerEvent.RESET));
    }

    public boolean canBeReseted() {
        return thrown && mPlayer.getTransform().getPositionY() - mBall.getTransform().getPositionY() > MIN_Y_OFFSET;
    }

    private void load3DModel() {
        mBall = LoadModelHelper.loadSceneObject(mPetContext.getGVRContext(),
                LoadModelHelper.BALL_MODEL_PATH);
    }
}
