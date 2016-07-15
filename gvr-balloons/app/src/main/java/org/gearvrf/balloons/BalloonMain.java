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
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRLightBase;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRTexture;
import org.gearvrf.IPickEvents;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.joml.Vector3f;

import android.graphics.Color;
import android.media.AudioManager;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.media.SoundPool;

public class BalloonMain extends GVRMain {

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject   PickedObject = null;

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onNoPick(GVRPicker picker)
        {
            PickedObject = null;
         }

        public void onPick(GVRPicker picker)
        {
            PickedObject = picker.getPicked()[0].hitObject;
        }
    }

    private GVRContext mGVRContext = null;
    private GVRScene mScene = null;
    private PickHandler mPickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<GVRMaterial> mMaterials;
    private GVRMesh     mSphereMesh;
    private Integer     mScore = 0;
    private int         mNumParticles = 0;
    private SoundPool   mAudioEngine;
    private SoundEffect mPopSound;
    private GVRTextViewSceneObject mScoreBoard;
    private Timer       mTimer;
    private boolean     mGameOver = false;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        /*
         * Load the balloon popping sound
         */
        try
        {
            mPopSound = new SoundEffect(gvrContext, mAudioEngine, "pop.wav", false);
            mPopSound.setVolume(0.6f);
        }
        catch (IOException ex)
        {
            Log.e("Audio", "Cannot load pop.wav");
        }
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

        mScene.addSceneObject(makeBackground(mGVRContext));
        mScoreBoard = makeScoreboard(mGVRContext);
        headTracker.addChildObject(mScoreBoard);
        /*
         * Respond to picking events
         */
        mScene.getMainCameraRig().getOwnerObject().attachComponent(new GVRPicker(gvrContext, mScene));
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);

        /*
         * Make balloon prototype sphere mesh
         */
        mMaterials = makeMaterials(gvrContext);
        mSphereMesh = new GVRSphereSceneObject(gvrContext, true).getRenderData().getMesh();

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
        mParticleSystem.TotalParticles = 20;
        mParticleSystem.EmissionRate = 3;
        mParticleSystem.MinVelocity = 2;
        mParticleSystem.MaxVelocity = 6;
        mParticleSystem.setEmitterArea(new Vector3f(-5.0f, -3.0f, -2.0f), new Vector3f(5.0f, -3.01f, -5.0f));
        particleRoot.attachComponent(mParticleSystem);
        mScene.addSceneObject(particleRoot);
        mParticleSystem.start();
        mTimer = new Timer();
        TimerTask gameOver = new TimerTask()
        {
            public void run() { gameOver(); }
        };
        long oneMinute = 60 * 1000;
        mTimer.schedule(gameOver, oneMinute);
    }

    /*
     * Make an array of materials for the particles
     * so they will not all be the same.
     */
    ArrayList<GVRMaterial> makeMaterials(GVRContext ctx)
    {
        float[][] colors = new float[][] {
                { 1.0f,   0.0f,   0.0f,   0.8f },
                { 0.0f,   1.0f,   0.0f,   0.8f },
                { 0.0f,   0.0f,   1.0f,   0.8f },
                { 1.0f,   0.0f,   1.0f,   0.8f },
                { 1.0f,   1.0f,   0.0f,   0.8f },
                { 0.0f,   1.0f,   1.0f,   0.8f }
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
     * Make a large sphere enclosing the scene with a pretty countryside.
     */
    GVRSceneObject makeBackground(GVRContext ctx)
    {
        Future<GVRTexture> tex = ctx.loadFutureCubemapTexture(new GVRAndroidResource(mGVRContext, R.raw.lycksele3));
        GVRMaterial mtl = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Cubemap.ID);
        mtl.setMainTexture(tex);
        GVRSphereSceneObject environment = new GVRSphereSceneObject(ctx, 18, 36, false, mtl, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        GVRDirectLight sunLight = new GVRDirectLight(mGVRContext);
        sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
        sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
        environment.attachComponent(sunLight);
        return environment;
    }

    GVRTextViewSceneObject makeScoreboard(GVRContext ctx)
    {
        GVRTextViewSceneObject scoreBoard = new GVRTextViewSceneObject(ctx, 3, 2, "Score: 0");
        GVRRenderData rdata = scoreBoard.getRenderData();
        scoreBoard.getTransform().setPosition(-1.2f, 1.2f, -3.0f);
        scoreBoard.setTextColor(Color.YELLOW);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        return scoreBoard;
    }
    /*
     * Make balloon particle
     */
    GVRSceneObject makeBalloon(GVRContext gvrContext)
    {
        GVRSceneObject balloon = new GVRSceneObject(gvrContext, mSphereMesh);
        GVRRenderData rdata = balloon.getRenderData();
        GVRSphereCollider collider = new GVRSphereCollider(gvrContext);
        Random rand = new Random();
        int mtlIndex = rand.nextInt(mMaterials.size() - 1);

        mtlIndex = mNumParticles++;
        balloon.setName("balloon");
        rdata.setShaderTemplate(GVRPhongShader.class);
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mMaterials.get(mtlIndex));
        rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        collider.setRadius(0.8f);
        balloon.attachComponent(collider);
        return balloon;
    }

    public void gameOver()
    {
        mParticleSystem.stop();
        mScoreBoard.getTransform().setPosition(0, 0, -2);
        mScoreBoard.setTextColor(Color.RED);
        mScoreBoard.setText(mScoreBoard.getTextString() + "\nTap to play again");
        mGameOver = true;
    }

    public void gameStart()
    {
        mScoreBoard.getTransform().setPosition(-1.2f, 1.2f, -3.0f);
        mScoreBoard.setTextColor(Color.YELLOW);
        mScore = 0;
        mScoreBoard.setText("Score: 0");
        mGameOver = false;
        mParticleSystem.start();
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        if (mGameOver)
        {
            gameStart();
            return;
        }
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
        if (!mGameOver && (particle != null))
        {
            mPopSound.play();
            mParticleSystem.stop(particle);
            mScore += Math.round(particle.Velocity);
            mScoreBoard.setText("Score: " + mScore.toString());
        }
    }

}
