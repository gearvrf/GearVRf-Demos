package org.gearvrf.balloons;

import android.media.MediaPlayer;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

public class SpatialSound extends GVRBehavior
{
    private CardboardAudioEngine mAudioEngine;
    private MediaPlayer         mMediaPlayer;
    private int                 mSoundID;
    private boolean             mLoop;
    private Vector3f            mCurPos = new Vector3f(0, 0, 0);
    static private long TYPE_SPATIAL_SOUND = newComponentType(SpatialSound.class);
    
    public SpatialSound(GVRContext ctx, CardboardAudioEngine audioEngine, final String soundFile, boolean loop)
    {
        super(ctx);
        mType = TYPE_SPATIAL_SOUND;
        mLoop = loop;
        mAudioEngine = audioEngine;
        // Avoid any delays during start-up due to decoding of sound files.
        Runnable task = new Runnable()
        {
            public void run()
            {
                mAudioEngine.preloadSoundFile(soundFile);
                mSoundID = mAudioEngine.createSoundObject(soundFile);
                setVolume(1.0f);
            }
        };
        new Thread(task).start();
    }

    static public long getComponentType() { return TYPE_SPATIAL_SOUND; }

    public void onAttach(GVRSceneObject owner)
    {
        play();
    }

    public void setVolume(float v)
    {
        if (mSoundID != 0)
        {
            mAudioEngine.setSoundVolume(mSoundID, v);
        }
    }

    public void play()
    {
        GVRSceneObject owner = getOwnerObject();
        if (mSoundID != 0)
        {
            if (owner != null)
            {
                Matrix4f worldmtx = owner.getTransform().getModelMatrix4f();
                worldmtx.getTranslation(mCurPos);
                mAudioEngine.setSoundObjectPosition(mSoundID, mCurPos.x, mCurPos.y, mCurPos.z);
            }
            mAudioEngine.playSound(mSoundID, mLoop);
        }
    }

    public void stop()
    {
        if (mSoundID != 0)
        {
            mAudioEngine.stopSound(mSoundID);
        }
    }
}
