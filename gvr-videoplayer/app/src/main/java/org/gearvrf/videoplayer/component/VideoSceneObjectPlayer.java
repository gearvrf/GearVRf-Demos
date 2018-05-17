package org.gearvrf.videoplayer.component;

import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;

import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

public class VideoSceneObjectPlayer implements GVRVideoSceneObjectPlayer<ExoPlayer> {

    private SimpleExoPlayer mSimpleExoPlayer;

    VideoSceneObjectPlayer(SimpleExoPlayer mSimpleExoPlayer) {
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
                        Log.d("VideoSceneObjectPlayer", "onPlayerStateChanged: STATE_BUFFERING");
                        break;
                    case Player.STATE_ENDED:
                        Log.d("VideoSceneObjectPlayer", "onPlayerStateChanged: STATE_ENDED");
                        break;
                    case Player.STATE_IDLE:
                        Log.d("VideoSceneObjectPlayer", "onPlayerStateChanged: STATE_IDLE");
                        break;
                    case Player.STATE_READY:
                        Log.d("VideoSceneObjectPlayer", "onPlayerStateChanged: STATE_READY");
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
