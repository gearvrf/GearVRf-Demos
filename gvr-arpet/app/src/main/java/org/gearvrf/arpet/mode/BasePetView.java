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

package org.gearvrf.arpet.mode;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.arpet.PetContext;

public abstract class BasePetView extends GVRSceneObject implements IPetView {
    protected final PetContext mPetContext;

    public BasePetView(PetContext petContext) {
        super(petContext.getGVRContext());
        mPetContext = petContext;
    }

    @Override
    public void show(GVRScene mainScene) {
        onShow(mainScene);
    }

    @Override
    public void hide(GVRScene mainScene) {
        onHide(mainScene);
    }

    protected abstract void onShow(GVRScene mainScene);

    protected abstract void onHide(GVRScene mainScene);
}
