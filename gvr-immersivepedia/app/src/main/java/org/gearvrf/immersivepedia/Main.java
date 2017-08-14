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

package org.gearvrf.immersivepedia;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.immersivepedia.focus.FocusableController;
import org.gearvrf.immersivepedia.focus.PickHandler;
import org.gearvrf.immersivepedia.input.TouchPadInput;
import org.gearvrf.immersivepedia.scene.DinosaurScene;
import org.gearvrf.immersivepedia.scene.MenuScene;
import org.gearvrf.immersivepedia.util.AudioClip;
import org.gearvrf.immersivepedia.util.FPSCounter;

import android.media.MediaPlayer;

public class Main extends GVRMain {

    private static GVRContext mGvrContext;

    private MenuScene menuScene;
    public static DinosaurScene dinosaurScene;
    private static MediaPlayer mediaPlayer;
    private PickHandler mPickHandler;
    private GVRPicker mPicker;

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        mGvrContext = gvrContext;

        AudioClip.getInstance(gvrContext.getContext());
        mediaPlayer = MediaPlayer.create(gvrContext.getContext(),
                R.raw.sfx_ambient_1_1);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.start();

        dinosaurScene = new DinosaurScene(gvrContext);
        menuScene = new MenuScene(gvrContext);

        GazeController.setupGazeCursor(gvrContext);
        closeSplashScreen();

        gvrContext.runOnGlThreadPostRender(64, new Runnable() {
            @Override
            public void run() {
                setMainScene(menuScene);
                GazeController.enableGaze();
            }
        });

        // Set up to handle picking events
        mPickHandler = new PickHandler();
        mPicker = new GVRPicker(gvrContext, menuScene);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.AUTOMATIC;
    }

    @Override
    public void onStep() {

        TouchPadInput.process();
        FPSCounter.tick();

        if (mGvrContext.getMainScene().equals(dinosaurScene)) {
            dinosaurScene.onStep();
        }
    }

    public void onSingleTapConfirmed() {
        if (null != mGvrContext) {
            FocusableController.clickProcess(mGvrContext, mPickHandler);
        }
    }

    public void onSwipe() {
        FocusableController.swipeProcess(mGvrContext, mPickHandler);
    }

    public static void clickOut() {
        if (null != dinosaurScene && mGvrContext.getMainScene().equals(Main.dinosaurScene)) {
            Main.dinosaurScene.closeObjectsInScene();
        }
    }

    public void onPause() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
        }
        if (null != dinosaurScene) {
            dinosaurScene.onPause();
        }
        // Pause all active streams.
        if (null != mGvrContext) {
            AudioClip.getInstance(mGvrContext.getContext()).autoPause();
        }
    }

    public void setMainScene(GVRScene newScene)
    {
        GVRScene oldScene = getGVRContext().getMainScene();
        oldScene.getEventReceiver().removeListener(mPickHandler);
        oldScene.getMainCameraRig().getHeadTransformObject().detachComponent(GVRPicker.getComponentType());
        newScene.getEventReceiver().addListener(mPickHandler);
        mPicker = new GVRPicker(mGvrContext, newScene);
        newScene.getMainCameraRig().getHeadTransformObject().attachComponent(mPicker);
        getGVRContext().setMainScene(newScene);
    }

    //@Override
    public boolean onBackPress() {
        final GVRScene mainScene = getGVRContext().getMainScene();
        if (dinosaurScene == mainScene) {
            menuScene = new MenuScene(getGVRContext());
            setMainScene(menuScene);
            GazeController.enableGaze();
            return true;
        }
        return false;
    }
}