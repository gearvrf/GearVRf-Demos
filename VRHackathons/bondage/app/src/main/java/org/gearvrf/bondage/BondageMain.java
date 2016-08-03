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

package org.gearvrf.bondage;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import org.gearvrf.GVRPicker;
import org.gearvrf.IPickEvents;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Future;

public class BondageMain extends GVRMain {

    public class GridPicker implements IPickEvents
    {
        public GVRSceneObject   PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
        {
            GVRSceneObject parent = sceneObj.getParent();
            if ((parent != null) && (parent.getParent() == mElementGrid))
            {
                parent.getTransform().setScale(1.2f, 1.2f, 1.2f);
            }
        }
        public void onExit(GVRSceneObject sceneObj)
        {
            GVRSceneObject parent = sceneObj.getParent();
            if ((parent != null) && (parent.getParent() == mElementGrid))
            {
                parent.getTransform().setScale(1.0f, 1.0f, 1.0f);
            }
        }
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
        public void onNoPick(GVRPicker picker)
        {
            PickedObject = null;
        }
        public void onPick(GVRPicker picker)
        {
            for (GVRPickedObject picked : picker.getPicked())
            {
                GVRSceneObject parent = picked.hitObject.getParent();
                if (parent.getParent() == mElementGrid)
                {
                    PickedObject = picked.hitObject;
                    return;
                }
            }
        }
    }

    private GVRScene mScene = null;
    private GridPicker mPickHandler;
    private GVRSceneObject mElementGrid = null;
    private GVRSceneObject mMolecule = null;
    private GVRSceneObject mHeadTracker;
    private ElementCursor mCursor = null;
    private BondAnimator mBondAnimator = null;
    private int mNumMatched = 0;
    private float mRotAngle = 0;
    private HashMap<String, String> mMoleculeMap = new HashMap<String, String>();

    @Override
    public void onInit(GVRContext context)
    {
        /*
         * Set the background color
         */
        mScene = context.getNextMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        mHeadTracker = new GVRSceneObject(context,
                context.createQuad(0.1f, 0.1f),
                context.loadTexture(new GVRAndroidResource(context, R.drawable.headtrackingpointer)));
        mHeadTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        mHeadTracker.getRenderData().setDepthTest(false);
        mHeadTracker.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        mScene.getMainCameraRig().addChildObject(mHeadTracker);
        /*
         * Add the environment and the molecule
         */;
        loadSounds(context);
        makeEnvironment(context);
        mMolecule = loadMolecule(context, "c2h4.obj", "C2H4");
        /*
         * Respond to picking events
         */
        mScene.getMainCameraRig().getOwnerObject().attachComponent(new GVRPicker(context, mScene));
        mPickHandler = new GridPicker();
        mScene.getEventReceiver().addListener(mPickHandler);
    }
    
    GVRSceneObject loadMolecule(GVRContext ctx, String fileName, String moleculeName)
    {
        if (mBondAnimator != null)
        {
            mScene.getEventReceiver().removeListener(mBondAnimator);
        }
        try
        {
            GVRSceneObject modelRoot = ctx.getAssetLoader().loadModel(fileName, mScene);
            GVRSceneObject.BoundingVolume bv = modelRoot.getBoundingVolume();
            GVRTransform trans = modelRoot.getTransform();
            mBondAnimator = new BondAnimator(ctx, mMoleculeMap, mGoodSound, mBadSound);

            trans.setScale(0.1f, 0.1f, 0.1f);
            trans.setPositionZ(-1.0f);
            mBondAnimator.setEnable(false);
            modelRoot.attachComponent(mBondAnimator);
            if (mElementGrid != null)
            {
                mScene.removeSceneObject(mElementGrid);
            }
            mElementGrid = makeElementGrid(ctx, modelRoot);
            mScene.addSceneObject(mElementGrid);
            mScene.getEventReceiver().addListener(mBondAnimator);
            mNumMatched = 0;
            makeMoleculeMap(fileName);
            return modelRoot;
        }
        catch (IOException ex)
        {
            Log.e("bondage", "Cannot load file " + ex.getMessage());
            return null;
        }
    }

    GVRSceneObject makeEnvironment(GVRContext context)
    {
        GVRSceneObject environment;
        try
        {
            environment = context.getAssetLoader().loadModel("playarea.obj", mScene);
            GVRDirectLight light = new GVRDirectLight(context);
            light.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            light.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            environment.attachComponent(light);
            return environment;
        }
        catch (IOException ex)
        {
            Log.e("bondage", ex.getMessage());
            GVRTexture tex = context.loadTexture(new GVRAndroidResource(context, R.drawable.gearvrf));
            environment = new GVRSceneObject(context, 4, 4, tex);
            GVRSceneObject lightObj = new GVRSceneObject(context);
            GVRDirectLight light = new GVRDirectLight(context);
            light.setAmbientIntensity(0.1f, 0.1f, 0.1f, 1.0f);
            light.setDiffuseIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            lightObj.attachComponent(light);
            lightObj.getTransform().setPosition(0, 1.0f, 1.0f);
            environment.getTransform().setPositionZ(-3.0f);
            environment.addChildObject(lightObj);
            mScene.addSceneObject(environment);
            return environment;
        }
    }

    private GVRSceneObject makeElementGrid(GVRContext ctx, GVRSceneObject srcRoot)
    {
        GVRSceneObject gridRoot = new GVRSceneObject(ctx);
        GVRTransform trans = gridRoot.getTransform();
        float sf = 0.15f;
        ElementGrid elementGrid = new ElementGrid(ctx);

        gridRoot.setName("ElementGrid");
        trans.setScale(sf, sf, sf);
        trans.setPosition(-0.8f, 0.80f, -1.0f);
        gridRoot.attachComponent(elementGrid);
        elementGrid.makeGrid(srcRoot);
        return gridRoot;
    }

    @Override
    public void onStep()
    {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                if ((mPickHandler != null) && (mPickHandler.PickedObject != null))
                {
                    onHitGrid(mPickHandler.PickedObject);
                    return;
                }
                if (mBondAnimator != null)
                {
                    GVRSceneObject target = mBondAnimator.getTarget();
                    mBondAnimator.onTouch();
                    if (mBondAnimator.WrongAnswer && (target != null))
                    {
                        attachToGrid(target);
                    }
                    return;
                }
                break;

            default:
                break;
        }
        if (mCursor != null)
        {
            mCursor.onTouchEvent(event);
        }
    }

    private void onHitGrid(GVRSceneObject sceneObj)
    {
        GVRSceneObject parent = sceneObj.getParent();
        if ((parent != null) && (parent.getParent() == mElementGrid))
        {
            if (mNumMatched == 0)
            {
                String elemName = BondAnimator.getElementName(sceneObj);
                NameMatcher matcher = new NameMatcher(elemName);
                Log.d("bondage", "Match " + elemName);
                matcher.Match = null;
                mMolecule.forAllDescendants(matcher);
                GVRSceneObject match = matcher.Match;
                if (match != null)
                {
                    String name = match.getName();
                    GVRRenderData rdata = match.getRenderData();
                    if (rdata != null)
                    {
                        rdata.setEnable(true);
                        sceneObj.setEnable(false);
                        ++mNumMatched;
                    }
                }
                return;
            }
            attachToCursor(sceneObj);
        }
    }

    private void attachToCursor(GVRSceneObject elemObj)
    {
        float sf = 0.1f;
        GVRSceneObject parent = elemObj.getParent();
        elemObj.getTransform().setScale(sf, sf, sf);

        parent.getParent().removeChildObject(elemObj);
        elemObj.getTransform().setPosition(0, 0, 0);
        mHeadTracker.addChildObject(elemObj);
        elemObj.getComponent(GVRCollider.getComponentType()).setEnable(false);
        mBondAnimator.setTarget(elemObj);
        mBondAnimator.setEnable(true);
    }

    private void attachToGrid(GVRSceneObject elemObj)
    {
        ElementGrid grid = (ElementGrid) mElementGrid.getComponent(ElementGrid.getComponentType());
        mHeadTracker.removeChildObject(elemObj);
        grid.addToGrid(elemObj);
        elemObj.getComponent(GVRCollider.getComponentType()).setEnable(true);
        mBondAnimator.setEnable(false);
        mCursor = null;
    }

    private void makeMoleculeMap(String name)
    {
        mMoleculeMap.clear();
        if (name.equals("c2h4.obj"))
        {
            mMoleculeMap.put("c1", "h1_Sphere.003 h2_Sphere.001 c2_Sphere.002");
            mMoleculeMap.put("c2", "h4_Sphere.004 h3_Sphere.005 c1_Sphere");
            mMoleculeMap.put("h1", "c1_Sphere");
            mMoleculeMap.put("h2", "c1_Sphere");
            mMoleculeMap.put("h3", "c2_Sphere.002");
            mMoleculeMap.put("h4", "c2_Sphere.002");
        }
    }

    private SoundPool   mAudioEngine;
    private SoundEffect mGoodSound;
    private SoundEffect mBadSound;

    private void loadSounds(GVRContext context)
    {
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        try
        {
            mGoodSound = new SoundEffect(context, mAudioEngine, "cashreg.wav", false);
            mGoodSound.setVolume(0.6f);
            mBadSound = new SoundEffect(context, mAudioEngine, "hammer.wav", false);
            mBadSound.setVolume(0.6f);
        }
        catch (IOException ex)
        {
            Log.e("Audio", "Cannot load pop.wav");
        }
    }
}
