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

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.joml.Quaternionf;

import android.util.Log;
import android.view.MotionEvent;

public class MultiLightScript extends GVRScript {

    private static final float LIGHT_Z = 100.0f;
    private static final float LIGHT_ROTATE_RADIUS = 100.0f;
    private GVRContext mGVRContext;
    private GVRSceneObject rotateObject;
    private GVRSceneObject backdrop;
    private GVRScene mScene;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mScene = mGVRContext.getNextMainScene();
        float zdist = 2;

        GVRSceneObject root = new GVRSceneObject(gvrContext);
        GVRSceneObject character = createCharacter(gvrContext);
        GVRSceneObject light1 = createLight(gvrContext, 1, 0, 0, 0.8f);
        GVRSceneObject light2 = createLight(gvrContext, 0, 1, 0, -0.8f);
        
        backdrop = createBackdrop(gvrContext);
        root.setName("root");
        root.getTransform().setPosition(0, 0, -zdist);
        mScene.addSceneObject(root);
        root.addChildObject(backdrop);
        root.addChildObject(light1);
        root.addChildObject(light2);
        root.addChildObject(character);
        rotateObject = light1;
    }

    private double theta = 0;

    @Override
    public void onStep() {
        FPSCounter.tick();
        theta += 0.005;
        if (theta >= Math.PI / 4)
            theta = -Math.PI / 4;
        if (rotateObject != null) {
            Quaternionf q = new Quaternionf();
            q.rotateAxis((float) theta, 0.0f, 1.0f, 0.0f);
            GVRTransform trans = rotateObject.getTransform();
            trans.setRotation(q.w, q.x, q.y, q.z);
        }
    }

    private boolean lightEnabled = true;

    public void onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            GVRRenderData rdata = backdrop.getRenderData();
            if (lightEnabled)
                rdata.disableLight();
            else
                rdata.enableLight();
            lightEnabled = !lightEnabled;
        }
    }
    
    /*
     * Load in a model of a little guy
     */
    private GVRSceneObject createCharacter(GVRContext context)
    {
         try
         {
            GVRModelSceneObject model = context.loadModel("astro_boy.dae");
            model.getTransform().setScale(10, 10, 10);
            model.getTransform().setPositionY(-1);
            return model;
         }
         catch (IOException e)
         {
            Log.e("multilight", "Failed to load a model: %s", e);
            return null;
         }
    }
    
    /*
     * Creates a  spot light in front of the character
     * pointing straight at it.
     */
    private GVRSceneObject createLight(GVRContext context, float r, float g, float b, float y)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRSpotLight light = new GVRSpotLight(context);
        Quaternionf q = new Quaternionf();
        
        lightNode.attachLight(light);         
        lightNode.getTransform().setPosition(0, y, 3);
        light.setAmbientIntensity(0.3f * r, 0.3f * g, 0.3f * b, 1);
        light.setDiffuseIntensity(r, g, b, 1);
        light.setSpecularIntensity(r, g, b, 1);
        q.rotationY((float) Math.PI);
        light.setDefaultOrientation(q);
        light.setInnerConeAngle(8);
        light.setOuterConeAngle(12);
        lightNode.setName("RedLight");
        return lightNode;
    }
    
    /*
     * Create a backdrop with the GearVRF logo and enable
     * multiple lighting support by choosing the GVRPhongShader template.
     * The multiple light shader uses the name "diffuseTexture" instead
     * of the name "main_texture".
     */
    private GVRSceneObject createBackdrop(GVRContext context)
    {
        GVRTexture tex = context.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.gearvrflogo));
        GVRSceneObject backdrop = new GVRSceneObject(context, 10.0f, 4.0f, tex);
        GVRRenderData rdata = backdrop.getRenderData();
        GVRMaterial material = new GVRMaterial(context);
        
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
        backdrop.setName("Backdrop");
        backdrop.getTransform().setPositionZ(-2.0f);
        rdata.setMaterial(material);
    	rdata.setShaderTemplate(GVRPhongShader.class);
    	return backdrop;
    }

}
