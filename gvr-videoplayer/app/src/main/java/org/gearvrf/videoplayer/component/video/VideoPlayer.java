package org.gearvrf.videoplayer.component.video;

import android.util.Log;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.io.File;

public class VideoPlayer extends GVRSceneObject {

    private static final String TAG = VideoPlayer.class.getSimpleName();

    private VideoComponent mVideoComponent;
    private VideoControllerComponent mVideoControllerComponent;

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
                mVideoControllerComponent.setPlayPauseButtonEnabled(true);
                Log.d(TAG, "Video started");
                mVideoControllerComponent.showPause();
            }

            @Override
            public void onLoading() {
                Log.d(TAG, "Video loading");
                mVideoControllerComponent.setPlayPauseButtonEnabled(false);
            }

            @Override
            public void onEnd() {
                Log.d(TAG, "Video ended");
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
}
