package org.gearvrf.arpet.mode;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class ShareAnchorView extends BasePetView implements IViewEvents, View.OnClickListener {
    private GVRContext mContext;
    private GVRSceneObject mInvitationObject;
    private Button mGuestButton, mHostButton;
    private TextView mMessage;
    private ProgressBar mProgressBar;
    private ProgressHandler mProgressHandler;
    private OnShareAnchorModeListener mSendingInviteListener;
    private ImageView mCheckImage;

    public ShareAnchorView(GVRContext gvrContext) {
        super(gvrContext);
        mContext = gvrContext;
        mInvitationObject = new GVRViewSceneObject(mContext, R.layout.share_anchor_layout, this);
    }

    public void setSendingInviteListener(OnShareAnchorModeListener listener) {
        mSendingInviteListener = listener;
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mGuestButton = view.findViewById(R.id.guest_button);
        mHostButton = view.findViewById(R.id.host_button);
        mMessage = view.findViewById(R.id.message);
        mProgressBar = view.findViewById(R.id.progress);
        mCheckImage = view.findViewById(R.id.check);
        mGuestButton.setOnClickListener(this);
        mHostButton.setOnClickListener(this);
        mProgressHandler = new ProgressHandler(mProgressBar, 10000);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject sendingInvitationObject, View view) {
        sendingInvitationObject.getTransform().setScale(7.2f, 6.2f, 1.0f);
        sendingInvitationObject.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        addChildObject(mInvitationObject);
        mInvitationObject.getRenderData().getMaterial().setColor(0.6f, 0.6f, 0.6f);
    }

    public void OnSending() {
        mSendingInviteListener.OnSending();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.guest_button) {
            mGuestButton.post(new Runnable() {
                @Override
                public void run() {
                    modeGuest();
                }
            });
        } else if (view.getId() == R.id.host_button) {
            modeHost();
        }
    }

    private void modeGuest() {
        mMessage.setText(R.string.waiting_guests);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void modeHost() {
        mMessage.setText(R.string.waiting_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void inviteAcceptedGuest() {
        mMessage.setText(R.string.connected_guests);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckImage.setVisibility(View.VISIBLE);
    }

    private void inviteAcceptedHost() {
        mMessage.setText(R.string.connected_host);
        mGuestButton.setVisibility(View.GONE);
        mHostButton.setVisibility(View.GONE);
        mCheckImage.setVisibility(View.VISIBLE);
    }

    class ProgressHandler extends Handler {

        private static final int TICK_DELAY = 10000; // millisecond
        private int currentProgress;
        private int duration;

        public ProgressHandler(ProgressBar progressBar, int duration) {
            mProgressBar = progressBar;
            this.duration = duration;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (currentProgress < duration) {
                mProgressBar.setProgress(currentProgress);
                currentProgress += TICK_DELAY;
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
    }
}
