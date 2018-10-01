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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
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
    private Button mGuestButton, mHostButton, mStatusMode;
    private TextView mMessage;
    private ProgressBar mProgressBar;
    private ImageView mCheckIcon, mPairing, mSpinner, mCheckBiggerIcon;
    private RelativeLayout mOverlayLayout;
    OnGuestOrHostListener mGuestOrHostListener;
    private ProgressHandler mProgressHandler;

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

    public void setListenerShareAnchorMode(OnGuestOrHostListener listener) {
        mGuestOrHostListener = listener;
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
        mCheckBiggerIcon = view.findViewById(R.id.check_bigger);
        mPairing = view.findViewById(R.id.image_action);
        mSpinner = view.findViewById(R.id.spinner);
        mStatusMode = view.findViewById(R.id.status_sharing_anchor);
        mOverlayLayout = view.findViewById(R.id.overlay);
        mProgressHandler = new ProgressHandler(mProgressBar);
        mGuestButton.setOnClickListener(this);
        mHostButton.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject shareAnchorView, View view) {
        shareAnchorView.setTextureBufferSize(ApiConstants.TEXTURE_BUFFER_SIZE);
        addChildObject(shareAnchorView);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.guest_button) {
            mGuestOrHostListener.OnGuest();
        } else if (view.getId() == R.id.host_button) {
            mGuestOrHostListener.OnHost();
        }
    }

    public void modeGuest() {
        mMessage.setText(R.string.waiting_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void modeHost() {
        mMessage.setText(R.string.waiting_guests);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void inviteAcceptedGuest() {
        mMessage.setText(R.string.connected_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void inviteAcceptedHost(int total) {
        mMessage.setText(getGVRContext().getContext().getResources().
                getQuantityString(R.plurals.connected_guests, total, total));
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckIcon.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void invitationView() {
        mMessage.setText(R.string.invitation);
        mProgressBar.setVisibility(View.GONE);
        mGuestButton.setVisibility(View.VISIBLE);
        mHostButton.setVisibility(View.VISIBLE);
    }

    public void pairingView() {
        mPairing.setImageResource(R.drawable.ic_pairing);
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
        mMessage.setText(R.string.stay_in_position);
    }

    public void paredView() {
        mPairing.setVisibility(View.GONE);
        mSpinner.clearAnimation();
        mSpinner.setVisibility(View.GONE);
        mCheckBiggerIcon.setVisibility(View.VISIBLE);
        mMessage.setText(R.string.paired);
    }

    public void modeView() {
        mStatusMode.setVisibility(View.VISIBLE);
        mCheckBiggerIcon.setVisibility(View.GONE);
        mMessage.setVisibility(View.GONE);
        mOverlayLayout.setBackground(null);
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

        void setDuration (int duration) {
            this.duration = duration;
        }
    }
}
