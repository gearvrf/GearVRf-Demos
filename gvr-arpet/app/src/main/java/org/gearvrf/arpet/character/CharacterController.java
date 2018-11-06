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

import android.util.SparseArray;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.BallThrowHandler;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.mode.ILoadEvents;
import org.gearvrf.arpet.movement.IPetAction;
import org.gearvrf.arpet.movement.OnPetActionListener;
import org.gearvrf.arpet.movement.PetActionType;
import org.gearvrf.arpet.movement.PetActions;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.data.PetActionCommand;
import org.gearvrf.arpet.service.event.PetActionCommandReceivedMessage;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.utility.Log;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CharacterController extends BasePetMode {

    private IPetAction mCurrentAction = null; // default action IDLE
    private final SparseArray<IPetAction> mPetActions;
    private GVRDrawFrameListener mDrawFrameHandler;
    private BallThrowHandler mBallThrowHandler;

    private SharedMixedReality mMixedReality;
    private IMessageService mMessageService;
    private boolean mIsPlaying = false;

    public CharacterController(PetContext petContext) {
        super(petContext, new CharacterView(petContext));

        mPetActions = new SparseArray<>();
        mDrawFrameHandler = null;
        mMixedReality = (SharedMixedReality) mPetContext.getMixedReality();
        mBallThrowHandler = BallThrowHandler.getInstance(mPetContext);

        mMessageService = MessageService.getInstance();

        initPet((CharacterView) mModeScene);
    }

    @Subscribe
    public void handleReceivedMessage(PetActionCommandReceivedMessage message) {
        onSetCurrentAction(message.getPetActionCommand().getType());
    }

    @Override
    protected void onEnter() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onExit() {
        EventBus.getDefault().unregister(this);
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
        addAction(new PetActions.IDLE(mPetContext, pet));

        addAction(new PetActions.TO_BALL(pet, mBallThrowHandler.getBall(), action -> {
            setCurrentAction(PetActions.GRAB.ID);
        }));

        addAction(new PetActions.TO_PLAYER(pet, mPetContext.getPlayer(), action -> {
            setCurrentAction(PetActions.IDLE.ID);
        }));

        addAction(new PetActions.GRAB(pet, mBallThrowHandler.getBall(), new OnPetActionListener() {
            @Override
            public void onActionEnd(IPetAction action) {
                setCurrentAction(PetActions.TO_PLAYER.ID);
            }
        }));

        addAction(new PetActions.TO_TAP(pet, pet.getTapObject(), action -> setCurrentAction(PetActions.IDLE.ID)));

        addAction(new PetActions.AT_EDIT(mPetContext, pet));

        setCurrentAction(PetActions.IDLE.ID);
    }

    public void goToTap(float x, float y, float z) {
        if (mCurrentAction == null
                || mCurrentAction.id() == PetActions.IDLE.ID
                || mCurrentAction.id() == PetActions.TO_TAP.ID) {
            ((CharacterView) mModeScene).setTapPosition(x, y, z);
            setCurrentAction(PetActions.TO_TAP.ID);
        }
    }

    public void grabBall(GVRSceneObject ball) {
        GVRSceneObject pivot = ((CharacterView) mModeScene).getGrabPivot();

        if (pivot != null) {
            if (ball.getParent() != null) {
                ball.getParent().removeChildObject(ball);
            }
            // FIXME: The ball should be attached to pet's bone(pivot) to
            // have walking animation.

            GVRTransform t = ((CharacterView) mModeScene).getTransform();

            ball.getTransform().setRotation(1, 0, 0, 0);
            ball.getTransform().setPosition(0, 0.3f, 0.42f);
            ball.getTransform().setScale(0.36f, 0.36f, 0.36f);

            ((CharacterView) mModeScene).addChildObject(ball);

        }
    }

    public void playBall() {
        mIsPlaying = true;
        mBallThrowHandler.reset();
        mBallThrowHandler.enable();
    }

    public void stopBall() {
        mIsPlaying = false;
        mBallThrowHandler.disable();
    }

    public void setPlane(GVRSceneObject plane) {
        CharacterView petView = (CharacterView) view();

        petView.setBoundaryPlane(plane);
    }

    public GVRSceneObject getPlane() {
        CharacterView petView = (CharacterView) view();

        return petView.getBoundaryPlane();
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

        if (mIsPlaying || mPetContext.getMode() == PetConstants.SHARE_MODE_GUEST) {
            if (mCurrentAction.id() == PetActions.IDLE.ID) {
                mBallThrowHandler.reset();
                mBallThrowHandler.enable();
            } else if (mCurrentAction.id() == PetActions.TO_PLAYER.ID) {
                mBallThrowHandler.disable();
                grabBall(mBallThrowHandler.getBall());
            }
        }
    }

    private void onSendCurrentAction(@PetActionType int action) {
        if (mPetContext.getMode() == PetConstants.SHARE_MODE_HOST) {
            mMessageService.sendPetActionCommand(new PetActionCommand(action));
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
            }
        }
    }
}
