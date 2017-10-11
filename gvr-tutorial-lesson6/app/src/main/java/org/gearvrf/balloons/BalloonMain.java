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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
;
import android.graphics.Color;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.MotionEvent;
import org.gearvrf.GVRPicker;
import org.gearvrf.IPickEvents;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.io.IOException;
import java.util.Timer;
import android.media.SoundPool;

public class BalloonMain extends GVRMain {

    public class PickHandler implements IPickEvents
    {
        public GVRSceneObject   PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onExit(GVRSceneObject sceneObj) { }
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onNoPick(GVRPicker picker)
        {
            PickedObject = null;
        }
        public void onPick(GVRPicker picker)
        {
            GVRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
        }
    }

    private GVRScene mScene = null;
    private PickHandler mPickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<GVRMaterial> mMaterials;
    private GVRMesh     mSphereMesh;
    private Random      mRandom = new Random();
    private SoundPool   mAudioEngine;
    private SoundEffect mPopSound;
    private GVRTextViewSceneObject mScoreBoard;
    private Integer     mScore = 0;
    private GVRPicker   mPicker;

    @Override
    public void onInit(GVRContext context)
    {
        /*
         * Load the balloon popping sound
         */
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        try
        {
            mPopSound = new SoundEffect(context, mAudioEngine, "pop.wav", false);
            mPopSound.setVolume(0.6f);
        }
        catch (IOException ex)
        {
            Log.e("Audio", "Cannot load pop.wav");
        }        /*
         * Set the background color
         */
        mScene = context.getMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        GVRSceneObject headTracker = new GVRSceneObject(context,
                context.createQuad(0.1f, 0.1f),
                context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the scoreboard
         */
        mScoreBoard = makeScoreboard(context, headTracker);
        /*
         * Add the environment
         */
        GVRSceneObject environment = makeEnvironment(context);
        mScene.addSceneObject(environment);
        /*
         * Make balloon prototype sphere mesh
         */
        mMaterials = makeMaterials(context);
        mSphereMesh = new GVRSphereSceneObject(context, true).getRenderData().getMesh();

        /*
         * Start the particle emitter making balloons
         */
        GVRSceneObject particleRoot = new GVRSceneObject(context);
        particleRoot.setName("ParticleSystem");
        ParticleEmitter.MakeParticle particleCreator = new ParticleEmitter.MakeParticle()
        {
            public GVRSceneObject create(GVRContext context) { return makeBalloon(context); }
        };
        mParticleSystem = new ParticleEmitter(context, mScene, particleCreator);
        mParticleSystem.MaxDistance = 10.0f;
        mParticleSystem.TotalParticles = 10;
        mParticleSystem.EmissionRate = 3;
        mParticleSystem.Velocity = new ParticleEmitter.Range<Float>(2.0f, 6.0f);
        mParticleSystem.EmitterArea = new ParticleEmitter.Range<Vector2f>(new Vector2f(-5.0f, -2.0f), new Vector2f(5.0f, 2.0f));
        particleRoot.getTransform().setRotationByAxis(-90.0f, 1, 0, 0);
        particleRoot.getTransform().setPosition(0, -3.0f, -3.0f);
        particleRoot.attachComponent(mParticleSystem);
        mScene.addSceneObject(particleRoot);
        /*
         * Respond to picking events
         */
        mPicker = new GVRPicker(context, mScene);
        mPickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
		/*
		 * start the game timer
		 */
        gameStart();
    }

    public void gameOver()
    {
        mParticleSystem.setEnable(false);
        mScoreBoard.getTransform().setPosition(0, 0, -1.0f);
        mScoreBoard.getCollider().setEnable(true);
        mScoreBoard.setTextSize(10.0f);
        mScoreBoard.setText(mScoreBoard.getTextString() + "\nTap to play again");
    }

    public void gameStart()
    {
        mScoreBoard.getTransform().setPosition(-1.2f, 1.2f, -2.2f);
        mScore = 0;
        float s = mScoreBoard.getTextSize();
        mScoreBoard.setTextSize(15.0f);
        mScoreBoard.setText("000");
        mScoreBoard.getCollider().setEnable(false);
        mParticleSystem.setEnable(true);
        Timer timer = new Timer();
        TimerTask gameOver = new TimerTask()
        {
            public void run() { gameOver(); }
        };
        long oneMinute = 60 * 1000;
        timer.schedule(gameOver, oneMinute);
    }

    GVRSceneObject makeBalloon(GVRContext context)
    {
        GVRSceneObject balloon = new GVRSceneObject(context, mSphereMesh);
        GVRRenderData rdata = balloon.getRenderData();
        GVRSphereCollider collider = new GVRSphereCollider(context);
        Random rand = new Random();
        int mtlIndex = rand.nextInt(mMaterials.size() - 1);

        balloon.setName("balloon");
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mMaterials.get(mtlIndex));
        rdata.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        collider.setRadius(0.8f);
        balloon.attachComponent(collider);
        return balloon;
    }

    GVRSceneObject makeEnvironment(GVRContext context)
    {
        GVRTexture tex = context.getAssetLoader().loadCubemapTexture(new GVRAndroidResource(context, R.raw.lycksele3));
        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        GVRSphereSceneObject environment = new GVRSphereSceneObject(context, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        if (!GVRShader.isVulkanInstance())
        {
            GVRDirectLight sunLight = new GVRDirectLight(context);
            sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            environment.attachComponent(sunLight);
        }
        return environment;
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
            GVRMaterial mtl = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Phong.ID);
            mtl.setDiffuseColor(colors[i][0], colors[i][1], colors[i][2], colors[i][3]);
            materials.add(mtl);
        }
        return materials;
    }

    /*
     * Make the scoreboard
     */
    GVRTextViewSceneObject makeScoreboard(GVRContext ctx, GVRSceneObject parent)
    {
        GVRTextViewSceneObject scoreBoard = new GVRTextViewSceneObject(ctx, 2.0f, 1.5f, "000");
        GVRRenderData rdata = scoreBoard.getRenderData();
        GVRCollider collider = new GVRMeshCollider(ctx, true);

        collider.setEnable(false);
        scoreBoard.attachComponent(collider);
        scoreBoard.setTextColor(Color.YELLOW);
        scoreBoard.setBackgroundColor(Color.argb(0, 0, 0, 0));
        scoreBoard.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderingOrder.OVERLAY);
        GVRSceneObject boardFrame = null;
        try
        {
            boardFrame = ctx.getAssetLoader().loadModel("mirror.3ds");
            GVRSceneObject.BoundingVolume bv = boardFrame.getBoundingVolume();
            GVRTransform trans = boardFrame.getTransform();
            Matrix4f mtx = new Matrix4f();
            float sf = 1.5f / bv.radius;

            trans.setScale(sf, sf, sf);
            trans.rotateByAxis(-90.0f, 0, 1, 0);
            trans.rotateByAxis(90.0f, 0, 0, 1);
            bv = boardFrame.getBoundingVolume();
            trans.setPosition(-bv.center.x, -bv.center.y, -bv.center.z + 0.1f);
            scoreBoard.addChildObject(boardFrame);
        }
        catch (IOException ex)
        {
            Log.e("Balloons", "Cannot load scoreboard frame " + ex.getMessage());
        }
        parent.addChildObject(scoreBoard);
        return scoreBoard;
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

    private void onHit(GVRSceneObject sceneObj)
    {
        Particle particle = (Particle) sceneObj.getComponent(Particle.getComponentType());
        if (particle != null)
        {
            mPopSound.play();
            mParticleSystem.stop(particle);
            mScore += Math.round(particle.Velocity);
            mScoreBoard.setText(mScore.toString());
        }
        else if (sceneObj == mScoreBoard)
        {
            gameStart();
        }
    }
}
