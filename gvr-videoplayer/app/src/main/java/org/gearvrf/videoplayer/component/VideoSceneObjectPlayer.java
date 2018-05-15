package org.gearvrf.videoplayer.component;

import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class VideoSceneObjectPlayer implements GVRVideoSceneObjectPlayer<ExoPlayer> {

    private SimpleExoPlayer mSimpleExoPlayer;

    public VideoSceneObjectPlayer(SimpleExoPlayer mSimpleExoPlayer) {
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
                        break;
                    case Player.STATE_ENDED:
                        mSimpleExoPlayer.seekTo(0);
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
}
