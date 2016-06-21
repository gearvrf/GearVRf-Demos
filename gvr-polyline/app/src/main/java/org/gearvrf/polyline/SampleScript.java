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
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

public class SampleScript extends GVRMain {

    private GVRContext mGVRContext = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getNextMainScene();
        scene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 0, 1.0f);
        scene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 0, 1.0f);
        float Z = -4;
        float[] L = { -2.5f, 1, Z, -2.5f, -1, Z, -1.5f, -1, Z };
        float[] I = { -1, 1, Z, -1, -1, Z };
        float[] N = { 0, -1, Z, 0, 1, Z, 1, -1, Z, 1, 1, Z };
        float[] E = { 2.5f, 1, Z, 1.5f, 1, Z, 1.5f, -1, Z,
                      2.5f, -1, Z, 2.5f, 0, Z, 1.5f, 0, Z,
                      1.5f, 1, Z, 1.5f, -1, Z};
    	GVRMaterial redMaterial = new GVRMaterial(gvrContext);
        GVRMaterial blueMaterial = new GVRMaterial(gvrContext);
        GVRMesh mesh = new GVRMesh(gvrContext);
        GVRRenderData rd = new GVRRenderData(gvrContext);

        redMaterial.setDiffuseColor(1, 0, 0, 1);
        redMaterial.setLineWidth(4.0f);
        blueMaterial.setDiffuseColor(0, 0, 1, 0.5f);
        blueMaterial.setLineWidth(8.0f);
        
        GVRSceneObject Lobj = new GVRSceneObject(gvrContext);
        mesh.setVertices(L);
    	rd.setMesh(mesh);
    	rd.setDrawMode(GLES20.GL_LINE_STRIP);
    	rd.setMaterial(redMaterial);
    	rd.setShaderTemplate(GVRPhongShader.class);
    	Lobj.attachRenderData(rd);
    	scene.addSceneObject(Lobj);

        GVRSceneObject Iobj = new GVRSceneObject(gvrContext);
    	mesh = new GVRMesh(gvrContext);
    	rd = new GVRRenderData(gvrContext);
    	mesh.setVertices(I);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(redMaterial);
        rd.setShaderTemplate(GVRPhongShader.class);
        Iobj.attachRenderData(rd);
        scene.addSceneObject(Iobj);

        GVRSceneObject Nobj = new GVRSceneObject(gvrContext);
        mesh = new GVRMesh(gvrContext);
        mesh.setVertices(N);
        rd = new GVRRenderData(gvrContext);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINE_STRIP);
        rd.setMaterial(redMaterial);
        rd.setShaderTemplate(GVRPhongShader.class);
        Nobj.attachRenderData(rd);
        scene.addSceneObject(Nobj);
        
        GVRSceneObject Eobj = new GVRSceneObject(gvrContext);
        mesh = new GVRMesh(gvrContext);
        mesh.setVertices(E);
        rd = new GVRRenderData(gvrContext);
        rd.setMesh(mesh);
        rd.setDrawMode(GLES20.GL_LINES);
        rd.setMaterial(blueMaterial);
        rd.setAlphaBlend(true);
        rd.setShaderTemplate(GVRPhongShader.class);
        Eobj.attachRenderData(rd);
        scene.addSceneObject(Eobj);
        
        GVRSceneObject sphere = new GVRSphereSceneObject(gvrContext);
        rd = sphere.getRenderData();
        rd.setAlphaBlend(true);
        rd.setShaderTemplate(GVRPhongShader.class);
        rd.setMaterial(blueMaterial);
        sphere.getTransform().setPositionZ(Z);
        scene.addSceneObject(sphere);
     }

    @Override
    public void onStep() {
    }

}
