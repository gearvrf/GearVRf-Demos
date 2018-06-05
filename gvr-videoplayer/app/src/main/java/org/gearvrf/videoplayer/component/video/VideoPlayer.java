package org.gearvrf.videoplayer.component.video;

import android.os.Handler;
import android.os.Message;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.gearvrf.videoplayer.component.FadeableViewObject;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.FocusableViewSceneObject;

import java.io.File;

public class VideoPlayer extends GVRSceneObject {

    private static final String TAG = VideoPlayer.class.getSimpleName();
    private static final int CONTROLLER_AUTO_HIDE_DELAY = 3000;
    private static final float CONTROLLER_WIDGET_FACTOR = .70f;
    private static final float CONTROLLER_HEIGHT_FACTOR = .25f;
    private static final float BACK_BUTTON_HEIGHT_FACTOR = .1f;

    private VideoPlayerScreen mVideoComponent;
    private VideoPlayerControlWidget mVideoControllerComponent;
    private VideoPlayerBackButton mBackButtonComponent;
    private boolean mIsControllerActive = true;
    private boolean mAutoHideControllerEnabled;
    private WidgetAutoHideTimer mWidgetAutoHideTimer;
    private boolean mPlayerActive = true;

    public VideoPlayer(GVRContext gvrContext, float width, float height) {
        super(gvrContext);
        mWidgetAutoHideTimer = new WidgetAutoHideTimer(this);
        addVideoComponent(width, height);
        addVideoControllerComponent(CONTROLLER_WIDGET_FACTOR * width, CONTROLLER_HEIGHT_FACTOR * height);
        addBackButtonComponent(BACK_BUTTON_HEIGHT_FACTOR * height, BACK_BUTTON_HEIGHT_FACTOR * height);
    }

    public void prepare(File... files) {
        if (files != null && files.length > 0) {
            mVideoComponent.prepare(files);
        } else {
            mVideoComponent.prepareDefault(); // from assets folder
        }
    }

    public void hideController() {
        Log.d(TAG, "hideController: ");
        if (mPlayerActive && mIsControllerActive) {
            mIsControllerActive = false;
            mVideoControllerComponent.fadeOut(new FadeableViewObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    removeChildObject(mVideoControllerComponent);
                }
            });
            mBackButtonComponent.fadeOut(new FadeableViewObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    removeChildObject(mBackButtonComponent);
                }
            });
        }
    }

    public void showController() {
        Log.d(TAG, "showController: ");
        if (mPlayerActive) {
            showController(mAutoHideControllerEnabled);
        }
    }

    private void showController(boolean autoHide) {
        Log.d(TAG, "showController: ");
        if (!mIsControllerActive) {
            addChildObject(mVideoControllerComponent);
            addChildObject(mBackButtonComponent);
            mVideoControllerComponent.fadeIn(new FadeableViewObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mIsControllerActive = true;
                }
            });
            mBackButtonComponent.fadeIn();
        }
        if (autoHide) {
            mWidgetAutoHideTimer.start();
        }
    }

    public void setAutoHideControllerEnabled(boolean autoHide) {
        mAutoHideControllerEnabled = autoHide;
        if (autoHide) {
            mWidgetAutoHideTimer.start();
        }
    }

    private void addVideoComponent(float width, float height) {
        mVideoComponent = new VideoPlayerScreen(getGVRContext(), width, height);
        mVideoComponent.setOnVideoPlayerListener(mInternalVideoPlayerListener);
        addChildObject(mVideoComponent);
    }

    private void addVideoControllerComponent(float width, float height) {

        mVideoControllerComponent = new VideoPlayerControlWidget(getGVRContext(), width, height);
        mVideoControllerComponent.setOnVideoControllerListener(mOnVideoControllerListener);
        mVideoControllerComponent.setFocusListener(mFocusListener);

        // Put video control widget below the video screen
        float positionY = -(height / CONTROLLER_HEIGHT_FACTOR / 2f);
        mVideoControllerComponent.getTransform().setPositionY(positionY * 1.02f);

        mVideoControllerComponent.getTransform().setPositionZ(mVideoComponent.getTransform().getPositionZ() + .05f);

        addChildObject(mVideoControllerComponent);
    }

    private void addBackButtonComponent(float width, float height) {

        mBackButtonComponent = new VideoPlayerBackButton(getGVRContext(), width, height);
        mBackButtonComponent.setFocusListener(mFocusListener);

        // Put back button above the video screen
        float positionY = (height / BACK_BUTTON_HEIGHT_FACTOR / 2f);
        mBackButtonComponent.getTransform().setPositionY(positionY - (positionY * .08f));

        mBackButtonComponent.getTransform().setPositionZ(mVideoComponent.getTransform().getPositionZ() + .05f);

        addChildObject(mBackButtonComponent);
    }

    public void setVideoPlayerListener(OnVideoPlayerListener listener) {
        mInternalVideoPlayerListener.setOnVideoPlayerListener(listener);
    }

    private FocusListener<FocusableViewSceneObject> mFocusListener = new FocusListener<FocusableViewSceneObject>() {
        @Override
        public void onFocusGained(FocusableViewSceneObject focusable) {
            Log.d(TAG, "onFocusGained: ");
            if (focusable.getName().equals("videoControlWidget")
                    || focusable.getName().equals("videoBackButton")) {
                mWidgetAutoHideTimer.cancel();
            }
        }

        @Override
        public void onFocusLost(FocusableViewSceneObject focusable) {
            Log.d(TAG, "onFocusLost: ");
            if (focusable.getName().equals("videoControlWidget")
                    || focusable.getName().equals("videoBackButton")) {
                mWidgetAutoHideTimer.start();
            }
        }
    };

    private VideoPlayerListenerDispatcher mInternalVideoPlayerListener = new VideoPlayerListenerDispatcher() {
        @Override
        public void onProgress(long progress) {
            mVideoControllerComponent.setProgress((int) progress);
            super.onProgress(progress);
        }

        @Override
        public void onPrepare(String title, long duration) {
            Log.d(TAG, "Video prepared: {title: " + title + ", duration: " + duration + "}");
            mVideoControllerComponent.setPlayPauseButtonEnabled(true);
            mVideoControllerComponent.setTitle(title);
            mVideoControllerComponent.setMaxProgress((int) duration);
            mVideoControllerComponent.setProgress((int) mVideoComponent.getProgress());
            mVideoControllerComponent.showPlay();
            super.onPrepare(title, duration);
        }

        @Override
        public void onStart() {
            Log.d(TAG, "Video started");
            mVideoControllerComponent.setPlayPauseButtonEnabled(true);
            mVideoControllerComponent.showPause();
            showController();
            super.onStart();
        }

        @Override
        public void onLoading() {
            Log.d(TAG, "Video loading");
            mVideoControllerComponent.setPlayPauseButtonEnabled(false);
            super.onLoading();
        }

        @Override
        public void onEnd() {
            Log.d(TAG, "Video ended");
            showController(false);
            super.onEnd();
        }

        @Override
        public void onAllEnd() {
            Log.d(TAG, "All videos ended");
            super.onAllEnd();
        }
    };

    private OnVideoControllerListener mOnVideoControllerListener = new OnVideoControllerListener() {
        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay: ");
            mVideoComponent.playVideo();
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause: ");
            mVideoComponent.pauseVideo();
        }

        @Override
        public void onBack() {
            Log.d(TAG, "onBack: ");
        }

        @Override
        public void onSeek(long progress) {
            Log.d(TAG, "onSeek: ");
            mVideoComponent.setProgress(progress);
        }
    };

    private static class WidgetAutoHideTimer extends Handler {

        private VideoPlayer mVideoPlayer;

        WidgetAutoHideTimer(VideoPlayer videoPlayer) {
            mVideoPlayer = videoPlayer;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mVideoPlayer.hideController();
        }

        void start() {
            removeMessages(0);
            sendEmptyMessageDelayed(0, CONTROLLER_AUTO_HIDE_DELAY);
        }

        public void cancel() {
            removeMessages(0);
        }
    }

    public void show() {
        if (!mPlayerActive) {
            mPlayerActive = true;
            mVideoComponent.fadeIn();
            showController();
        }
    }

    public void hide() {
        if (mPlayerActive) {
            mWidgetAutoHideTimer.cancel();
            mVideoComponent.pauseVideo();
            mVideoComponent.fadeOut();
            hideController();
            mPlayerActive = false;
        }
    }
}
