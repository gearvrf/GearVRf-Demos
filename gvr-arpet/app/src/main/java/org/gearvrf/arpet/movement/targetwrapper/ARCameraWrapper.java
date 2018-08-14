/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.movement.targetwrapper;

import org.gearvrf.GVRTransform;
import org.gearvrf.arpet.movement.TargetObject;
import org.gearvrf.mixedreality.GVRMixedReality;

public class ARCameraWrapper implements TargetObject {

    private GVRMixedReality mMixedReality;

    public ARCameraWrapper(GVRMixedReality mMixedReality) {
        this.mMixedReality = mMixedReality;
    }

    @Override
    public float[] getPoseMatrix() {
        return mMixedReality.getCameraPoseMatrix();
    }

    @Override
    public GVRTransform getTransform() {
        return mMixedReality.getTransform();
    }
}
