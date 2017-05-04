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

package org.gearvrf.gvrcockpit;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.utility.Log;

import java.io.IOException;

public class CockpitMain extends GVRMain {

    @Override
    public void onInit(GVRContext gvrContext) {

        GVRScene mainScene = gvrContext.getMainScene();
        GVRAssetLoader loader = gvrContext.getAssetLoader();

        mainScene.getMainCameraRig().getTransform().setPosition(0.0f, 6.0f, 1.0f);
        /*
         * Use the asset loader to load a 3D model of a ship and a 3D environment with stars.
         * The files are in res/raw (models and textures). Once the asset loader finishes
         * loading each model and all of its associated textures, it is automatically
         * added to the scene.
         */
        try
        {
            loader.loadModel(new GVRAndroidResource(gvrContext, R.raw.gvrf_ship_mesh), GVRImportSettings.getRecommendedSettings(), false, mainScene);
            loader.loadModel(new GVRAndroidResource(gvrContext, R.raw.gvrf_space_mesh), GVRImportSettings.getRecommendedSettings(), false, mainScene);
        }
        catch (IOException ex)
        {
            Log.e("cockpit", "Cannot load assets " + ex.getMessage());
        }
    }

}
