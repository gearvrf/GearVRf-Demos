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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;
import org.gearvrf.video.shaders.AdditiveShader;
import org.gearvrf.video.shaders.RadiosityShader;

import java.io.IOException;

public class IMAXMovieTheater extends MovieTheater {


    GVRSceneObject background = null;
    GVRSceneObject additive = null;
    GVRSceneObject screen = null;

    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    public IMAXMovieTheater(GVRContext context, GVRVideoSceneObjectPlayer player,
                            GVRExternalTexture screenTexture) {
        super(context);
        try {
            // background
            GVRMesh backgroundMesh = context.loadMesh(
                    new GVRAndroidResource(context, "imax/cinema.obj"));
            GVRTexture backgroundLightOffTexture = context.getAssetLoader().loadTexture(
                    new GVRAndroidResource(context, "imax/cinema_light_off.png"));
            GVRTexture backgroundLightOnTexture = context.getAssetLoader().loadTexture(
                    new GVRAndroidResource(context, "imax/cinema_light_on.png"));
            GVRMesh backgroundRadiosity = context.loadMesh(new GVRAndroidResource(context, "imax/radiosity1.obj"));
            backgroundMesh.setNormals(backgroundRadiosity.getVertices());
            background = new GVRSceneObject(context, backgroundMesh, backgroundLightOffTexture);
            background.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            GVRMesh additiveMesh = context.loadMesh(
                    new GVRAndroidResource(context, "imax/additive.obj"));
            GVRTexture additiveTexture = context.getAssetLoader().loadTexture(
                    new GVRAndroidResource(context, "imax/additive.png"));
            GVRMesh additiveRadiosity = context.loadMesh(new GVRAndroidResource(context, "imax/radiosity2.obj"));
            additiveMesh.setNormals(additiveRadiosity.getVertices());
            additive = new GVRSceneObject(context, additiveMesh, additiveTexture);
            additive.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            additive.getRenderData().setRenderingOrder(2500);

            // radiosity
            RadiosityShader radiosityShader = new RadiosityShader(context);
            background.getRenderData().getMaterial().setShaderType(radiosityShader.getShaderId());
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_OFF_KEY, backgroundLightOffTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.TEXTURE_ON_KEY, backgroundLightOnTexture);
            background.getRenderData().getMaterial().setTexture(
                    RadiosityShader.SCREEN_KEY, screenTexture);
            AdditiveShader additiveShader = new AdditiveShader(context);
            additive.getRenderData().getMaterial().setShaderType(additiveShader.getShaderId());
            additive.getRenderData().getMaterial().setTexture(AdditiveShader.TEXTURE_KEY, additiveTexture);
            // screen
            GVRMesh screenMesh = context.loadMesh(new GVRAndroidResource(
                    context, "imax/screen.obj"));
            screen = new GVRVideoSceneObject(context, screenMesh, player,
                    screenTexture, GVRVideoSceneObject.GVRVideoType.MONO);
            screen.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            this.addChildObject(background);
            this.addChildObject(additive);
            this.addChildObject(screen);
            this.getTransform().setPosition(3.353f, -0.401f, 0.000003f);
            this.getTransform().rotateByAxisWithPivot(90.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideCinemaTheater() {
        background.getRenderData().setRenderMask(0);
        additive.getRenderData().setRenderMask(0);
        screen.getRenderData().setRenderMask(0);
    }

    @Override
    public void showCinemaTheater() {
        mFadeWeight = 0.0f;
        background.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                | GVRRenderData.GVRRenderMaskBit.Right);
        additive.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
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
        // Nothing to do as Imax mode is not available
    }

    @Override
    public void setShaderValues() {
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.WEIGHT_KEY, 0.1f);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
        background.getRenderData().getMaterial()
                .setFloat(RadiosityShader.LIGHT_KEY, 1.0f);
        additive.getRenderData().getMaterial()
                .setFloat(AdditiveShader.WEIGHT_KEY, 0.1f);
        additive.getRenderData().getMaterial()
                .setFloat(AdditiveShader.FADE_KEY, mFadeWeight);
    }
}
