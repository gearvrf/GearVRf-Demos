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

package org.gearvrf.sample.leap;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRTexture;
import org.gearvrf.leap.LeapController;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import java.util.concurrent.Future;

public class LeapMain extends GVRMain {

    private static final float SCALE = 200.0f;

    @Override
    public void onInit(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getNextMainScene();

        LeapController controller = new LeapController(gvrContext, scene);

        scene.addSceneObject(controller.getLeftHandSceneObject());
        scene.addSceneObject(controller.getRightHandSceneObject());

        Future<GVRTexture> futureTexture = gvrContext.loadFutureTexture(new
                GVRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSphereSceneObject skyBox = new GVRSphereSceneObject(gvrContext, false, material);
        skyBox.getTransform().setScale(SCALE, SCALE, SCALE);
        skyBox.getRenderData().getMaterial().setMainTexture(futureTexture);
        scene.addSceneObject(skyBox);
    }

    @Override
    public void onStep() {
    }
}
