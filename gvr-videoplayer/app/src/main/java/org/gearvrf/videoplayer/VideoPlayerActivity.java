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

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

import java.io.File;
import java.io.IOException;

import static android.view.MotionEvent.ACTION_DOWN;

public class VideoPlayerActivity extends GVRActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!USE_EXO_PLAYER) {
            videoSceneObjectPlayer = makeMediaPlayer();
        } else {
            videoSceneObjectPlayer = makeExoPlayer();
        }

        if (null != videoSceneObjectPlayer) {
            final VideoPlayerMain main = new VideoPlayerMain(videoSceneObjectPlayer);
            setMain(main, "gvr.xml");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != videoSceneObjectPlayer) {
            final Object player = videoSceneObjectPlayer.getPlayer();
            if (!USE_EXO_PLAYER) {
                MediaPlayer mediaPlayer = (MediaPlayer) player;
                mediaPlayer.pause();
            } else {
                ExoPlayer exoPlayer = (ExoPlayer) player;
                exoPlayer.setPlayWhenReady(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != videoSceneObjectPlayer) {
            final Object player = videoSceneObjectPlayer.getPlayer();
            if (!USE_EXO_PLAYER) {
                MediaPlayer mediaPlayer = (MediaPlayer) player;
                mediaPlayer.start();
            } else {
                ExoPlayer exoPlayer = (ExoPlayer) player;
                exoPlayer.setPlayWhenReady(true);
            }
        }
    }

    private GVRVideoSceneObjectPlayer<MediaPlayer> makeMediaPlayer() {
        final MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            final File file = new File(Environment.getExternalStorageDirectory() + "/gvr-360video/video.mp4");
            if (!file.exists()) {
                final AssetFileDescriptor afd = getAssets().openFd("video.mp4");
                android.util.Log.d("VideoPlayerMain", "Assets was found.");
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                android.util.Log.d("VideoPlayerMain", "DataSource was set.");
                afd.close();
            } else {
                mediaPlayer.setDataSource(file.getAbsolutePath());
            }

            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
            android.util.Log.e("VideoPlayerMain", "Assets were not loaded. Stopping application!");
            return null;
        }

        mediaPlayer.setLooping(true);
        android.util.Log.d("VideoPlayerMain", "starting player.");

        return GVRVideoSceneObject.makePlayerInstance(mediaPlayer);
    }

    private GVRVideoSceneObjectPlayer<ExoPlayer> makeExoPlayer() {
        final Context context = this;
        final DataSource.Factory dataSourceFactory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return new AssetDataSource(context);
            }
        };
        final MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("asset:///video.mp4"),
                dataSourceFactory,
                new DefaultExtractorsFactory(), null, null);

        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context,
                new DefaultTrackSelector());
        player.prepare(mediaSource);

        return new GVRVideoSceneObjectPlayer<ExoPlayer>() {
            @Override
            public ExoPlayer getPlayer() {
                return player;
            }

            @Override
            public void setSurface(final Surface surface) {
                player.addListener(new Player.DefaultEventListener() {
                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        switch (playbackState) {
                            case Player.STATE_BUFFERING:
                                break;
                            case Player.STATE_ENDED:
                                player.seekTo(0);
                                break;
                            case Player.STATE_IDLE:
                                break;
                            case Player.STATE_READY:
                                break;
                            default:
                                break;
                        }
                    }
                });

                player.setVideoSurface(surface);
            }

            @Override
            public void release() {
                player.release();
            }

            @Override
            public boolean canReleaseSurfaceImmediately() {
                return false;
            }

            @Override
            public void pause() {
                player.setPlayWhenReady(false);
            }

            @Override
            public void start() {
                player.setPlayWhenReady(true);
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != videoSceneObjectPlayer) {
            if (ACTION_DOWN == event.getAction()) {
                if (mPlaying) {
                    videoSceneObjectPlayer.pause();
                } else {
                    videoSceneObjectPlayer.start();
                }
                mPlaying = !mPlaying;
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean mPlaying = true;
    private GVRVideoSceneObjectPlayer<?> videoSceneObjectPlayer;

    static final boolean USE_EXO_PLAYER = false;
}
