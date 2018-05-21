package org.gearvrf.videoplayer.component.video;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVROpacityAnimation;

import java.io.File;

public class VideoPlayer extends GVRSceneObject {

    private static final String TAG = VideoPlayer.class.getSimpleName();
    private static final int CONTROLLER_AUTO_HIDE_DELAY = 5000;
    private static final float CONTROLLER_WIDGET_FACTOR = .75f;
    private static final float CONTROLLER_HEIGHT_FACTOR = .25f;
    private static final float DURATION = 5000.0f;

    private VideoComponent mVideoComponent;
    private VideoControllerComponent mVideoControllerComponent;
    private boolean mIsControllerActive = true;
    private boolean mAutoHideController;
    private ControllerAutoHideHandler mControllerAutoHideHandler;

    public VideoPlayer(GVRContext gvrContext, float width, float height) {
        super(gvrContext);
        mControllerAutoHideHandler = new ControllerAutoHideHandler(this);
        addVideoComponent(width, height);
        addVideoControllerComponent(CONTROLLER_WIDGET_FACTOR * width, CONTROLLER_HEIGHT_FACTOR * height);
    }

    public void prepare(File... files) {
        if (files != null && files.length > 0) {
            mVideoComponent.prepare(files);
        } else {
            mVideoComponent.prepareDefault(); // from assets folder
        }
    }

    public void play() {
        mVideoComponent.playVideo();
        mVideoControllerComponent.showPause();
    }

    public void pause() {
        mVideoComponent.pauseVideo();
        mVideoControllerComponent.showPlay();
    }

    public void hideController() {
        Log.d(TAG, "hideController: ");
        if (mIsControllerActive) {
            mIsControllerActive = false;
            new GVROpacityAnimation(mVideoControllerComponent, .1f, 0)
                    .start(getGVRContext().getAnimationEngine());
            removeChildObject(mVideoControllerComponent);
        }
    }

    public void showController() {
        Log.d(TAG, "showController: ");
        showController(mAutoHideController);
    }

    private void showController(boolean autoHide) {
        Log.d(TAG, "showController: ");
        if (!mIsControllerActive) {
            mIsControllerActive = true;
            new GVROpacityAnimation(mVideoControllerComponent, .1f, 1)
                    .start(getGVRContext().getAnimationEngine());
            addChildObject(mVideoControllerComponent);
        }
        if (autoHide) {
            mControllerAutoHideHandler.start();
        }
    }

    public void setAutoHideController(boolean autoHide) {
        mAutoHideController = autoHide;
        if (autoHide) {
            mControllerAutoHideHandler.start();
        }
    }

    private void addVideoComponent(float width, float height) {
        mVideoComponent = new VideoComponent(getGVRContext(), width, height);
        mVideoComponent.setOnVideoPlayerListener(mOnVideoPlayerListener);
        addChildObject(mVideoComponent);
    }

    private void addVideoControllerComponent(float width, float height) {

        mVideoControllerComponent = new VideoControllerComponent(getGVRContext(), width, height);
        mVideoControllerComponent.setOnVideoControllerListener(mOnVideoControllerListener);

        // Place video control widget below the video screen
        float positionY = -((height / CONTROLLER_HEIGHT_FACTOR / 2f) + height / 2f);
        mVideoControllerComponent.getTransform().setPositionY(positionY * 1.02f);

        // Adjust widget vision degree
        mVideoControllerComponent.getTransform().rotateByAxis(-15, 1, 0, 0);

        addChildObject(mVideoControllerComponent);
    }

    private OnVideoPlayerListener mOnVideoPlayerListener = new OnVideoPlayerListener() {
        @Override
        public void onProgress(long progress) {
            mVideoControllerComponent.setProgress((int) progress);
        }

        @Override
        public void onPrepare(String title, long duration) {
            Log.d(TAG, "Video prepared: {title: " + title + ", duration: " + duration + "}");
            mVideoControllerComponent.setPlayPauseButtonEnabled(true);
            mVideoControllerComponent.setTitle(title);
            mVideoControllerComponent.setMaxProgress((int) duration);
            mVideoControllerComponent.setProgress((int) mVideoComponent.getProgress());
            mVideoControllerComponent.showPlay();
        }

        @Override
        public void onStart() {
            Log.d(TAG, "Video started");
            mVideoControllerComponent.setPlayPauseButtonEnabled(true);
            mVideoControllerComponent.showPause();
            showController();
        }

        @Override
        public void onLoading() {
            Log.d(TAG, "Video loading");
            mVideoControllerComponent.setPlayPauseButtonEnabled(false);
        }

        @Override
        public void onEnd() {
            Log.d(TAG, "Video ended");
            showController(false);
        }

        @Override
        public void onAllEnd() {
            Log.d(TAG, "All videos ended");
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

    private static class ControllerAutoHideHandler extends Handler {

        private VideoPlayer mVideoPlayer;

        ControllerAutoHideHandler(VideoPlayer videoPlayer) {
            mVideoPlayer = videoPlayer;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mVideoPlayer.hideController();
        }

        void start() {
            if (!hasMessages(0)) {
                sendEmptyMessageDelayed(0, CONTROLLER_AUTO_HIDE_DELAY);
            } else {
                removeMessages(0);
                sendEmptyMessageDelayed(0, CONTROLLER_AUTO_HIDE_DELAY);
            }
        }

        public void cancel() {
            removeMessages(0);
        }
    }

    private void showVideoController(GVRContext gvrContext) {
        new GVROpacityAnimation(mVideoControllerComponent, DURATION, 1).start(gvrContext.getAnimationEngine());
    }

    private void hideVideoController(GVRContext gvrContext) {
        new GVROpacityAnimation(mVideoControllerComponent, 1.0f, 0).start(gvrContext.getAnimationEngine());
        removeChildObject(mVideoControllerComponent);
    }
}
