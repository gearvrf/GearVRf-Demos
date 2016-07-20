package org.gearvrf.balloons;

import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

public class Sound extends GVRBehavior
{
    private MediaPlayer         mPlayer;
    private int                 mSoundID;
    private boolean             mLoop;
    private Vector3f            mCurPos = new Vector3f(0, 0, 0);
    static private long TYPE_SOUND = newComponentType(Sound.class);

    public Sound(GVRContext ctx, final String soundFile, boolean loop) throws IOException
    {
        super(ctx);
        mType = TYPE_SOUND;
        mLoop = loop;
        mPlayer = new MediaPlayer();
        AssetFileDescriptor descriptor = ctx.getContext().getAssets().openFd(soundFile);
        mPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
        descriptor.close();
        mPlayer.setLooping(mLoop);
    }

    static public long getComponentType() { return TYPE_SOUND; }

    public void setVolume(float v)
    {
        if (mSoundID != 0)
        {
            mPlayer.setVolume(v, v);
        }
    }

    public void play()
    {
        if (mPlayer != null)
        {
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer)
                {
                    mPlayer.reset();
                }
            });
            mPlayer.start();
        }
    }

    public void stop()
    {
        if (mPlayer != null)
        {
            mPlayer.release();
            mPlayer = null;
        }
    }

}
