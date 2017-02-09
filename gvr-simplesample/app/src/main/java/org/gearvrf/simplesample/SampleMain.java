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

package org.gearvrf.simplesample;

import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
//import org.gearvrf.GVRScript;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;

public class SampleMain extends GVRMain {

    private GVRContext mGVRContext;
/*
    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }
*/
private GVRSceneObject asyncSceneObject(GVRContext context,
                                        String textureName) throws IOException {
    return new GVRSceneObject(context, //
            new GVRAndroidResource(context, "sphere.obj"), //
            new GVRAndroidResource(context, textureName));
}
    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        GVRScene scene = gvrContext.getMainScene();

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera()
                .setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.WHITE);

       // load texture
  /*    GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));

        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 1.0f, 1.0f,
                texture);

        sceneObject.getTransform().setPosition(0.0f, 0.0f, -1.0f);

        scene.addSceneObject(sceneObject);
*/
 /*           GVRSceneObject benchModel = gvrContext.getAssetLoader().loadModel("cube_diffuse_pointlight.fbx", scene);

            benchModel.getTransform().setScale(8.66f, 8.66f, 8.66f);
            benchModel.getTransform().setPosition(0.0f, -0.1f, -0.2f);
           benchModel.getTransform().setRotationByAxis(180.0f, 1.0f, 0.0f, 0.0f);
        scene.addSceneObject(benchModel);
*/
      /*     GVRTexture tex = gvrContext.getAssetLoader().loadCubemapTexture(new GVRAndroidResource(gvrContext, R.raw.lycksele3));
        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        GVRSphereSceneObject environment = new GVRSphereSceneObject(gvrContext, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

       // environment.setName("environment");
        scene.addSceneObject(environment);
*/
      GVRSceneObject sunMeshObject = asyncSceneObject(gvrContext, "sunmap.astc");
       // sunMeshObject.getTransform().setScale(10.0f, 10.0f, 10.0f);
        sunMeshObject.getTransform().setPosition(0,0,-3.0f);
      scene.addSceneObject(sunMeshObject);
    }

    @Override
    public void onStep() {
    }

}
