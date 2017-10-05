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

package org.gearvrf.video.movie;

import android.media.MediaPlayer;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.video.shaders.RadiosityShader;

import java.io.IOException;

public class MultiplexMovieTheater extends MovieTheater {

    GVRSceneObject background = null;
    GVRSceneObject screen = null;

    private boolean mIsImax = false;
    private float mTransitionWeight = 0.0f;
    private float mTransitionTarget = 0.0f;
    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    public MultiplexMovieTheater(GVRContext context, MediaPlayer player,
                                 GVRExternalTexture screenTexture) {
        super(context);
        try {
            // background
            GVRMesh backgroundMesh = context.getAssetLoader().loadMesh(
                    new GVRAndroidResource(context, "multiplex/theater_background.obj"));
            GVRTexture backgroundLightOffTexture = context.getAssetLoader().loadTexture(
                    new GVRAndroidResource(context, "multiplex/theater_background_light_off.jpg"));
            GVRTexture backgroundLightOnTexture = context.getAssetLoader().loadTexture(
                    new GVRAndroidResource(context, "multiplex/theater_background_light_on.jpg"));
            background = new GVRSceneObject(context, backgroundMesh, backgroundLightOffTexture);
            background.setName("background");
            background.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            // radiosity
            RadiosityShader radiosityShader = new RadiosityShader(context);
           // background.getRenderData().getMaterial().set(radiosityShader.getShaderId());
            background.getRenderData().setMaterial(new GVRMaterial(context, new GVRShaderId(RadiosityShader.class)));
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_OFF_KEY, backgroundLightOffTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_ON_KEY, backgroundLightOnTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.SCREEN_KEY, screenTexture);
            // screen
            GVRMesh screenMesh = context.getAssetLoader().loadMesh(new GVRAndroidResource(
                    context, "multiplex/screen.obj"));
            screen = new GVRVideoSceneObject(context, screenMesh, player,
                    screenTexture, GVRVideoSceneObject.GVRVideoType.MONO);
            screen.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            this.addChildObject(background);
            this.addChildObject(screen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideCinemaTheater() {
        background.getRenderData().setRenderMask(0);
        screen.getRenderData().setRenderMask(0);
    }

    @Override
    public void showCinemaTheater() {
        mFadeWeight = 0.0f;
        background.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                | GVRRenderData.GVRRenderMaskBit.Right);
        screen.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                | GVRRenderData.GVRRenderMaskBit.Right);
    }

    @Override
    public void switchOnLights() {
        background.getRenderData().getMaterial().setMainTexture(
                background.getRenderData().getMaterial().getTexture(RadiosityShader.TEXTURE_ON_KEY));
    }

    @Override
    public void switchOffLights() {
        background.getRenderData().getMaterial().setMainTexture(
                background.getRenderData().getMaterial().getTexture(RadiosityShader.TEXTURE_OFF_KEY));
    }

    @Override
    public void switchToImax() {
        if (mIsImax) {
            mIsImax = false;
            mTransitionTarget = 0.0f;
        } else {
            mIsImax = true;
            mTransitionTarget = 1.0f;
        }
    }

    @Override
    public void setShaderValues() {
        mTransitionWeight += 0.1f * (mTransitionTarget - mTransitionWeight);
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.WEIGHT_KEY, 0.1f);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.LIGHT_KEY, 2.0f);
        float scale = 1.0f + mTransitionWeight;
        if (scale >= 1.0f) {
            background.getTransform().setScale(scale, scale, 1.0f);
            screen.getTransform().setScale(scale, scale, 1.0f);
        }
    }
}
