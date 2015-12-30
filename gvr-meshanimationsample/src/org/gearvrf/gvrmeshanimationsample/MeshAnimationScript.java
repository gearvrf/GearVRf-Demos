package org.gearvrf.gvrmeshanimationsample;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import android.util.Log;

public class MeshAnimationScript extends GVRScript {

    private GVRContext mGVRContext;
    private GVRModelSceneObject mCharacter;

    private final String mModelPath = "TRex_NoGround.fbx";

    private GVRActivity mActivity;

    private static final String TAG = "MeshAnimationSample";

    private GVRAnimationEngine mAnimationEngine;
    GVRAnimation mAssimpAnimation = null;
    

    public MeshAnimationScript(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();

        GVRScene mainScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                mAssimpAnimation.start(mAnimationEngine);
            }
        });

        try {
            mCharacter = gvrContext.loadModel(mModelPath);
            mCharacter.getTransform().setPosition(0.0f, -10.0f, -10.0f);
            mCharacter.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);
            mCharacter.getTransform().setRotationByAxis(40.0f, 0.0f, 1.0f, 0.0f);
            mCharacter.getTransform().setScale(1.5f, 1.5f, 1.5f);

            mainScene.addSceneObject(mCharacter);

            mAssimpAnimation = mCharacter.getAnimations().get(0);
            mAssimpAnimation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    @Override
    public void onStep() {
    }
}
