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

package org.gearvrf.arpet.mode;

import android.annotation.SuppressLint;
import android.util.Log;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.character.CharacterController;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.arpet.manager.connection.event.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.util.EventBusUtils;
import org.greenrobot.eventbus.Subscribe;

import static org.gearvrf.arpet.manager.connection.IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST;

public class HudMode extends BasePetMode {
    private OnModeChange mModeChangeListener;
    private HudView mHudView;

    private PetConnectionManager mConnectionManager;
    private SharedMixedReality mSharedMixedReality;
    private CharacterController mPetController;

    public HudMode(PetContext petContext, CharacterController petController, OnModeChange listener) {
        super(petContext, new HudView(petContext));
        mModeChangeListener = listener;
        mPetController = petController;

        mHudView = (HudView) mModeScene;
        mHudView.setListener(new OnHudItemClickedHandler());
        mHudView.setDisconnectListener(new OnDisconnectClickedHandler());

        mConnectionManager = (PetConnectionManager) PetConnectionManager.getInstance();
        mSharedMixedReality = (SharedMixedReality) petContext.getMixedReality();
    }

    @Override
    protected void onEnter() {
        EventBusUtils.register(this);
        if (mPetContext.getMode() != PetConstants.SHARE_MODE_NONE) {
            Log.d(TAG, "Play Ball activated by sharing mode!");
            mModeChangeListener.onPlayBall();
        }
    }

    @Override
    protected void onExit() {
        EventBusUtils.unregister(this);
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {

    }

    private class OnHudItemClickedHandler implements OnHudItemClicked {

        @Override
        public void onBallClicked() {
            mModeChangeListener.onPlayBall();
            Log.d(TAG, "Play Ball Mode");
        }

        @Override
        public void onShareAnchorClicked() {
            mModeChangeListener.onShareAnchor();
            Log.d(TAG, "Share Anchor Mode");
        }

        @Override
        public void onEditModeClicked() {
            mModeChangeListener.onEditMode();
            Log.d(TAG, "Edit Mode");
        }

        @Override
        public void onCameraClicked() {
            mModeChangeListener.onScreenshot();
            Log.d(TAG, "Camera Mode");
        }

        @Override
        public void onConnectedClicked() {
            Log.d(TAG, "Connected label clicked");
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.showDisconnectView(mConnectionManager.getConnectionMode());
                mHudView.hideConnectedLabel();
            });
        }
    }

    private class OnDisconnectClickedHandler implements OnDisconnectClicked {
        @Override
        public void onCancel() {
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.showConnectedLabel();
            });
        }

        @Override
        public void onDisconnect() {
            petExit();
            mSharedMixedReality.stopSharing();
            mConnectionManager.disconnect();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
            });
            mPetController.stopBall();
        }
    }

    @SuppressLint("SwitchIntDef")
    @Subscribe
    public void handleConnectionEvent(PetConnectionEvent message) {
        if (message.getType() == EVENT_ALL_CONNECTIONS_LOST) {
            petExit();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
            });
        }
    }

    private void petExit() {
        if (mPetContext.getMode() == PetConstants.SHARE_MODE_GUEST) {
            //TODO: after finish the sharing anchor experience as guest, the scene will be reseted
            // and the user should be notified to detect planes and positioning the pet again
            mPetController.exit();
        }
    }
}
