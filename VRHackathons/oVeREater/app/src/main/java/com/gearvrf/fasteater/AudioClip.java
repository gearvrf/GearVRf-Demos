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

package com.gearvrf.fasteater;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class AudioClip {

    private static AudioClip instance = null;
    private boolean plays = false;
    private boolean loaded = false;
    private int priority;

    private Context context;
    private SoundPool soundPool;
    private static int uiSoundDrinkID, uiSoundBGID, uiSoundEatID, uiSoundGrenadeID;

    public static synchronized AudioClip getInstance(Context androidContext) {
        if (instance == null) {
            instance = new AudioClip(10, AudioManager.STREAM_MUSIC, 0, androidContext);
        }
        return instance;
    }

    private AudioClip(int maxStreams, int streamType, int srcQuality, Context context) {
        this.context = context;
        soundPool = new SoundPool(maxStreams, streamType, srcQuality);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        loadinSounds();
    }

    public int playSound(int soundID, float leftVolume, float rightVolume) {
        int streamID = 0;
        if (loaded) {
            streamID = soundPool.play(soundID, leftVolume, rightVolume, 1, 0, 1f);
            priority = priority++;
            plays = true;
        }

        return streamID;
    }

    public int playLoop(int soundID, float leftVolume, float rightVolume) {
        int streamID = 0;
        if (loaded) {
            streamID = soundPool.play(soundID, leftVolume, rightVolume, 1, -1, 1f);
            priority = priority++;
            plays = true;
        }
        return streamID;
    }

    public void pauseSound(int streamID) {
        if (plays) {
            soundPool.pause(streamID);
            plays = false;
        }
    }

    public void stopSound(int streamID) {
        if (plays) {
            soundPool.stop(streamID);
            plays = false;
        }
    }

    private void loadinSounds() {
        uiSoundBGID = soundPool.load(context, R.raw.citymusic, 1);
        uiSoundEatID = soundPool.load(context, R.raw.splat, 1);
        uiSoundDrinkID = soundPool.load(context, R.raw.slrup, 1);
        uiSoundGrenadeID = soundPool.load(context, R.raw.bomb, 1);
    }

    public static int getUISoundBGID() {
        return uiSoundBGID;
    }

    public static int getUISoundDrinkID() {
        return uiSoundDrinkID;
    }

    public static int getUISoundEatID() {
        return uiSoundEatID;
    }

    public static int getUISoundGrenadeID() {
        return uiSoundGrenadeID;
    }

}
