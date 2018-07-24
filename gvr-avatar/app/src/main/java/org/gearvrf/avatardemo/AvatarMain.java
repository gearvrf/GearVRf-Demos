package org.gearvrf.avatardemo;

import java.io.IOException;
import java.util.EnumSet;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.GVRSceneObject;

import android.util.Log;

public class AvatarMain extends GVRMain
{
    //private final String mModelPath = "TRex_NoGround.fbx";
    private final String mModelPath = "Mike_BellyDance.dae";
    private static final String TAG = "AVATAR";

    private GVRContext mGVRContext;
    private GVRSceneObject mCharacter;
    private GVRAnimator mAnimation = null;
    private GVRActivity mActivity;

    public AvatarMain(GVRActivity activity) {
        mActivity = activity;
    }

    public void centerModel(GVRSceneObject model, GVRTransform camTrans)
    {
        GVRSceneObject.BoundingVolume bv = model.getBoundingVolume();
        float x = camTrans.getPositionX();
        float y = camTrans.getPositionY();
        float z = camTrans.getPositionZ();
        float sf = 1 / bv.radius;
        model.getTransform().setScale(sf, sf, sf);
        bv = model.getBoundingVolume();
        model.getTransform().setPosition(x - bv.center.x, y - bv.center.y, z - bv.center.z - bv.radius);
    }

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        GVRScene mainScene = gvrContext.getMainScene();

        try
        {
            EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedMorphSettings();
            mCharacter = gvrContext.getAssetLoader().loadModel(mModelPath, settings, false, mainScene);
            centerModel(mCharacter, mainScene.getMainCameraRig().getTransform());

            mAnimation = (GVRAnimator) mCharacter.getComponent(GVRAnimator.getComponentType());
            mAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
            mAnimation.setRepeatCount(-1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
        gvrContext.getInputManager().selectController();
        mAnimation.start();
    }

    @Override
    public void onStep() {
    }
}
