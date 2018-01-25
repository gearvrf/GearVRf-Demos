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
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.resonanceaudio.GVRAudioManager;
import org.gearvrf.resonanceaudio.GVRAudioSource;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Threads;

public final class SpatialAudioActivity extends GVRActivity
{
    private SpatialAudioMain mMain;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mMain = new SpatialAudioMain();

        setMain(mMain, "gvr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mMain.toggleListener();
        }

        return super.onTouchEvent(event);
    }

    private static final class SpatialAudioMain extends GVRMain
    {
        private static final String SOUND_FILE = "cube_sound.wav";
        GVRModelSceneObject r2d2Model;
        private float modelY = -1.5f;
        private GVRAudioManager audioListener;
        private float time = 0f;
        private final float distance = 10;

        @Override
        public void onInit(GVRContext gvrContext) throws Throwable {
            GVRScene scene = gvrContext.getMainScene();
            audioListener = new GVRAudioManager(gvrContext);

            r2d2Model = gvrContext.getAssetLoader().loadModel("R2D2/R2D2.dae");
            r2d2Model.getTransform().setPosition(distance * (float)Math.sin(time), modelY,
                    distance * (float)Math.cos(time));
            scene.addSceneObject(r2d2Model);

            final GVRAudioSource audioSource = new GVRAudioSource(gvrContext);
            audioListener.addSource(audioSource);
            r2d2Model.attachComponent(audioSource);

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
                            audioSource.load(SOUND_FILE);
                            audioSource.play(true);
                            audioListener.setEnable(true);
                        }
                    });
        }

        public void toggleListener() {
            if (audioListener != null) {
                audioListener.setEnable(!audioListener.isEnabled());
            }
        }

        @Override
        public void onStep() {
            if (r2d2Model != null) {
                time += 0.016f;

                r2d2Model.getTransform().setPosition(distance * (float) Math.sin(time), modelY,
                        distance * (float) Math.cos(time));
            }
        }
    }

}
