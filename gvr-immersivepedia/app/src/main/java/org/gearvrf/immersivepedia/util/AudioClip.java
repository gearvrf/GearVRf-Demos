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

package org.gearvrf.immersivepedia.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

import org.gearvrf.immersivepedia.R;

public class AudioClip {

    private static AudioClip instance;
    private boolean loaded = false;
    private int priority;

    private Context context;
    private SoundPool soundPool;
    private static int uiImageCloseSoundID;
    private static int uiImageOpenSoundID;
    private static int uiLoadingSoundID;
    private static int uiMenuHoverSoundID;
    private static int uiMenuSelectSoundID;
    private static int uiMenuSelectWrongSoundID;
    private static int uiRotateSoundID;
    private static int uiLoopRotateSoundID;
    private static int uiTextAppearSoundID;
    private static int uiTextDisappearSoundID;

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
        }

        return streamID;
    }

    public int playLoop(int soundID, float leftVolume, float rightVolume) {
        int streamID = 0;
        if (loaded) {
            streamID = soundPool.play(soundID, leftVolume, rightVolume, 1, -1, 1f);
            priority = priority++;
        }
        return streamID;
    }

    public void pauseSound(int streamID) {
        // If the stream is not playing (e.g. is stopped or was previously paused), calling this function will have no effect.
        soundPool.pause(streamID);
    }

    public void stopSound(int streamID) {
        // If the stream is not playing, it will have no effect.
        soundPool.stop(streamID);
    }

    public void autoPause() {
        soundPool.autoPause();
    }

    private void loadinSounds() {
        uiImageCloseSoundID = soundPool.load(context, R.raw.sfx_ui_image_close_4_1, 1);
        uiImageOpenSoundID = soundPool.load(context, R.raw.sfx_ui_image_open_4_1, 1);
        uiLoadingSoundID = soundPool.load(context, R.raw.sfx_ui_loading_1, 1);
        uiMenuHoverSoundID = soundPool.load(context, R.raw.sfx_ui_menu_hover_2_2, 1);
        uiMenuSelectSoundID = soundPool.load(context, R.raw.sfx_ui_menu_select_1_1, 1);
        uiMenuSelectWrongSoundID = soundPool.load(context, R.raw.sfx_ui_menu_select_wrong_1_1, 1);
        uiRotateSoundID = soundPool.load(context, R.raw.sfx_ui_rotate_1_1, 1);
        uiTextAppearSoundID = soundPool.load(context, R.raw.sfx_text_appear_4_1, 1);
        uiTextDisappearSoundID = soundPool.load(context, R.raw.sfx_text_disappear_4_1, 1);
        uiLoopRotateSoundID = soundPool.load(context, R.raw.loop_rotate, 1);
    }

    public static int getUIImageCloseSoundID() {
        return uiImageCloseSoundID;
    }

    public static int getUIImageOpenSoundID() {
        return uiImageOpenSoundID;
    }

    public static int getUILoadingSoundID() {
        return uiLoadingSoundID;
    }

    public static int getUIMenuHoverSoundID() {
        return uiMenuHoverSoundID;
    }

    public static int getUIMenuSelectSoundID() {
        return uiMenuSelectSoundID;
    }

    public static int getUIMenuSelectWrongSoundID() {
        return uiMenuSelectWrongSoundID;
    }

    public static int getUIRotateSoundID() {
        return uiRotateSoundID;
    }

    public static int getUITextAppearSoundID() {
        return uiTextAppearSoundID;
    }

    public static int getUITextDisappearSoundID() {
        return uiTextDisappearSoundID;
    }

    public static int getUiLoopRotateSoundID() {
        return uiLoopRotateSoundID;
    }

}
