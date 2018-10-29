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

package org.gearvrf.arpet.mode;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.PetConstants;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.util.Locale;

public class ShareAnchorView extends BasePetView implements IViewEvents, View.OnClickListener {

    private GVRViewSceneObject mShareAnchorObject;
    private Button mGuestButton, mHostButton, mStatusMode, mCancelButton, mTryButton, mCancelBackShareAnchor;
    private Button mDisconnect, mTryPairingError, mCancel_waiting, mContinue;
    private TextView mMessage, message_stayInPosition, mQuantity_guest;
    private ProgressBar mProgressBar;
    private ImageView mCheckIcon, mPairing, mSpinner, mPaired, mPoint, mSpinner_waiting;
    private RelativeLayout mOverlayLayout;
    private LinearLayout mBackButtonShareAnchor;
    private ShareAnchorListener mShareAnchorListener;
    private ProgressHandler mProgressHandler;
    private @UserType
    int userType;

    @IntDef({UserType.GUEST, UserType.HOST})
    public @interface UserType {
        int GUEST = 0;
        int HOST = 1;
    }

    public ShareAnchorView(PetContext petContext) {
        super(petContext);
        mShareAnchorObject = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.share_anchor_layout, this);
        mShareAnchorObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);

        getTransform().setPosition(0.0f, 0.0f, -0.72f);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListenerShareAnchorMode(ShareAnchorListener listener) {
        mShareAnchorListener = listener;
    }

    public int getUserType() {
        return userType;
    }

    public ProgressHandler getProgressHandler() {
        return mProgressHandler;
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mGuestButton = view.findViewById(R.id.guest_button);
        mHostButton = view.findViewById(R.id.host_button);
        mMessage = view.findViewById(R.id.message);
        // mProgressBar = view.findViewById(R.id.progress);
        mCheckIcon = view.findViewById(R.id.check);
        mPaired = view.findViewById(R.id.ic_paired);
        mPairing = view.findViewById(R.id.image_action);
        mSpinner = view.findViewById(R.id.spinner);
        mSpinner_waiting = view.findViewById(R.id.spinner_waiting);
        mStatusMode = view.findViewById(R.id.status_sharing_anchor);
        mOverlayLayout = view.findViewById(R.id.overlay);
        mBackButtonShareAnchor = view.findViewById(R.id.button_back_sharing);
        mCancelButton = view.findViewById(R.id.cancel_button);
        mTryButton = view.findViewById(R.id.try_button);
        mPoint = view.findViewById(R.id.point);
        message_stayInPosition = view.findViewById(R.id.message_stayInPosition);
        mCancelBackShareAnchor = view.findViewById(R.id.cancel_button_back_share_anchor);
        mDisconnect = view.findViewById(R.id.disconnect_button);
        mTryPairingError = view.findViewById(R.id.try_button_pairing_error);
        mCancel_waiting = view.findViewById(R.id.cancel_waiting_button);
        mContinue = view.findViewById(R.id.continue_button);
        mQuantity_guest = view.findViewById(R.id.quantity_guest);
        mProgressHandler = new ProgressHandler(mProgressBar);
        mBackButtonShareAnchor.setOnClickListener(this);
        mGuestButton.setOnClickListener(this);
        mHostButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mTryButton.setOnClickListener(this);
        mStatusMode.setOnClickListener(this);
        mCancelBackShareAnchor.setOnClickListener(this);
        mDisconnect.setOnClickListener(this);
        mTryPairingError.setOnClickListener(this);
        mCancel_waiting.setOnClickListener(this);
        mContinue.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject shareAnchorView, View view) {
        shareAnchorView.setTextureBufferSize(PetConstants.TEXTURE_BUFFER_SIZE);
        addChildObject(shareAnchorView);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.guest_button) {
            userType = UserType.GUEST;
            mShareAnchorListener.onGuestButtonClicked();
        } else if (view.getId() == R.id.host_button) {
            userType = UserType.HOST;
            mShareAnchorListener.onHostButtonClicked();
        } else if (view.getId() == R.id.button_back_sharing) {
            mShareAnchorListener.OnBackShareAnchor();
        } else if (view.getId() == R.id.cancel_button) {
            mShareAnchorListener.OnCancel();
        } else if (view.getId() == R.id.try_button) {
            mShareAnchorListener.OnTry();
        } else if (view.getId() == R.id.status_sharing_anchor) {
            mShareAnchorListener.OnDisconnectScreen();
        } else if (view.getId() == R.id.cancel_button_back_share_anchor) {
            mShareAnchorListener.OnConnectedScreen();
        } else if (view.getId() == R.id.disconnect_button) {
            mShareAnchorListener.OnCancel();
        } else if (view.getId() == R.id.try_button_pairing_error) {
            mShareAnchorListener.OnTryPairingError();
        } else if (view.getId() == R.id.cancel_waiting_button) {
            mShareAnchorListener.OnCancelConnection();
        } else if (view.getId() == R.id.continue_button) {
            mShareAnchorListener.OnContinue();
        }
    }

    public void modeGuest() {
        mMessage.setText(R.string.waiting_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mPairing.setImageResource(R.drawable.ic_waiting_for_connection);
        mCancelButton.setVisibility(View.GONE);
        mBackButtonShareAnchor.setVisibility(View.GONE);
        mTryButton.setVisibility(View.GONE);
        RotateAnimation anim = new RotateAnimation(0f, 350f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(2000);
        mSpinner_waiting.startAnimation(anim);
        mSpinner_waiting.setVisibility(View.VISIBLE);
        mCancel_waiting.setVisibility(View.VISIBLE);
    }

    public void modeHost() {
        mMessage.setText(R.string.view_waiting_for_guests_status_text);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mPairing.setImageResource(R.drawable.ic_waiting_for_connection);
        mCancelButton.setVisibility(View.GONE);
        mTryButton.setVisibility(View.GONE);
        mBackButtonShareAnchor.setVisibility(View.GONE);
        mSpinner_waiting.setVisibility(View.VISIBLE);
        RotateAnimation anim = new RotateAnimation(0f, 350f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(2000);
        mSpinner_waiting.startAnimation(anim);
        mCancel_waiting.setVisibility(View.VISIBLE);
        mContinue.setVisibility(View.VISIBLE);
        mQuantity_guest.setVisibility(View.VISIBLE);
    }

    public void updateQuantityGuest(int quantity) {
        mPetContext.getActivity().runOnUiThread(() ->
                mQuantity_guest.setText(String.format(Locale.getDefault(), "%02d", quantity)));
    }

    public void inviteAcceptedGuest() {
        mMessage.setText(R.string.host_found);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.ic_connection_established);
        mCancel_waiting.setVisibility(View.GONE);
        mContinue.setVisibility(View.GONE);
        mSpinner_waiting.clearAnimation();
        mSpinner_waiting.setVisibility(View.GONE);
    }

    public void inviteAcceptedHost(int total) {
        mMessage.setText(getGVRContext().getContext().getResources().
                getQuantityString(R.plurals.guests_found, total, total));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.ic_connection_established);
        mCancel_waiting.setVisibility(View.GONE);
        mContinue.setVisibility(View.GONE);
        mSpinner_waiting.clearAnimation();
        mSpinner_waiting.setVisibility(View.GONE);
    }

    public void waitingView() {
        mPairing.setVisibility(View.GONE);
        mSpinner.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.waiting_for_host_sharing);
        mCheckIcon.setVisibility(View.GONE);
        RotateAnimation anim = new RotateAnimation(0f, 350f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(2000);
        mSpinner.startAnimation(anim);
    }

    public void toPairView() {
        mOverlayLayout.setBackgroundResource(R.drawable.circle_mask);
        message_stayInPosition.setVisibility(View.VISIBLE);
        message_stayInPosition.setText(R.string.stay_in_position);
        mPoint.setVisibility(View.VISIBLE);
        mPairing.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mTryPairingError.setVisibility(View.GONE);
    }

    public void modeView() {
        mStatusMode.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackground(null);
        mBackButtonShareAnchor.setVisibility(View.GONE);
        mPaired.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        message_stayInPosition.setVisibility(View.GONE);
        mCancelBackShareAnchor.setVisibility(View.GONE);
        mDisconnect.setVisibility(View.GONE);
        mPoint.setVisibility(View.GONE);
        mPairing.setVisibility(View.GONE);
    }

    public void notFound(String type) {
        mPairing.setImageResource(R.drawable.ic_no_connection_found);
        mMessage.setText(getGVRContext().getContext().getResources().getString(R.string.view_no_connection_found_status_text, type));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.VISIBLE);
        mTryButton.setVisibility(View.VISIBLE);
        mCancel_waiting.setVisibility(View.GONE);
        mContinue.setVisibility(View.GONE);
    }

    public void pairingError() {
        mPairing.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.ic_sharing_error);
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText("Pairing error!\n Would you like to try again?");
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.VISIBLE);
        mTryPairingError.setVisibility(View.VISIBLE);
        message_stayInPosition.setVisibility(View.GONE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        mPoint.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.GONE);
    }

    public void disconnectScreenHost() {
        mPairing.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.disconnect);
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.disconnect_host);
        mStatusMode.setVisibility(View.GONE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        mCancelBackShareAnchor.setVisibility(View.VISIBLE);
        mDisconnect.setVisibility(View.VISIBLE);
        mPoint.setVisibility(View.GONE);
    }

    public void disconnectScreenGuest() {
        mPairing.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.disconnect);
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.disconnect_guest);
        mStatusMode.setVisibility(View.GONE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        mCancelBackShareAnchor.setVisibility(View.VISIBLE);
        mDisconnect.setVisibility(View.VISIBLE);
    }

    public void centerPetView() {
        mPairing.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        message_stayInPosition.setVisibility(View.VISIBLE);
        message_stayInPosition.setText(R.string.center_pet);
        mOverlayLayout.setBackgroundResource(R.drawable.circle_mask);
        mPoint.setVisibility(View.VISIBLE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.GONE);
    }

    public void lookingSidebySide() {
        mPairing.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        message_stayInPosition.setVisibility(View.VISIBLE);
        message_stayInPosition.setText(R.string.looking_the_same_thing);
        mOverlayLayout.setBackgroundResource(R.drawable.circle_mask);
        mPoint.setVisibility(View.VISIBLE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.GONE);
    }

    public void moveAroundView() {
        message_stayInPosition.setText(R.string.move_around);
    }

    public void sharedHost() {
        mPairing.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.icon_connect);
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.common_text_shared);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        mPoint.setVisibility(View.GONE);
        message_stayInPosition.setVisibility(View.GONE);
    }

    public void mainView() {
        mStatusMode.setVisibility(View.GONE);
        mPairing.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.icon_start_sharing);
        mBackButtonShareAnchor.setVisibility(View.VISIBLE);
        mHostButton.setVisibility(View.VISIBLE);
        mGuestButton.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        message_stayInPosition.setVisibility(View.GONE);
        mPoint.setVisibility(View.GONE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.GONE);
        mStatusMode.setVisibility(View.VISIBLE);
        mSpinner_waiting.clearAnimation();
        mSpinner_waiting.setVisibility(View.GONE);
        mStatusMode.setVisibility(View.GONE);
        mQuantity_guest.setVisibility(View.GONE);
        mMessage.setVisibility(View.VISIBLE);
    }

    public void showNoInternetMessage() {
        Toast.makeText(mPetContext.getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
    }

    public void showBluetoothDisabledMessage() {
        Toast.makeText(mPetContext.getActivity(), R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
    }

    public void showDeviceNotVisibleMessage() {
        Toast.makeText(mPetContext.getActivity(), R.string.device_not_visible, Toast.LENGTH_LONG).show();
    }

    class ProgressHandler extends Handler {

        private static final int TICK_DELAY = 50; // millisecond
        private int currentProgress;
        private int duration = 0;

        public ProgressHandler(ProgressBar progressBar) {
            mProgressBar = progressBar;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (currentProgress < duration) {
                currentProgress += TICK_DELAY;
                mProgressBar.setProgress(currentProgress);
                sendEmptyMessageDelayed(0, TICK_DELAY);
            }
        }

        void start() {
            currentProgress = 0;
            mProgressBar.setMax(duration);
            sendEmptyMessage(0);
        }

        void stop() {
            removeMessages(0);
        }

        void setDuration(int duration) {
            this.duration = duration;
        }
    }
}
