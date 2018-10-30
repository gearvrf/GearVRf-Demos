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

package org.gearvrf.arpet.character;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.arpet.BallThrowHandler;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.mode.ILoadEvents;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.movement.OnPetActionListener;
import org.gearvrf.arpet.movement.PetActionType;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageCallback;
import org.gearvrf.arpet.service.MessageException;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.arpet.service.data.PetActionCommand;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.utility.Log;

public class CharacterController extends BasePetMode {

    private IPetAction mCurrentAction = null; // default action IDLE
    private final SparseArray<IPetAction> mPetActions;
    private GVRDrawFrameListener mDrawFrameHandler;
    private BallThrowHandler mBallThrowHandler;

    private SharedMixedReality mMixedReality;
    private IMessageService mMessageService;
    private GVRSceneObject mTapObject;

    private class LocalMessageReceiver extends SimpleMessageReceiver {

        public LocalMessageReceiver(@NonNull String name) {
            super(name);
        }

        @Override
        public void onReceivePetActionCommand(PetActionCommand command) throws MessageException {
            try {
                onSetCurrentAction(command.getType());
            } catch (Throwable t) {
                throw new MessageException("Error processing pet action: " + command, t);
            }
        }
    }

    public CharacterController(PetContext petContext) {
        super(petContext, new CharacterView(petContext));

        mPetActions = new SparseArray<>();
        mDrawFrameHandler = null;
        mMixedReality = (SharedMixedReality) mPetContext.getMixedReality();
        mBallThrowHandler = BallThrowHandler.getInstance(mPetContext);

        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new LocalMessageReceiver(TAG));

        initPet((CharacterView) mModeScene);
    }

    @Override
    protected void onEnter() {
    }

    @Override
    protected void onExit() {
    }

    @Override
    public void load(ILoadEvents listener) {
        super.load(listener);

        mModeScene.load(listener);
    }

    @Override
    public void unload() {
        super.unload();

        mModeScene.unload();
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

    private void initPet(CharacterView pet) {
        addAction(new PetActions.IDLE(pet, mPetContext.getPlayer()));

        addAction(new PetActions.TO_BALL(pet, mBallThrowHandler.getBall(), action -> {
            setCurrentAction(PetActions.GRAB.ID);
        }));

        addAction(new PetActions.TO_PLAYER(pet, mPetContext.getPlayer(), action -> {
            setCurrentAction(PetActions.IDLE.ID);
            // TODO: Improve this Ball handler api
            mBallThrowHandler.enable();
            mBallThrowHandler.reset();
        }));

        addAction( new PetActions.GRAB(pet, mBallThrowHandler.getBall(), new OnPetActionListener() {
            @Override
            public void onActionEnd(IPetAction action) {
                setCurrentAction(PetActions.TO_PLAYER.ID);
                mBallThrowHandler.disable();
                grabBall(mBallThrowHandler.getBall());
            }
        }));

        mTapObject = new GVRSceneObject(mPetContext.getGVRContext());
        addAction(new PetActions.TO_TAP(pet, mTapObject, action -> setCurrentAction(PetActions.IDLE.ID)));

        addAction(new PetActions.AT_EDIT(mPetContext, pet));

        addAction(new PetActions.AT_SHARE(mPetContext, pet));

        setCurrentAction(PetActions.IDLE.ID);
    }

    public void goToTap(float x, float y, float z) {
        if (mCurrentAction == null
                || mCurrentAction.id() == PetActions.IDLE.ID
                || mCurrentAction.id() == PetActions.TO_TAP.ID) {
            mTapObject.getTransform().setPosition(x, y, z);
            setCurrentAction(PetActions.TO_TAP.ID);
        }
    }

    public void grabBall(GVRSceneObject ball) {
        GVRSceneObject pivot = ((CharacterView)mModeScene).getGrabPivot();

        if (pivot != null) {
            if (ball.getParent() != null) {
                ball.getParent().removeChildObject(ball);
            }
            // FIXME: The ball should be attached to pet's bone(pivot) to
            // have walking animation.

            ball.getTransform().setRotation(1, 0, 0, 0);
            ball.getTransform().setPosition(0, 0.48f, 0.42f);
            ball.getTransform().setScale(0.36f, 0.36f, 0.36f);

            ((CharacterView) mModeScene).addChildObject(ball);

        }
    }

    public void playBall() {
        mBallThrowHandler.enable();
    }

    public void stopBall() {
        mBallThrowHandler.disable();
    }

    public void setPlane(GVRPlane plane) {
        CharacterView petView = (CharacterView) view();

        if (petView.getBoundaryPlane() != null) {
            petView.getBoundaryPlane().removeChildObject(mTapObject);
            mPetContext.unregisterSharedObject(petView.getBoundaryPlane().getSceneObject());
        }

        plane.getSceneObject().addChildObject(mTapObject);
        mPetContext.registerSharedObject(plane.getSceneObject(), ArPetObjectType.PLANE);

        petView.setBoundaryPlane(plane);
    }

    public GVRPlane getPlane() {
        return ((CharacterView) view()).getBoundaryPlane();
    }

    public void setAnchor(GVRAnchor anchor) {
        CharacterView petView = (CharacterView) view();
        petView.setAnchor(anchor);
    }

    public CharacterView getView() {
        return (CharacterView) view();
    }

    public void setCurrentAction(@PetActionType int action) {
        onSetCurrentAction(action);
        onSendCurrentAction(action);
    }

    private void onSetCurrentAction(@PetActionType int action) {
        mCurrentAction = mPetActions.get(action);
    }

    private void onSendCurrentAction(@PetActionType int action) {
        if (mPetContext.getMode() == SharedMixedReality.HOST) {
            PetActionCommand command = new PetActionCommand(action);
            mMessageService.sendPetActionCommand(command, new MessageCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Success sending current %d action to guest(s)", action);
                }

                @Override
                public void onFailure(Exception error) {
                    Log.w(TAG, "Failure sending current %d action to guest(s)", action);
                }
            });
        }
    }

    private void addAction(IPetAction action) {
        mPetActions.put(action.id(), action);
    }

    public void enableActions() {
        if (mDrawFrameHandler == null) {
            Log.w(TAG, "On actions enabled");
            mDrawFrameHandler = new DrawFrameHandler();
            mPetContext.getGVRContext().registerDrawFrameListener(mDrawFrameHandler);
        }
    }

    public void disableActions() {
        if (mDrawFrameHandler != null) {
            Log.w(TAG, "On actions disabled");
            mPetContext.getGVRContext().unregisterDrawFrameListener(mDrawFrameHandler);
            mDrawFrameHandler = null;
        }
    }

    public void setInitialScale() {
        CharacterView petView = (CharacterView) view();
        petView.setInitialScale();
    }

    private class DrawFrameHandler implements GVRDrawFrameListener {
        IPetAction activeAction = null;

        @Override
        public void onDrawFrame(float frameTime) {
            if (mCurrentAction != activeAction) {
                if (activeAction != null) {
                    activeAction.exit();
                }
                activeAction = mCurrentAction;
                activeAction.entry();
            } else if (activeAction != null) {
                activeAction.run(frameTime);
            }

            // FIXME: Move this to a proper place
            if (mBallThrowHandler.canBeReseted() && activeAction != null) {
                setCurrentAction(PetActions.IDLE.ID);
                mBallThrowHandler.reset();
            }
        }
    }
}
