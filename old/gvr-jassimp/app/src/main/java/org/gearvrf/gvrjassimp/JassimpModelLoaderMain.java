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

package org.gearvrf.gvrjassimp;

import android.graphics.Color;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IAssetEvents;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JassimpModelLoaderMain extends GVRMain {

    @SuppressWarnings("unused")
    private static final String TAG = Log
            .tag(JassimpModelLoaderMain.class);

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;
    private List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

    @Override
    public void onInit(GVRContext gvrContext) {
        mAnimationEngine = gvrContext.getAnimationEngine();
        mMainScene = gvrContext.getNextMainScene();
        mMainScene.setFrustumCulling(true);

        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getTransform().setPosition(0.0f, 0.0f, 0.0f);

        // Model with texture and animation
        try {
            GVRModelSceneObject astroBoyModel = gvrContext.getAssetLoader().loadModel("astro_boy.dae", mMainScene);
            List<GVRAnimation> animations = astroBoyModel.getAnimations();
            if (animations.size() >= 1) {
                GVRAnimation animation = animations.get(0);
                animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
                mAnimations.add(animation);
            }

            astroBoyModel.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
            astroBoyModel.getTransform().setScale(3, 3, 3);
            astroBoyModel.getTransform().setPosition(0.0f, -0.4f, -0.5f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load a model: %s", e);
        }

        // Model with color
        try {
            GVRSceneObject benchModel = gvrContext.getAssetLoader().loadModel("bench.dae", mMainScene);

            benchModel.getTransform().setScale(0.66f, 0.66f, 0.66f);
            benchModel.getTransform().setPosition(0.0f, -4.0f, -20.0f);
            benchModel.getTransform().setRotationByAxis(180.0f, 0.0f, 1.0f, 0.0f);
            } catch (IOException e) {
            Log.e(TAG, "Failed to load a model: %s", e);
        }

        // Model over network
        String urlBase = "https://github.com/gearvrf/GearVRf-Demos/raw/master/old/gvr-jassimp/app/src/main/assets/";
        AssetListener eventHandler = new AssetListener(mMainScene);
        try {
            GVRSceneObject treesModel = gvrContext.getAssetLoader().loadModel(urlBase + "trees/trees9.3ds", eventHandler);
            treesModel.getTransform().setPosition(5.0f, 0.0f, 0.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load a model from URL: %s", e);
        } catch (SecurityException se) {
            Log.e(TAG, "Failed to load a model from URL: %s", se);
        }
        for (GVRAnimation animation : mAnimations) {
            animation.start(mAnimationEngine);
        }
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.MANUAL;
    }

    void onTap() {
        // toggle whether stats are displayed.
        boolean statsEnabled = mMainScene.getStatsEnabled();
        mMainScene.setStatsEnabled(!statsEnabled);
    }

    class AssetListener implements IAssetEvents
    {
        private GVRScene mScene;

        public AssetListener(GVRScene scene) {
            mScene = scene;
        }

        public void onModelLoaded(GVRContext context, GVRSceneObject model, String modelFile) { }
        public void onTextureLoaded(GVRContext context, GVRTexture texture, String texFile) {}
        public void onModelError(GVRContext context, String error, String modelFile) { }
        public void onTextureError(GVRContext context, String error, String texFile) { }
        public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath, String errors)
        {
            if ((model != null) && (errors == null))
            {
                Log.d("gvrjassimp", filePath + " loaded successfully");
                mScene.addSceneObject(model);
                closeSplashScreen();
            }
            else
            {
                Log.e("gvrjassimp", filePath + " did not load " + errors);
                getGVRContext().getActivity().finish();
            }
        }
    }

}
