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

package org.gearvrf.videoplayer.component.video;

import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;

import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class DefaultExoPlayer implements GVRVideoSceneObjectPlayer<ExoPlayer> {

    private SimpleExoPlayer mSimpleExoPlayer;

    public DefaultExoPlayer(SimpleExoPlayer mSimpleExoPlayer) {
        this.mSimpleExoPlayer = mSimpleExoPlayer;
    }

    @Override
    public ExoPlayer getPlayer() {
        return mSimpleExoPlayer;
    }

    @Override
    public void setSurface(final Surface surface) {
        mSimpleExoPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        Log.d("DefaultExoPlayer", "onPlayerStateChanged: STATE_BUFFERING");
                        break;
                    case Player.STATE_ENDED:
                        Log.d("DefaultExoPlayer", "onPlayerStateChanged: STATE_ENDED");
                        break;
                    case Player.STATE_IDLE:
                        Log.d("DefaultExoPlayer", "onPlayerStateChanged: STATE_IDLE");
                        break;
                    case Player.STATE_READY:
                        Log.d("DefaultExoPlayer", "onPlayerStateChanged: STATE_READY");
                        break;
                    default:
                        break;
                }
            }
        });

        mSimpleExoPlayer.setVideoSurface(surface);
    }

    @Override
    public void release() {
        mSimpleExoPlayer.release();
    }

    @Override
    public boolean canReleaseSurfaceImmediately() {
        return false;
    }

    @Override
    public void pause() {
        mSimpleExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void start() {
        mSimpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public boolean isPlaying() {
        return mSimpleExoPlayer.getPlayWhenReady();
    }

    public long getDuration() {
        return mSimpleExoPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return mSimpleExoPlayer.getCurrentPosition();
    }

    public void stop() {
        mSimpleExoPlayer.setPlayWhenReady(false);
        mSimpleExoPlayer.stop();
    }

    public void seekTo(long position) {
        mSimpleExoPlayer.seekTo(position);
    }

    public void prepare(MediaSource mediaSource) {
        mSimpleExoPlayer.prepare(mediaSource);
    }
}
