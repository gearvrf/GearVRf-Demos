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

package org.gearvrf.video.overlay;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.video.focus.FocusListener;
import org.gearvrf.video.focus.FocusableSceneObject;

public class Button extends FocusableSceneObject {

    public Button(GVRContext gvrContext, GVRMesh mesh, GVRTexture active, GVRTexture inactive) {
        super(gvrContext, mesh, inactive);
        this.getRenderData().getMaterial().setTexture("active_texture", active);
        this.getRenderData().getMaterial().setTexture("inactive_texture", inactive);
        this.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT + 1);
        this.getRenderData().setOffset(true);
        this.getRenderData().setOffsetFactor(-1.0f);
        this.getRenderData().setOffsetUnits(-1.0f);
        this.attachComponent(new GVRSphereCollider(gvrContext));

        super.setFocusListener(new FocusListener() {
            @Override
            public void gainedFocus(FocusableSceneObject object) {
                getRenderData().getMaterial().setMainTexture(
                        getRenderData().getMaterial().getTexture("active_texture"));
            }

            @Override
            public void lostFocus(FocusableSceneObject object) {
                getRenderData().getMaterial().setMainTexture(
                        getRenderData().getMaterial().getTexture("inactive_texture"));
            }
        });
    }

    public void setPosition(float x, float y, float z) {
        getTransform().setPosition(x, y, z);
    }

    public void show() {
        getRenderData().setRenderMask(
                GVRRenderData.GVRRenderMaskBit.Left | GVRRenderData.GVRRenderMaskBit.Right);
        getCollider().setEnable(true);
    }

    public void hide() {
        getRenderData().setRenderMask(0);
        getCollider().setEnable(false);
    }
}
