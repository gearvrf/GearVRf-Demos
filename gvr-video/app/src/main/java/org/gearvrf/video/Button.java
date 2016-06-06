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

package org.gearvrf.video;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class Button extends GVRSceneObject {

    public Button(GVRContext gvrContext, GVRMesh mesh, GVRTexture active, GVRTexture inactive) {
        super(gvrContext, mesh, inactive);
        getRenderData().getMaterial().setTexture("active_texture", active);
        getRenderData().getMaterial().setTexture("inactive_texture", inactive);
        getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.TRANSPARENT + 1);
        getRenderData().setOffset(true);
        getRenderData().setOffsetFactor(-1.0f);
        getRenderData().setOffsetUnits(-1.0f);
        GVREyePointeeHolder holder = new GVREyePointeeHolder(gvrContext);
        holder.addPointee(new GVRMeshEyePointee(gvrContext, getRenderData().getMesh()));
        attachEyePointeeHolder(holder);
    }

    public void setPosition(float x, float y, float z) {
        getTransform().setPosition(x, y, z);
    }

    public void show() {
        getRenderData().setRenderMask(
                GVRRenderData.GVRRenderMaskBit.Left | GVRRenderData.GVRRenderMaskBit.Right);
        getEyePointeeHolder().setEnable(true);
    }

    public void hide() {
        getRenderData().setRenderMask(0);
        getEyePointeeHolder().setEnable(false);
    }

    public void activate() {
        getRenderData().getMaterial().setMainTexture(
                getRenderData().getMaterial().getTexture("active_texture"));
    }

    public void inactivate() {
        getRenderData().getMaterial().setMainTexture(
                getRenderData().getMaterial().getTexture("inactive_texture"));
    }
}
