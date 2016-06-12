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

package org.gearvrf.polyline;


import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

public class SampleScript extends GVRMain {

    private GVRContext mGVRContext = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getNextMainScene();


		float[] vertices1 = {1,1,1, 2,2,2, 3,3,3, 4,4,4, 5,5,5, 6,6,6, 7,7,7, 8,8,8, 9,9,9 };
		GVRMesh mesh = new GVRMesh(gvrContext);
		GVRRenderData rd = new GVRRenderData(gvrContext);
		GVRMaterial material = new GVRMaterial(gvrContext);

		mesh.setVertices(vertices1);
		material.setColor(Color.WHITE);
		rd.setMesh(mesh);
		rd.setDrawMode(GLES20.GL_LINE_STRIP);
		material.setFloat("line_width", 1.0f);
		rd.setMaterial(material);
		rd.setShaderTemplate(GVRPhongShader.class);
		GVRSceneObject obj = new GVRSceneObject(gvrContext);
		obj.getTransform().setPosition(-2.0f, -2.0f, -8.0f);
		obj.attachRenderData(rd);
		scene.addSceneObject(obj);
     }

    @Override
    public void onStep() {
    }

}
