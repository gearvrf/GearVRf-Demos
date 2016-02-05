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

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRLight;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;


public class SpatialAudioScript extends GVRScript
{
    private GVRContext gvrContext;
    private GVRCameraRig cameraRig;
    private static final String SOUND_FILE = "cube_sound.wav";
    private CardboardAudioEngine audioEngine;
    private volatile int soundId = CardboardAudioEngine.INVALID_ID;
    private float modelX = 0.0f;
    private float modelY = -1.5f;
    private float modelZ = -9.0f;
    private GVRLight light;
    private static final float LIGHT_Z = 100.0f;

    public SpatialAudioScript(CardboardAudioEngine audioEngine) {
        this.audioEngine = audioEngine;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        GVRScene scene = gvrContext.getNextMainScene();
        cameraRig = scene.getMainCameraRig();

        // setup light
        light = new GVRLight(gvrContext);
        light.setPosition(0.0f, 0.0f, LIGHT_Z);
        light.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1.0f);
        light.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 1.0f);
        light.setSpecularIntensity(1.0f, 0.5f, 0.5f, 1.0f);

        try {
            GVRModelSceneObject r2d2Model = gvrContext.loadModel("R2D2/R2D2.dae");
            r2d2Model.getTransform().setPosition(modelX, modelY, modelZ);
            scene.addSceneObject(r2d2Model);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // add a floor
        GVRSceneObject floor = new GVRSceneObject(gvrContext, gvrContext.createQuad(120.0f, 120.0f),
        gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.floor)));

        GVRMaterial floorMaterial = floor.getRenderData().getMaterial();
        setupLight(floorMaterial);

        floor.getTransform().setRotationByAxis(-90, 1, 0, 0);
        floor.getTransform().setPositionY(-1.5f);
        floor.getRenderData().setRenderingOrder(0);
        floor.getRenderData().setLight(light);
        floor.getRenderData().enableLight();

        scene.addSceneObject(floor);

        // Avoid any delays during start-up due to decoding of sound files.
        new Thread(
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
            })
        .start();

        updateModelPosition();
    }

    private void setupLight(GVRMaterial material) {
        material.setColor(0.8f, 0.8f, 0.8f);
        material.setOpacity(1.0f);
        material.setAmbientColor(1.0f, 1.0f, 1.0f, 1.0f);
        material.setDiffuseColor(1.0f, 1.0f, 1.0f, 1.0f);
        material.setSpecularColor(1.0f, 1.0f, 1.0f, 1.0f);
        material.setSpecularExponent(128.0f);
    }

    private void updateModelPosition() {
        // Update the sound location to match it with the new cube position.
        if (soundId != CardboardAudioEngine.INVALID_ID) {
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
