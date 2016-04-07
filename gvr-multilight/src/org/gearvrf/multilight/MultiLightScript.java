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

package org.gearvrf.multilight;

import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRLightTemplate;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongLight;
import org.gearvrf.GVRPhongPointLight;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPhongSpotLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import android.util.Log;
import android.view.MotionEvent;

public class MultiLightScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 3.0f;
    private GVRContext mGVRContext;
    private static final float LIGHT_Z = 100.0f;
    private static final float LIGHT_ROTATE_RADIUS = 100.0f;

    GVRSceneObject rotateObject;
    GVRSceneObject litObject;
    GVRScene mScene;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mScene = mGVRContext.getNextMainScene();

        float zdist = CUBE_WIDTH * 0.25f;

        GVRMesh mesh = gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH);
        GVRTexture tex = gvrContext.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.front));
        GVRSceneObject root = new GVRSceneObject(gvrContext);
        GVRSceneObject mFrontFace = new GVRSceneObject(gvrContext, mesh, tex);
        
        root.setName("root");
        root.getTransform().setPosition(0, 0, -zdist);
        //root.getTransform().setRotationByAxis(45.0f, 1.0f, 0.0f, 0.0f);
        mScene.addSceneObject(root);
        mFrontFace.setName("front");
        mScene.addSceneObject(mFrontFace);
        mFrontFace.getTransform().setPosition(0, 0, -zdist);
        
        // lit object
        litObject = new GVRSphereSceneObject(gvrContext);
        GVRRenderData rdata = litObject.getRenderData();
        GVRTransform trans = litObject.getTransform();
        litObject.setName("litcube");
        trans.setScale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        trans.setRotationByAxis(45.0f, 1.0f, 0.0f, 0.0f);
        rotateObject = setupShader(mScene, root, litObject.getRenderData());
        root.addChildObject(rotateObject);
        root.addChildObject(litObject);

        for (GVRSceneObject so : mScene.getWholeSceneObjects()) {
            Log.v("", "scene object name : " + so.getName());
        }
    }

    private double theta = 0;

    @Override
    public void onStep() {
        FPSCounter.tick();
        theta += 0.01;
        if (theta >= Math.PI / 2)
            theta = 0;
        double sine = Math.cos(theta);
        double cosine = Math.sin(theta);
        if (rotateObject != null) {
            Quaternionf q = new Quaternionf();
            q.rotateAxis((float) theta, 0.0f, 1.0f, 0.0f);
            GVRTransform trans = rotateObject.getTransform();
            trans.setRotation(q.w, q.x, q.y, q.z);
            trans.setPosition((float) sine * 5, 0, (float) cosine * 5);
        }
    }

    private boolean lightEnabled = true;

    public void onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            GVRRenderData rdata = litObject.getRenderData();
            if (lightEnabled)
                rdata.disableLight();
            else
                rdata.enableLight();
            lightEnabled = !lightEnabled;
        }
    }
    
    private GVRMaterial createCustomMaterial(GVRContext context)
    {
        Future<GVRTexture> texture = context.loadFutureTexture(new GVRAndroidResource(context, R.drawable.earthmap1k));
        GVRMaterial litMaterial = new GVRMaterial(context);
        litMaterial.setVec4("diffuse_color", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        litMaterial.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        litMaterial.setFloat("specular_exponent", 10.0f);
        litMaterial.setTexture("diffuseTexture", texture);
    	return litMaterial;
    }
    
    private GVRSceneObject createSpinningLight(GVRContext context)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRPhongSpotLight light = new GVRPhongSpotLight(context);
        Quaternionf q = new Quaternionf();
        
        lightNode.attachLight(light);         
        lightNode.getTransform().setPosition(0, 0, 5.0f);
        light.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1.0f);
        light.setDiffuseIntensity(1.0f, 0.3f, 0.3f, 1.0f);
        q.rotationY((float) Math.PI);
        light.setDefaultOrientation(q);
        light.setInnerConeAngle(10.0f);
        light.setOuterConeAngle(20.0f);
        lightNode.setName("RedLight");
        return lightNode;
    }
    
    private GVRSceneObject createSunLight(GVRContext context)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRPhongLight light = new GVRPhongLight(context);
        Quaternionf q = new Quaternionf();
        
        lightNode.attachLight(light);         
        lightNode.getTransform().setPosition(0, 5.0f, 0);
        light.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1.0f);
        light.setDiffuseIntensity(0.3f, 0.3f, 1.0f, 1.0f);
        q.rotationX((float) Math.toRadians(90.0f));
        lightNode.getTransform().setRotation(q.w, q.x, q.y, q.z);
        lightNode.setName("BlueLight");
        return lightNode;
    }
    
    private GVRSceneObject setupShader(GVRScene scene, GVRSceneObject root, GVRRenderData rdata)
    {
        GVRContext context = scene.getGVRContext();
    	GVRMaterial litMaterial = createCustomMaterial(context);
        GVRSceneObject lightNode1 = createSpinningLight(context);
        GVRSceneObject lightNode2 = createSunLight(context);
        rdata.setMaterial(litMaterial);
        rdata.enableLight();
    	rdata.setShaderTemplate(GVRPhongShader.class);
    	root.addChildObject(lightNode2);
    	return lightNode1;
    }

}
