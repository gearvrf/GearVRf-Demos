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
    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private static final float CLICKED_COLOR_R = 0.5f;
    private static final float CLICKED_COLOR_G = 0.5f;
    private static final float CLICKED_COLOR_B = 1.0f;
    private static final float CLICKED_COLOR_A = 1.0f;

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

    public void onPickEnter() {
        if (m3dModel == null)
            return;

        m3dModel.getRenderData().getMaterial().setDiffuseColor(PICKED_COLOR_R,
                PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
    }

    public void onPickExit() {
        if (m3dModel == null)
            return;

        m3dModel.getRenderData().getMaterial().setDiffuseColor(UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
    }

    public void onTouchStart() {
        if (m3dModel == null)
            return;

        m3dModel.getRenderData().getMaterial().setDiffuseColor(CLICKED_COLOR_R,
                CLICKED_COLOR_G, CLICKED_COLOR_B, CLICKED_COLOR_A);
    }

    public void onTouchEnd() {
        if (m3dModel == null)
            return;

        m3dModel.getRenderData().getMaterial().setDiffuseColor(PICKED_COLOR_R,
                PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
    }
}