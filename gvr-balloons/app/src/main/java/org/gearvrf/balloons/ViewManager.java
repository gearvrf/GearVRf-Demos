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

package org.gearvrf.balloons;

import java.util.ArrayList;
import java.util.Random;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.IPickEvents;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.joml.Vector3f;

import android.util.Log;
import android.view.MotionEvent;

public class ViewManager extends GVRMain {

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject   PickedObject = null;
        
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }      
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onNoPick(GVRPicker picker) { }
        public void onPick(GVRPicker picker)
        {
            GVRPickedObject picked = picker.getPicked()[0];
            if (PickedObject != picked.hitObject)
            {
                Log.d("Pick", "Pick: " + picked.hitObject.getName());
            }
            PickedObject = picked.hitObject;
            //PickedObject.getRenderData().getMaterial().setDiffuseColor(1, 0, 1, 0.5f);
        }        
    }

    private GVRContext mGVRContext = null;
    private GVRScene mScene = null;
    private PickHandler mPickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<GVRMaterial> mMaterials;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        /*
         * Set the background color
         */
        mScene = mGVRContext.getNextMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        GVRSceneObject headTracker = new GVRSceneObject(gvrContext,
                new FutureWrapper<GVRMesh>(gvrContext.createQuad(0.1f, 0.1f)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        
        /*
         * Respond to picking events
         */
        headTracker.attachComponent(new GVRPicker(gvrContext, mScene));
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
        
        /*
         * Make balloon prototype particle
         */
        mMaterials = makeMaterials(gvrContext);
        GVRSceneObject particleProto = makeBalloon(gvrContext);
        mScene.addSceneObject(particleProto);
        
        /*
         * Start the particle emitter making balloons
         */
        GVRSceneObject particleRoot = new GVRSceneObject(gvrContext);
        particleRoot.setName("ParticleSystem");
        ParticleEmitter.MakeParticle particleCreator = new ParticleEmitter.MakeParticle()
        {
            public GVRSceneObject create(GVRContext ctx) { return makeBalloon(ctx); }
        };
        mParticleSystem = new ParticleEmitter(gvrContext, mScene, particleCreator);
        mParticleSystem.MaxDistance = 10.0f;
        mParticleSystem.TotalParticles = 4;
        mParticleSystem.EmissionRate = 1;
        mParticleSystem.setEmitterArea(new Vector3f(-5.0f, -2.0f, -2.0f), new Vector3f(5.0f, -2.01f, -5.0f));
        particleRoot.attachComponent(mParticleSystem);
        mScene.addSceneObject(particleRoot);
        mParticleSystem.start();
    }

    /*
     * Make an array of materials for the particles
     * so they will not all be the same.
     */
    ArrayList<GVRMaterial> makeMaterials(GVRContext ctx)
    {
        float[][] colors = new float[][] {
            { 1.0f,   0.0f,   0.0f,   0.4f },
            { 0.0f,   1.0f,   0.0f,   0.3f },
            { 0.0f,   0.0f,   1.0f,   0.5f },
            { 1.0f,   0.0f,   1.0f,   0.4f },
            { 1.0f,   1.0f,   0.0f,   0.6f },
            { 0.0f,   1.0f,   1.0f,   0.3f }
        };
        ArrayList<GVRMaterial> materials = new ArrayList<GVRMaterial>();
        for (int i = 0; i < 6; ++i)
        {
            GVRMaterial mtl = new GVRMaterial(ctx);
            mtl.setDiffuseColor(colors[i][0], colors[i][1], colors[i][2], colors[i][3]);
            materials.add(mtl);
        }
        return materials;
    }
    
    /*
     * Make balloon particle
     */
    GVRSceneObject makeBalloon(GVRContext gvrContext)
    {
        GVRSceneObject particleProto = new GVRSphereSceneObject(gvrContext, true);
        GVRRenderData rdata = particleProto.getRenderData();
        Random rand = new Random();
        int mtlIndex = rand.nextInt(mMaterials.size());
        
        particleProto.setName("balloon");
        rdata.setShaderTemplate(GVRPhongShader.class);
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mMaterials.get(mtlIndex));
        rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        particleProto.attachComponent(new GVRSphereCollider(gvrContext));
        return particleProto;
    }
    
    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
            if (mPickHandler.PickedObject != null)
            {
                onHit(mPickHandler.PickedObject);
            }
            break;
            
            default:
            break;
        }
    }
    
    public void onHit(GVRSceneObject sceneObj)
    {
        Particle particle = (Particle) sceneObj.getComponent(Particle.getComponentType());
        if (particle != null)
        {
            Log.d("Pick", "Pick: hit " + sceneObj.getName());
            mParticleSystem.stop(particle);
        }
    }

}
