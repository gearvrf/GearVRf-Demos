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

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class ShareAnchorView extends BasePetView implements IViewEvents, View.OnClickListener {
    private GVRSceneObject mShareAnchorObject;
    private Button mGuestButton, mHostButton, mStatusMode, mCancelButton, mTryButton, mCancelBackShareAnchor, mDisconnect;
    private TextView mMessage, message_stayInPosition;
    private ProgressBar mProgressBar;
    private ImageView mCheckIcon, mPairing, mSpinner, mPaired, mPoint;
    private RelativeLayout mOverlayLayout;
    private LinearLayout mBackButtonShareAnchor;
    ShareAnchorListener mShareAnchorListener;
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
        mShareAnchorObject = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.share_anchor_layout, this);
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
        mProgressBar = view.findViewById(R.id.progress);
        mCheckIcon = view.findViewById(R.id.check);
        mPaired = view.findViewById(R.id.ic_paired);
        mPairing = view.findViewById(R.id.image_action);
        mSpinner = view.findViewById(R.id.spinner);
        mStatusMode = view.findViewById(R.id.status_sharing_anchor);
        mOverlayLayout = view.findViewById(R.id.overlay);
        mBackButtonShareAnchor = view.findViewById(R.id.button_back_sharing);
        mCancelButton = view.findViewById(R.id.cancel_button);
        mTryButton = view.findViewById(R.id.try_button);
        mPoint = view.findViewById(R.id.point);
        message_stayInPosition = view.findViewById(R.id.message_stayInPosition);
        mCancelBackShareAnchor = view.findViewById(R.id.cancel_button_back_share_anchor);
        mDisconnect = view.findViewById(R.id.disconnect_button);
        mProgressHandler = new ProgressHandler(mProgressBar);
        mBackButtonShareAnchor.setOnClickListener(this);
        mGuestButton.setOnClickListener(this);
        mHostButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mTryButton.setOnClickListener(this);
        mStatusMode.setOnClickListener(this);
        mCancelBackShareAnchor.setOnClickListener(this);
        mDisconnect.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject shareAnchorView, View view) {
        shareAnchorView.setTextureBufferSize(ApiConstants.TEXTURE_BUFFER_SIZE);
        addChildObject(shareAnchorView);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.guest_button) {
            userType = UserType.GUEST;
            mShareAnchorListener.OnGuest();
        } else if (view.getId() == R.id.host_button) {
            userType = UserType.HOST;
            mShareAnchorListener.OnHost();
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
        }
    }

    public void modeGuest() {
        mMessage.setText(R.string.waiting_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.icon_waiting_gest);
        mCancelButton.setVisibility(View.GONE);
        mTryButton.setVisibility(View.GONE);
        mBackButtonShareAnchor.setVisibility(View.GONE);
    }

    public void modeHost() {
        mMessage.setText(R.string.waiting_guests);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.icon_waiting_gest);
        mCancelButton.setVisibility(View.GONE);
        mTryButton.setVisibility(View.GONE);
        mBackButtonShareAnchor.setVisibility(View.GONE);
    }

    public void inviteAcceptedGuest() {
        mMessage.setText(R.string.host_found);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPairing.setImageResource(R.drawable.ic_host_found);
    }

    public void inviteAcceptedHost(int total) {
        mMessage.setText(getGVRContext().getContext().getResources().
                getQuantityString(R.plurals.found_guests, total, total));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPairing.setImageResource(R.drawable.ic_host_found);
    }

    public void waitingView() {
        mPairing.setVisibility(View.GONE);
        mSpinner.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.waiting);
        mCheckIcon.setVisibility(View.GONE);
        RotateAnimation anim = new RotateAnimation(0f, 350f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(2000);
        mSpinner.startAnimation(anim);
    }

    public void toPairView() {
        message_stayInPosition.setText(R.string.stay_in_position);
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
        mPairing.setImageResource(R.drawable.icon_error_start_sharing);
        mMessage.setText(getGVRContext().getContext().getResources().getString(R.string.not_found, type));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.VISIBLE);
        mTryButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void pairingError() {
        mPairing.setImageResource(R.drawable.icon_error_to_pair);
        mMessage.setText("Pairing error!\n Would you like to try again?");
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.VISIBLE);
        mTryButton.setVisibility(View.VISIBLE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
    }

    public void disconnectScreenHost() {
        mPairing.setVisibility(View.VISIBLE);
        mPairing.setImageResource(R.drawable.disconnet);
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
        mPairing.setImageResource(R.drawable.disconnet);
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
        mMessage.setText(R.string.shared);
        mCheckIcon.setVisibility(View.VISIBLE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        mPoint.setVisibility(View.GONE);
        message_stayInPosition.setVisibility(View.GONE);
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
