package org.gearvrf.gvrmeshanimation;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.GVRSceneObject;

import android.util.Log;

public class MeshAnimationMain extends GVRMain {

    private GVRContext mGVRContext;
    private GVRSceneObject mCharacter;

    private final String mModelPath = "TRex_NoGround.fbx";

    private GVRActivity mActivity;

    private static final String TAG = "MeshAnimationSample";

    private GVRAnimationEngine mAnimationEngine;
    GVRAnimator mAssimpAnimation = null;
    

    public MeshAnimationMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mAnimationEngine = gvrContext.getAnimationEngine();

        GVRScene mainScene = gvrContext.getMainScene();


        try {
            mCharacter = gvrContext.getAssetLoader().loadModel(mModelPath, mainScene);
            mCharacter.getTransform().setPosition(0.0f, -10.0f, -10.0f);
            mCharacter.getTransform().setRotationByAxis(40.0f, 1.0f, 0.0f, 0.0f);
            mCharacter.getTransform().setScale(1.5f, 1.5f, 1.5f);

            mAssimpAnimation = (GVRAnimator) mCharacter.getComponent(GVRAnimator.getComponentType());
            mAssimpAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
            mAssimpAnimation.setRepeatCount(-1);
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
        mAssimpAnimation.start();
    }

    @Override
    public void onStep() {
    }
}
