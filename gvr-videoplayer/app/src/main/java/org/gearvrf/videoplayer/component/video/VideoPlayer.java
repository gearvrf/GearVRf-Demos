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

    private VideoComponent mVideoComponent;
    private VideoControllerComponent mVideoControllerComponent;
    private boolean mIsControllerActive = true;
    private boolean mIsAutoHideController;
    private ControllerAutoHideHandler mControllerAutoHideHandler = new ControllerAutoHideHandler();

    public VideoPlayer(GVRContext gvrContext) {
        super(gvrContext);
        addVideoComponent();
        addVideoControllerComponent();
        initComponents();
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

    private void hideController() {
        Log.d(TAG, "hideController: ");
        if (mIsControllerActive) {
            new GVROpacityAnimation(mVideoControllerComponent, .1f, 0).start(getGVRContext().getAnimationEngine());
            removeChildObject(mVideoControllerComponent);
        }
    }

    public void showController() {
        Log.d(TAG, "showController: ");
        showController(mIsAutoHideController);
    }

    private void showController(boolean autoHide) {
        Log.d(TAG, "showController: ");
        if (!mIsControllerActive) {
            new GVROpacityAnimation(mVideoControllerComponent, .1f, 1).start(getGVRContext().getAnimationEngine());
            addChildObject(mVideoControllerComponent);
        }
        if (autoHide) {
            mControllerAutoHideHandler.start();
        }
    }

    public void setIsAutoHideController(boolean autoHide) {
        mIsAutoHideController = autoHide;
        if (autoHide) {
            mControllerAutoHideHandler.start();
        }
    }

    private void addVideoComponent() {
        mVideoComponent = new VideoComponent(getGVRContext(), 8f, 4f);
        mVideoComponent.getTransform().setPosition(0.0f, 0.0f, -7.0f);
        addChildObject(mVideoComponent);
    }

    private void addVideoControllerComponent() {
        mVideoControllerComponent = new VideoControllerComponent(getGVRContext(), 6f, 1f);
        mVideoControllerComponent.getTransform().setPosition(0.0f, -2.5f, -6.5f);
        mVideoControllerComponent.getTransform().rotateByAxis(-15, 1, 0, 0);
        addChildObject(mVideoControllerComponent);
    }

    private void initComponents() {

        mVideoComponent.setOnVideoPlayerListener(new OnVideoPlayerListener() {
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
        });

        mVideoControllerComponent.setOnVideoControllerListener(new OnVideoControllerListener() {
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
        });
    }

    private class ControllerAutoHideHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hideController();
        }

        public void start() {
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
}
