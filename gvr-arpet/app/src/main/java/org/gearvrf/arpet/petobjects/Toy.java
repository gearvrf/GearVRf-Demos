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

package org.gearvrf.arpet.petobjects;

import android.support.annotation.NonNull;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.arpet.gesture.scale.BaseScalableObject;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

public class Toy extends BaseScalableObject {

    public Toy(@NonNull GVRContext context, @NonNull GVRMixedReality mixedReality, @NonNull float[] poseMatrix) {
        super(context, mixedReality, setPose(poseMatrix));

        GVRSphereSceneObject toy = new GVRSphereSceneObject(context, true);
        toy.getTransform().setScale(0.03f, 0.03f, 0.03f);
        toy.getTransform().setPositionY(0.05f);

        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);
        material.setDiffuseColor(0f, 1f, 0f, 1f);

        toy.getRenderData().setMaterial(material);
        toy.getRenderData().setAlphaBlend(true);

        toy.attachComponent(new GVRBoxCollider(context));
        toy.attachComponent(new GVRRigidBody(context, 1.0f));

        addChildObject(toy);
    }

    private static float[] setPose(float[] pose) {
        pose[12] -= 0.2f;
        return pose;
    }
}
