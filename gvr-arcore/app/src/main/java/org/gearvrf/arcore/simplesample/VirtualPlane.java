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

import android.graphics.Color;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.arcore.simplesample.arobjects.GVRPlaneObject;

public class VirtualPlane extends GVRPlaneObject {
    private int hsvHUE = 0;

    public VirtualPlane(GVRContext gvrContext) {
        super(gvrContext);

        load3dModel(gvrContext);
    }

    private void load3dModel(final GVRContext gvrContext) {
        GVRMesh mesh = GVRMesh.createQuad(gvrContext,
                "float3 a_position", 1.0f, 1.0f);

        GVRMaterial mat = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);

        GVRSceneObject polygonObject = new GVRSceneObject(gvrContext, mesh, mat);

        hsvHUE += 35;
        float[] hsv = new float[3];
        hsv[0] = hsvHUE % 360;
        hsv[1] = 1f; hsv[2] = 1f;

        int c =  Color.HSVToColor(50, hsv);
        mat.setDiffuseColor(Color.red(c) / 255f,Color.green(c) / 255f,
                Color.blue(c) / 255f, 0.2f);

        polygonObject.getRenderData().setMaterial(mat);
        polygonObject.getRenderData().setAlphaBlend(true);
        polygonObject.getTransform().setRotationByAxis(-90, 1, 0, 0);

        addChildObject(polygonObject);
    }
}
