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

package org.gearvrf.sample.gvrcardboardaudio;

import android.os.Bundle;

import com.google.vr.sdk.audio.GvrAudioEngine;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Threads;

public final class SpatialAudioActivity extends GVRActivity
{
    private GvrAudioEngine cardboardAudioEngine;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        cardboardAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
        setMain(new SpatialAudioMain(cardboardAudioEngine), "gvr.xml");
    }

    @Override
    public void onPause() {
        cardboardAudioEngine.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        cardboardAudioEngine.resume();
    }


    private static final class SpatialAudioMain extends GVRMain
    {
        private GVRCameraRig cameraRig;
        private static final String SOUND_FILE = "cube_sound.wav";
        private GvrAudioEngine audioEngine;
        private volatile int soundId = GvrAudioEngine.INVALID_ID;
        private float modelX = 0.0f;
        private float modelY = -1.5f;
        private float modelZ = -9.0f;

        public SpatialAudioMain(GvrAudioEngine audioEngine) {
            this.audioEngine = audioEngine;
        }

        @Override
        public void onInit(GVRContext gvrContext) throws Throwable {
            GVRScene scene = gvrContext.getMainScene();
            cameraRig = scene.getMainCameraRig();

            GVRModelSceneObject r2d2Model = gvrContext.getAssetLoader().loadModel("R2D2/R2D2.dae");
            r2d2Model.getTransform().setPosition(modelX, modelY, modelZ);
            scene.addSceneObject(r2d2Model);

            // add a floor
            final GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.floor));
            GVRSceneObject floor = new GVRSceneObject(gvrContext, 120.0f, 120.0f, texture);

            floor.getTransform().setRotationByAxis(-90, 1, 0, 0);
            floor.getTransform().setPositionY(-1.5f);
            floor.getRenderData().setRenderingOrder(0);
            scene.addSceneObject(floor);

            // Avoid any delays during start-up due to decoding of sound files.
            Threads.spawn(
                    new Runnable() {
                        public void run() {
                            // Start spatial audio playback of SOUND_FILE at the model postion. The returned
                            //soundId handle is stored and allows for repositioning the sound object whenever
                            // the cube position changes.
                            audioEngine.preloadSoundFile(SOUND_FILE);
                            soundId = audioEngine.createSoundObject(SOUND_FILE);
                            audioEngine.setSoundObjectPosition(soundId, modelX, modelY, modelZ);
                            audioEngine.playSound(soundId, true /* looped playback */);
                        }
                    });

            updateModelPosition();
        }

        private void updateModelPosition() {
            // Update the sound location to match it with the new cube position.
            if (soundId != GvrAudioEngine.INVALID_ID) {
                audioEngine.setSoundObjectPosition(soundId, modelX, modelY, modelZ);
            }
        }

        @Override
        public void onStep() {
            // Update the 3d audio engine with the most recent head rotation.
            float headX = cameraRig.getHeadTransform().getRotationX();
            float headY = cameraRig.getHeadTransform().getRotationY();
            float headZ = cameraRig.getHeadTransform().getRotationZ();
            float headW = cameraRig.getHeadTransform().getRotationW();
            audioEngine.setHeadRotation(headX, headY, headZ, headW);

            // update audio position if need be
            updateModelPosition();
        }
    }

}
