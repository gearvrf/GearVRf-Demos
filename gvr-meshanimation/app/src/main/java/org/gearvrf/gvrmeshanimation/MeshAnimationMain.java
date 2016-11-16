package org.gearvrf.gvrmeshanimation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;

import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;

import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import org.gearvrf.utility.Log;

public class MeshAnimationMain extends GVRMain {
    private static final String TAG = "MeshAnimationSample";
    private static final String TREX_FILENAME = "TRex_NoGround.fbx";
    private static final String HAND_FILENAME = "r_hand_skin.fbx";
    private static final String BOAT_FILENAME = "RowBoatFBX/RowBoatAnimated.fbx";
    private static final String ASTRO_BOY_FILENAME = "astro_boy.dae";
    private GVRActivity mActivity;

    private GVRAnimationEngine mAnimationEngine;

    private List<AnimationObject> animationObjects;
    private int currentIndex;
    private GVRScene mainScene;
    private boolean onInitComplete = false;

    private static final class AnimationObject {
        private final GVRModelSceneObject object;
        private final GVRAnimation animation;

        AnimationObject(GVRModelSceneObject object, GVRAnimation animation) {
            this.object = object;
            this.animation = animation;
        }
    }

    public MeshAnimationMain(GVRActivity activity) {
        mActivity = activity;
        animationObjects = new ArrayList<AnimationObject>();
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mAnimationEngine = gvrContext.getAnimationEngine();
        mainScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                //set the first index
                setIndex(currentIndex);
                onInitComplete = true;
            }
        });

        try {
            //Add TRex
            GVRModelSceneObject tRex = gvrContext.getAssetLoader().loadModel(TREX_FILENAME);
            tRex.getTransform().setPosition(0.0f, -10.0f, -10.0f);
            tRex.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);
            tRex.getTransform().setRotationByAxis(40.0f, 0.0f, 1.0f, 0.0f);
            tRex.getTransform().setScale(1.5f, 1.5f, 1.5f);
            addAnimationObject(tRex, tRex.getAnimations().get(0));

            //Add Hand
            GVRModelSceneObject hand = gvrContext.getAssetLoader().loadModel(HAND_FILENAME);
            GVRAnimation animation = new CustomAnimation(gvrContext, hand, 1.5f);
            addAnimationObject(hand, animation);

            //Add Boat
            GVRModelSceneObject boat = gvrContext.loadModel(BOAT_FILENAME);
            boat.getTransform().setScale(0.5f, 0.5f, 0.5f);
            boat.getTransform().setRotationByAxis(20, 0, 1, 0);
            boat.getTransform().setPosition(0, -20.0f, -80.0f);
            addAnimationObject(boat, boat.getAnimations().get(0));

            //Add Astro Boy
            GVRModelSceneObject astroBoyModel = gvrContext.getAssetLoader().loadModel
                    (ASTRO_BOY_FILENAME);
            astroBoyModel.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
            astroBoyModel.getTransform().setScale(3, 3, 3);
            astroBoyModel.getTransform().setPosition(0.0f, -0.4f, -1.0f);
            addAnimationObject(astroBoyModel, astroBoyModel.getAnimations().get(0));

            //for the boat anim
            mainScene.setFrustumCulling(false);
        } catch (IOException e) {
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    @Override
    public void onStep() {

    }

    private void setIndex(int index) {
        AnimationObject animationObject = animationObjects.get(index);
        mainScene.addSceneObject(animationObject.object);
        animationObject.animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        mAnimationEngine.start(animationObject.animation);
    }

    private void removeIndex(int index) {
        AnimationObject animationObject = animationObjects.get(index);
        animationObject.animation.setRepeatMode(GVRRepeatMode.ONCE).setRepeatCount(0);
        mAnimationEngine.stop(animationObject.animation);
        mainScene.removeSceneObject(animationObject.object);
    }

    private void addAnimationObject(GVRModelSceneObject modelSceneObject, GVRAnimation animation) {
        ArrayList<GVRRenderData> rdata = modelSceneObject.getAllComponents(GVRRenderData
                .getComponentType());
        for (GVRRenderData r : rdata) {
            if (r != null) {
                r.disableLight();
            }
        }
        animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        animationObjects.add(new AnimationObject(modelSceneObject, animation));
    }

    void onTap() {
        if (!onInitComplete) {
            return;
        }

        removeIndex(currentIndex);
        currentIndex++;
        currentIndex = currentIndex % animationObjects.size();
        setIndex(currentIndex);
    }
}

