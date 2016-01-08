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

package org.gearvrf.solarsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAtlasInformation;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.utility.Log;

public class SolarViewManager extends GVRScript {

    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(SolarViewManager.class);

    private GVRAnimationEngine mAnimationEngine;
    private GVRScene mMainScene;

    private GVRSceneObject asyncSceneObject(GVRContext context,
            String textureName) throws IOException {

        return new GVRSceneObject(context,
                new GVRAndroidResource(context, "sphere.obj"),
                new GVRAndroidResource(context, textureName));
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        mAnimationEngine = gvrContext.getAnimationEngine();

        mMainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                for (GVRAnimation animation : mAnimations) {
                    animation.start(mAnimationEngine);
                }

                // mMainScene.export( "/storage/sdcard0/Download/SolarSystem.obj");
                mAnimations = null;
            }
        });

        mMainScene.setFrustumCulling(true);

        mMainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mMainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);

        mMainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 0.0f, 0.0f);

        GVRSceneObject solarSystemObject = new GVRSceneObject(gvrContext);
        mMainScene.addSceneObject(solarSystemObject);

        GVRSceneObject sunRotationObject = new GVRSceneObject(gvrContext);
        solarSystemObject.addChildObject(sunRotationObject);

        GVRSceneObject sunMeshObject = asyncSceneObject(gvrContext, "sunmap.astc");
        sunMeshObject.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunRotationObject.addChildObject(sunMeshObject);
        sunMeshObject.setName("SUN");
        sunMeshObject.getRenderData().disableLightMap();

        GVRSceneObject mercuryRevolutionObject = new GVRSceneObject(gvrContext);
        mercuryRevolutionObject.getTransform().setPosition(14.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(mercuryRevolutionObject);

        GVRSceneObject mercuryRotationObject = new GVRSceneObject(gvrContext);
        mercuryRevolutionObject.addChildObject(mercuryRotationObject);

        GVRSceneObject mercuryMeshObject = asyncSceneObject(gvrContext, "mercurymap.jpg");
        mercuryMeshObject.getTransform().setScale(0.3f, 0.3f, 0.3f);
        mercuryRotationObject.addChildObject(mercuryMeshObject);
        mercuryMeshObject.setName("MERCURY");
        mercuryMeshObject.getRenderData().enableLightMap();

        GVRSceneObject venusRevolutionObject = new GVRSceneObject(gvrContext);
        venusRevolutionObject.getTransform().setPosition(17.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(venusRevolutionObject);

        GVRSceneObject venusRotationObject = new GVRSceneObject(gvrContext);
        venusRevolutionObject.addChildObject(venusRotationObject);

        GVRSceneObject venusMeshObject = asyncSceneObject(gvrContext, "venusmap.jpg");
        venusMeshObject.getTransform().setScale(0.8f, 0.8f, 0.8f);
        venusRotationObject.addChildObject(venusMeshObject);
        venusMeshObject.setName("VENUS");
        venusMeshObject.getRenderData().enableLightMap();

        GVRSceneObject earthRevolutionObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.getTransform().setPosition(22.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(earthRevolutionObject);

        GVRSceneObject earthRotationObject = new GVRSceneObject(gvrContext);
        earthRevolutionObject.addChildObject(earthRotationObject);

        GVRSceneObject earthMeshObject = asyncSceneObject(gvrContext, "earthmap1k.jpg");
        earthMeshObject.getTransform().setScale(1.0f, 1.0f, 1.0f);
        earthRotationObject.addChildObject(earthMeshObject);
        earthMeshObject.setName("EARTH");
        earthMeshObject.getRenderData().enableLightMap();

        GVRSceneObject moonRevolutionObject = new GVRSceneObject(gvrContext);
        moonRevolutionObject.getTransform().setPosition(4.0f, 0.0f, 0.0f);
        earthRevolutionObject.addChildObject(moonRevolutionObject);
        moonRevolutionObject.addChildObject(mMainScene.getMainCameraRig());

        GVRSceneObject marsRevolutionObject = new GVRSceneObject(gvrContext);
        marsRevolutionObject.getTransform().setPosition(30.0f, 0.0f, 0.0f);
        solarSystemObject.addChildObject(marsRevolutionObject);

        GVRSceneObject marsRotationObject = new GVRSceneObject(gvrContext);
        marsRevolutionObject.addChildObject(marsRotationObject);

        GVRSceneObject marsMeshObject = asyncSceneObject(gvrContext, "mars_1k_color.jpg");
        marsMeshObject.getTransform().setScale(0.6f, 0.6f, 0.6f);
        marsRotationObject.addChildObject(marsMeshObject);
        marsMeshObject.setName("MARS");
        marsMeshObject.getRenderData().enableLightMap();

        counterClockwise(sunRotationObject, 50f);

        counterClockwise(mercuryRevolutionObject, 150f);

        counterClockwise(venusRevolutionObject, 400f);

        counterClockwise(earthRevolutionObject, 600f);

        counterClockwise(moonRevolutionObject, 60f);

        clockwise(mMainScene.getMainCameraRig().getTransform(), 60f);

        counterClockwise(marsRevolutionObject, 1200f);

        applySceneLightMap(mMainScene, gvrContext);
    }

    @Override
    public void onStep() {
    }

    void onTap() {
        if (null != mMainScene) {
            // toggle whether stats are displayed.
            boolean statsEnabled = mMainScene.getStatsEnabled();
            mMainScene.setStatsEnabled(!statsEnabled);
        }
    }

    private List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

    private void setup(GVRAnimation animation) {
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimations.add(animation);
    }

    private void counterClockwise(GVRSceneObject object, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                object, duration, 360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    private void clockwise(GVRTransform transform, float duration) {
        setup(new GVRRotationByAxisWithPivotAnimation( //
                transform, duration, -360.0f, //
                0.0f, 1.0f, 0.0f, //
                0.0f, 0.0f, 0.0f));
    }

    public void applySceneLightMap(GVRScene scene, GVRContext gvrContext) {
        List<GVRAtlasInformation> atlasInformation;
        GVRTexture lightMapTexture;
        try {
            atlasInformation = gvrContext.loadTextureAtlasInformation(new GVRAndroidResource(gvrContext, "lightmap_info.json"));

            lightMapTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, "lightmap_texture.png"));

            for (GVRAtlasInformation gvrAtlasInformation : atlasInformation) {
                GVRSceneObject sceneObj = scene.getSceneObjectByName(gvrAtlasInformation.getName());
                if (sceneObj != null && sceneObj.getRenderData().isLightMapEnabled()) {
                    sceneObj.getRenderData().getMaterial().setShaderType(GVRMaterial.GVRShaderType.LightMap.ID);
                    sceneObj.getRenderData().getMaterial().setLightMapInfo(gvrAtlasInformation);
                    sceneObj.getRenderData().getMaterial().setLightMapTexture(lightMapTexture);
                }
            }
        } catch (IOException ioe) {
            // TODO Auto-generated catch block
            ioe.printStackTrace();
        }
    }
}
