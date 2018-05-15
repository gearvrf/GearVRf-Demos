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

package org.gearvrf.videoplayer;

import android.net.Uri;

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
    private VideoSceneObjectPlayer mediaPlayer;
    private float mWidth, mHeight;
    private boolean mActive;
    private LinkedList<File> mFiles;
    private DataSource.Factory mFileDataSourceFactory;
    private DataSource.Factory mAssetDataSourceFactory;

    VideoComponent(final GVRContext gvrContext, float width, float height) {
        super(gvrContext, 0, 0);

        this.mGvrContext = gvrContext;
        this.mWidth = width;
        this.mHeight = height;
        this.mFiles = new LinkedList<>();

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
        mediaPlayer = new VideoSceneObjectPlayer(ExoPlayerFactory.newSimpleInstance(mGvrContext.getContext(), new DefaultTrackSelector()));
        mediaPlayer.getPlayer().addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    logd("The player has finished playing the media");
                    playNext();
                }
            }
        });
        addChildObject(new GVRVideoSceneObject(mGvrContext, mWidth, mHeight, mediaPlayer, GVRVideoType.MONO));
    }

    public void playVideo() {
        mediaPlayer.start();
    }

    public void pauseVideo() {
        mediaPlayer.pause();
    }

    public boolean isPlaying() {
        return mediaPlayer.getPlayer().getPlayWhenReady();
    }

    public long getDuration() {
        return mediaPlayer.getPlayer().getDuration();
    }

    public long getCurrentPosition() {
        return mediaPlayer.getPlayer().getCurrentPosition();
    }

    public void showVideo() {
        new GVROpacityAnimation(this, ANIM_OPACITY_DURATION, 1).start(mGvrContext.getAnimationEngine());
        mActive = true;
    }

    public void hideVideoComponent() {
        mActive = false;
        mediaPlayer.getPlayer().stop();
        mediaPlayer.release();
        mediaPlayer = null;
        new GVROpacityAnimation(this, .1f, 0).start(mGvrContext.getAnimationEngine());
        mGvrContext.getMainScene().removeSceneObject(this);
    }

    public boolean isActive() {
        return mActive;
    }

    public void playFiles(File[] files) {
        mediaPlayer.getPlayer().stop();
        this.mFiles.clear();
        if (files != null) {
            this.mFiles.addAll(Arrays.asList(files));
        }
        playNext();
    }

    private void playNext() {
        logd("Playing next file. Queue size: " + mFiles.size());
        if (!mFiles.isEmpty()) {
            MediaSource mediaSource = new ExtractorMediaSource(
                    Uri.fromFile(mFiles.pop()),
                    mFileDataSourceFactory,
                    new DefaultExtractorsFactory(), null, null
            );
            mediaPlayer.getPlayer().prepare(mediaSource);
            mediaPlayer.start();
        }
    }

    public void playDefault() {
        logd("Playing default file " + Uri.parse("asset:///dinos.mp4"));
        mediaPlayer.getPlayer().stop();
        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse("asset:///dinos.mp4"),
                mAssetDataSourceFactory,
                new DefaultExtractorsFactory(), null, null);
        mediaPlayer.getPlayer().prepare(mediaSource);
        mediaPlayer.start();
    }

    private static void logd(String text) {
        android.util.Log.d(TAG, text);
    }
}
