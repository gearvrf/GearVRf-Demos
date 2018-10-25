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
import org.gearvrf.arpet.manager.connection.PetConnectionEvent;
import org.gearvrf.arpet.manager.connection.PetConnectionEventHandler;
import org.gearvrf.arpet.manager.connection.PetConnectionManager;
import org.gearvrf.arpet.service.share.SharedMixedReality;

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
        mConnectionManager.addEventHandler(new LocalConnectionEventHandler());
        mSharedMixedReality = (SharedMixedReality) petContext.getMixedReality();
    }

    @Override
    protected void onEnter() {
        if (mPetContext.getMode() != SharedMixedReality.OFF) {
            Log.d(TAG, "Play Ball activated by sharing mode!");
            mModeChangeListener.onPlayBall();
        }
    }

    @Override
    protected void onExit() {
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
            mSharedMixedReality.stopSharing();
            mConnectionManager.disconnect();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
            });
            mPetController.stopBall();
        }
    }

    private class LocalConnectionEventHandler implements PetConnectionEventHandler {

        @SuppressLint("SwitchIntDef")
        @Override
        public void handleEvent(PetConnectionEvent message) {
            mPetContext.getActivity().runOnUiThread(() -> {
                switch (message.getType()) {
                    case EVENT_ALL_CONNECTIONS_LOST:
                        onSharingOff();
                        break;
                    default:
                        break;
                }
            });
        }

        private void onSharingOff() {
            mSharedMixedReality.stopSharing();
            mPetController.stopBall();
            mPetContext.getActivity().runOnUiThread(() -> {
                mHudView.hideDisconnectView();
                mHudView.hideConnectedLabel();
            });
        }
    }
}
