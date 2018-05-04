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

package org.gearvrf.immersivepedia.dinosaur;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;
import org.gearvrf.immersivepedia.util.RenderingOrderApplication;


public class Dinosaur extends FocusableSceneObject {

    public Dinosaur(GVRContext context, GVRSceneObject dino, GVRSceneObject base,
            GVRSceneObject ground) {
        super(context, dino.getRenderData().getMesh(), dino.getRenderData().getMaterial().getMainTexture());

        if (dino != null && dino.getRenderData() != null
                && dino.getRenderData().getMaterial() != null) {
            dino.getRenderData().setRenderingOrder(RenderingOrderApplication.DINOSAUR);
            this.addChildObject(dino);
        }

        if (base != null && base.getRenderData() != null
                && base.getRenderData().getMaterial() != null) {
            base.getRenderData().setRenderingOrder(RenderingOrderApplication.DINOSAUR);
            this.addChildObject(base);
        }

        if (ground != null && ground.getRenderData() != null
                && ground.getRenderData().getMaterial() != null) {
            ground.getRenderData().setRenderingOrder(RenderingOrderApplication.DINOSAUR);
            this.addChildObject(ground);
        }
    }
}
