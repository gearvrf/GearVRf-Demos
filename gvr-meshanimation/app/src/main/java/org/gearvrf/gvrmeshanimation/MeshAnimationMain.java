package org.gearvrf.gvrmeshanimation;

import android.util.Log;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import java.io.IOException;

public class MeshAnimationMain extends GVRMain {

    private GVRActivity mActivity;
    private GVRModelSceneObject mCharacter;

    public MeshAnimationMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        final GVRScene nextMainScene = gvrContext.getNextMainScene();

        try {
            mCharacter = gvrContext.loadModel(mModelPath);
            mCharacter.getTransform().setPosition(0.0f, -10.0f, -10.0f);
            mCharacter.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);
            mCharacter.getTransform().setRotationByAxis(40.0f, 0.0f, 1.0f, 0.0f);
            mCharacter.getTransform().setScale(1.5f, 1.5f, 1.5f);

            nextMainScene.addSceneObject(mCharacter);
            final GVRAnimator animator = (GVRAnimator)mCharacter.getComponent(GVRAnimator.getComponentType());
            animator.start();
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
    }

    private static final String mModelPath = "TRex_NoGround.fbx";
    private static final String TAG = "MeshAnimationSample";
}
