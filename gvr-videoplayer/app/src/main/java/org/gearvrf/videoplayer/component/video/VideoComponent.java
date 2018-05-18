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

package org.gearvrf.videoplayer.component.video;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public class VideoComponent extends GVRSceneObject {

    private static final String TAG = VideoComponent.class.getSimpleName();
    private static final float ANIM_OPACITY_DURATION = 0.5f;

    private GVRContext mGvrContext;
    private DefaultExoPlayer mMediaPlayer;
    private float mWidth, mHeight;
    private boolean mActive;
    private File[] mFiles;
    private LinkedList<File> mPlayingNowQueue;
    private DataSource.Factory mFileDataSourceFactory;
    private DataSource.Factory mAssetDataSourceFactory;
    private File mPlayingNow;
    private OnVideoPlayerListener mOnVideoPlayerListener;
    private ProgressHandler mProgressHandler = new ProgressHandler();
    private boolean mIsPlaying;

    private Player.EventListener mPlayerListener = new Player.DefaultEventListener() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            mIsPlaying = playbackState == Player.STATE_READY && playWhenReady;

            if (playbackState == Player.STATE_READY) {

                if (playWhenReady) {
                    logd("Video started " + mPlayingNow.getName());
                    notifyVideoStarted();
                    mProgressHandler.start();
                } else {
                    logd("Video prepared: " + mPlayingNow.getName());
                    notifyVideoPrepared(mPlayingNow.getName(), mMediaPlayer.getDuration());
                }

            } else if (playbackState == Player.STATE_ENDED) {

                logd("Video ended: " + mPlayingNow.getName());
                notifyVideoEnded();

                if (mPlayingNowQueue.isEmpty()) {
                    logd("All videos ended: " + mPlayingNow.getName());
                    resetQueue();
                    notifyAllVideosEnded();
                }

                // Goes to IDLE state
                mMediaPlayer.stop();

            } else if (playbackState == Player.STATE_IDLE) {

                prepareNextFile();

            } else if (playbackState == Player.STATE_BUFFERING) {

                logd("Loading video: " + mPlayingNow.getName());
                notifyVideoLoading();
            }
        }
    };

    public VideoComponent(final GVRContext gvrContext, float width, float height) {
        super(gvrContext, 0, 0);

        this.mGvrContext = gvrContext;
        this.mWidth = width;
        this.mHeight = height;

        this.mFileDataSourceFactory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new FileDataSource();
            }
        };

        this.mAssetDataSourceFactory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new AssetDataSource(gvrContext.getContext());
            }
        };

        createVideoSceneObject();
    }

    private void createVideoSceneObject() {
        mMediaPlayer = new DefaultExoPlayer(ExoPlayerFactory.newSimpleInstance(mGvrContext.getContext(), new DefaultTrackSelector()));
        mMediaPlayer.getPlayer().setRepeatMode(Player.REPEAT_MODE_OFF);
        mMediaPlayer.getPlayer().addListener(mPlayerListener);
        addChildObject(new GVRVideoSceneObject(mGvrContext, mWidth, mHeight, mMediaPlayer, GVRVideoType.MONO));
    }

    public void playVideo() {
        logd("Request play video");
        playCurrentPrepared();
    }

    public void pauseVideo() {
        mMediaPlayer.pause();
        logd("Video paused");
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    public long getProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    public File getPlayingNow() {
        return mPlayingNow;
    }

    public void setProgress(long progress) {
        mMediaPlayer.seekTo(progress);
    }

    public void showVideo() {
        new GVROpacityAnimation(this, ANIM_OPACITY_DURATION, 1).start(mGvrContext.getAnimationEngine());
        mActive = true;
    }

    public void hideVideoComponent() {
        mActive = false;
        mMediaPlayer.stop();
        mMediaPlayer.release();
        new GVROpacityAnimation(this, .1f, 0).start(mGvrContext.getAnimationEngine());
        mGvrContext.getMainScene().removeSceneObject(this);
    }

    public boolean isActive() {
        return mActive;
    }

    public void prepare(@NonNull File[] files) {
        mMediaPlayer.stop();
        if (files.length > 0) {
            mFiles = files;
            mPlayingNowQueue = new LinkedList<>(Arrays.asList(mFiles));
            prepareNextFile();
        } else {
            logd("Files array is empty");
        }
    }

    private void resetQueue() {
        mPlayingNowQueue = new LinkedList<>(Arrays.asList(mFiles));
    }

    private void prepareNextFile() {
        mMediaPlayer.stop();
        if (!mPlayingNowQueue.isEmpty()) {
            MediaSource mediaSource = new ExtractorMediaSource(
                    Uri.fromFile(mPlayingNow = mPlayingNowQueue.pop()),
                    mFileDataSourceFactory,
                    new DefaultExtractorsFactory(), null, null
            );
            mMediaPlayer.prepare(mediaSource);
        }
    }

    private void playCurrentPrepared() {
        mMediaPlayer.start();
    }

    public void playDefault() {
        logd("Playing default file " + Uri.parse("asset:///dinos.mp4"));
        mMediaPlayer.stop();
        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse("asset:///dinos.mp4"),
                mAssetDataSourceFactory,
                new DefaultExtractorsFactory(), null, null);
        mMediaPlayer.prepare(mediaSource);
        playCurrentPrepared();
    }

    public void setOnVideoPlayerListener(OnVideoPlayerListener listener) {
        this.mOnVideoPlayerListener = listener;
    }

    private void notifyVideoPrepared(String title, long duration) {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onPrepare(title, duration);
        }
    }

    private void notifyVideoStarted() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onStart();
        }
    }

    private void notifyVideoEnded() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onEnd();
        }
    }

    private void notifyAllVideosEnded() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onAllEnd();
        }
    }

    private void notifyVideoLoading() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onLoading();
        }
    }

    private static void logd(String text) {
        android.util.Log.d(TAG, text);
    }

    @SuppressLint("HandlerLeak")
    private class ProgressHandler extends Handler {

        ProgressHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (mOnVideoPlayerListener != null && mIsPlaying) {
                mOnVideoPlayerListener.onProgress(mMediaPlayer.getCurrentPosition());
                sendEmptyMessageDelayed(0, 100);
            } else {
                logd("Progress stopped");
            }
        }

        void start() {
            logd("Progress started");
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }
    }
}
