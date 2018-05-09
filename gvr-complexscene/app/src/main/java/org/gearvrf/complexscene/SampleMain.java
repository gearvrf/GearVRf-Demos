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

package org.gearvrf.complexscene;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;
import java.util.EnumSet;

import static org.gearvrf.GVRImportSettings.NO_LIGHTING;

public class SampleMain extends GVRMain {
    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        // set background color
        GVRScene scene = gvrContext.getMainScene();
        scene.setBackgroundColor(1, 1, 1, 1);
        scene.setFrustumCulling(false);

        float NORMAL_CURSOR_SIZE = 0.4f;
        float CURSOR_Z_POSITION = -9.0f;
        int CURSOR_RENDER_ORDER = 100000;

        GVRSceneObject cursor = new GVRSceneObject(gvrContext,
                NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, "cursor_idle.png")));
        cursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        cursor.setName("cursor");
        cursor.getRenderData()
                .setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY)
                .setDepthTest(false)
                .setRenderingOrder(CURSOR_RENDER_ORDER);
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);

        try {
            EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedSettingsWith(EnumSet.of(NO_LIGHTING));
            GVRMesh mesh = gvrContext.getAssetLoader().loadMesh(
                    new GVRAndroidResource(gvrContext, "bunny.obj"),
                    settings);

            final int OBJECTS_CNT = 8;
            for (int x=-OBJECTS_CNT; x<=OBJECTS_CNT; ++x) {
                for (int y=-OBJECTS_CNT; y<=OBJECTS_CNT; ++y) {
                    GVRSceneObject sceneObject = getColorMesh(1.0f, mesh);
                    sceneObject.getTransform().setPosition(1.0f*x, 1.0f*y, -7.5f);
                    sceneObject.getTransform().setScale(0.5f, 0.5f, 1.0f);
                    scene.addSceneObject(sceneObject);
                }

        }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh) {
        GVRMaterial material = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Color.ID);
        material.setColor(1.0f, 0.0f, 1.0f);

        GVRSceneObject meshObject = new GVRSceneObject(getGVRContext(), mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);

        return meshObject;
    }
}
