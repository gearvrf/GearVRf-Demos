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
import org.gearvrf.scene_objects.GVRCubeSceneObject;

public class Bowl extends BaseScalableObject {

    public Bowl(@NonNull GVRContext context, @NonNull GVRMixedReality mixedReality, @NonNull float[] poseMatrix) {
        super(context, mixedReality, setPose(poseMatrix));

        GVRCubeSceneObject bowl = new GVRCubeSceneObject(context, true);
        bowl.getTransform().setScale(0.05f, 0.05f, 0.05f);
        bowl.getTransform().setPositionY(0.05f);

        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);
        material.setDiffuseColor(1f, 1f, 1f, 1f);

        bowl.getRenderData().setMaterial(material);
        bowl.getRenderData().setAlphaBlend(true);

        bowl.attachComponent(new GVRBoxCollider(context));
        bowl.attachComponent(new GVRRigidBody(context, 1.0f));

        addChildObject(bowl);
    }

    private static float[] setPose(float[] pose) {
        pose[14] += 0.2f;
        return pose;
    }
}
