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

package org.gearvrf.arcore.simplesample;

import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;

public class VirtualObject extends GVRSceneObject {
    private static final float[] UNPICKED_COLOR = {0.7f, 0.7f, 0.7f, 1.0f};
    private static final float[] PICKED_COLOR = {1.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] CLICKED_COLOR = {0.5f, 0.5f, 1.0f, 1.0f};
    private float[] current_color = UNPICKED_COLOR;

    private GVRSceneObject m3dModel;

    public VirtualObject(GVRContext gvrContext) {
        super(gvrContext);

        try {
            load3dModel(gvrContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load3dModel(final GVRContext gvrContext) throws IOException {
        final GVRSceneObject sceneObject = gvrContext.getAssetLoader().loadModel("objects/andy.obj");
        addChildObject(sceneObject);

        sceneObject.forAllDescendants(new GVRSceneObject.SceneVisitor() {
            @Override
            public boolean visit(GVRSceneObject childObject) {
                GVRRenderData renderData
                        = (GVRRenderData) childObject.getComponent(GVRRenderData.getComponentType());
                if (renderData == null || renderData.getMesh() == null)
                    return true;

                final GVRCollider collider = new GVRMeshCollider(gvrContext,
                        renderData.getMesh());

                VirtualObject.this.attachComponent(collider);

                m3dModel = childObject;

                return false;
            }
        });
    }


    public void reactToLightEnvironment(float lightEstimate) {
        m3dModel.getRenderData().getMaterial().setDiffuseColor(
                current_color[0] * lightEstimate, current_color[1] * lightEstimate,
                current_color[2] * lightEstimate, current_color[3]);
    }


    public void onPickEnter() {
        if (m3dModel == null)
            return;

        current_color = PICKED_COLOR;
    }

    public void onPickExit() {
        if (m3dModel == null)
            return;

        current_color = UNPICKED_COLOR;
    }

    public void onTouchStart() {
        if (m3dModel == null)
            return;

        current_color = CLICKED_COLOR;
    }

    public void onTouchEnd() {
        if (m3dModel == null)
            return;

        current_color = PICKED_COLOR;
    }
}