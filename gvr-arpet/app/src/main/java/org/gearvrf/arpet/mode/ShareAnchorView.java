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

import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.ApiConstants;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class ShareAnchorView extends BasePetView implements IViewEvents, View.OnClickListener {
    private GVRSceneObject mShareAnchorObject;
    private Button mGuestButton, mHostButton, mStatusMode, mCancelButton, mTryButton;
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
        mProgressHandler = new ProgressHandler(mProgressBar);
        mBackButtonShareAnchor.setOnClickListener(this);
        mGuestButton.setOnClickListener(this);
        mHostButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mTryButton.setOnClickListener(this);
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
        mMessage.setText(R.string.connected_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPairing.setImageResource(R.drawable.icon_connect);
    }

    public void inviteAcceptedHost(int total) {
        mMessage.setText(getGVRContext().getContext().getResources().
                getQuantityString(R.plurals.connected_guests, total, total));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mPairing.setImageResource(R.drawable.icon_connect);
    }

    public void invitationView() {
        mMessage.setText(R.string.invitation);
        mProgressBar.setVisibility(View.GONE);
        mGuestButton.setVisibility(View.VISIBLE);
        mHostButton.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.GONE);
        mTryButton.setVisibility(View.GONE);
    }

    public void pairingView() {
        mPairing.setImageResource(R.drawable.icon_to_pair);
        mMessage.setText(R.string.looking_the_same_thing);
        mSpinner.setVisibility(View.VISIBLE);
        mCheckIcon.setVisibility(View.GONE);
        RotateAnimation anim = new RotateAnimation(0f, 350f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(2000);
        mSpinner.startAnimation(anim);
    }

    public void toPairView() {
        mPairing.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        message_stayInPosition.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackgroundResource(R.drawable.circle_mask);
        mPoint.setVisibility(View.VISIBLE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.GONE);
    }

    public void paredView() {
        mPaired.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackgroundResource(R.drawable.bg_overlay);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mMessage.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.paired);
        message_stayInPosition.setVisibility(View.GONE);
        mPoint.setVisibility(View.GONE);
    }

    public void modeView() {
        mStatusMode.setVisibility(View.VISIBLE);
        mOverlayLayout.setBackground(null);
        mBackButtonShareAnchor.setVisibility(View.GONE);
        mPaired.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
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

    public void pairingError(){
        mPairing.setImageResource(R.drawable.icon_error_to_pair);
        mMessage.setText("Pairing error!\n Would you like to try again?");
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.VISIBLE);
        mTryButton.setVisibility(View.VISIBLE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
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
