/* Copyright 2017 Samsung Electronics Co., LTD
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
package org.gearvrf.rendertotexture;

import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderTarget;
import org.gearvrf.GVRRenderTexture;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.List;

public final class RenderToTextureActivity extends GVRActivity {
    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setMain(new RenderToTextureMain());
    }

    final class RenderToTextureMain extends GVRMain {
        @Override
        public void onInit(final GVRContext gvrContext) {
            if (!GVRShader.isVulkanInstance()) {
                addLight();
            }
            final GVRSceneObject cube = addCube();
            final GVRScene scene = createRenderToTextureScene();

            gvrContext.runOnGlThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mRenderTexture = new GVRRenderTexture(gvrContext, 512, 512);
                    GVRRenderTarget renderTarget = new GVRRenderTarget(mRenderTexture, scene);

                    scene.getMainCameraRig().getOwnerObject().attachComponent(renderTarget);
                    //to prevent rendering untextured cube for few frames at the start
                    cube.getRenderData().getMaterial().setMainTexture(mRenderTexture);
                    closeSplashScreen();
                    renderTarget.setEnable(true);
                }
            });
        }

        private GVRScene createRenderToTextureScene() {
            final GVRScene newScene = new GVRScene(getGVRContext());

            final GVRPerspectiveCamera centerCamera = newScene.getMainCameraRig().getCenterCamera();
            centerCamera.setBackgroundColor(0.7f, 0.4f, 0, 1);

            try {
                final GVRModelSceneObject model = getGVRContext().getAssetLoader().loadModel("astro_boy.dae", newScene);

                model.getTransform()
                        .setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f)
                        .setScale(2, 2, 2)
                        .setPosition(0.0f, -0.15f, -0.3f);

                final List<GVRAnimation> animations = model.getAnimations();
                if (animations.size() >= 1) {
                    final GVRAnimation animation = animations.get(0);
                    animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
                    animation.start(getGVRContext().getAnimationEngine());
                }
            } catch (final IOException e) {
                Log.e(TAG, "Failed to load a model: %s", e);
                getGVRContext().getActivity().finish();
            }

            return newScene;
        }

        private GVRSceneObject addCube() {
            final GVRSceneObject cube = new GVRCubeSceneObject(getGVRContext(), true);
            mCubeTransform = cube.getTransform();
            mCubeTransform.setPosition(0, 0, -4f).setScale(2, 2, 2);

            getGVRContext().getMainScene().addSceneObject(cube);
            return cube;
        }

        private void addLight() {
            final GVRContext context = getGVRContext();

            final GVRSpotLight light = new GVRSpotLight(context);
            light.setAmbientIntensity(0.8f, 0.8f, 0.8f, 1);
            light.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 1);
            light.setSpecularIntensity(0.8f, 0.8f, 0.8f, 1);
            light.setInnerConeAngle(8);
            light.setOuterConeAngle(24);

            final GVRSceneObject lightNode = new GVRSceneObject(context);
            lightNode.attachLight(light);
            lightNode.getTransform().setPosition(0, 1, 3);

            context.getMainScene().addSceneObject(lightNode);
        }

        @Override
        public void onStep() {
            mCubeTransform.rotateByAxis(0.25f, 0, 0, 1);
            mCubeTransform.rotateByAxis(0.5f, 1, 0, 0);
            mCubeTransform.rotateByAxis(0.25f, 0, 1, 0);
        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.MANUAL;
        }

        private volatile GVRRenderTexture mRenderTexture;
        private GVRTransform mCubeTransform;
        private static final String TAG = "RenderToTextureMain";
    }

}
