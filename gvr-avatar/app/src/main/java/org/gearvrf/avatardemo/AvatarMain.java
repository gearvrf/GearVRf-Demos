package org.gearvrf.avatardemo;

import java.io.IOException;
import java.io.InputStream;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRRepeatMode;


import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

public class AvatarMain extends GVRMain
{
    private final String mModelPath = "YBot/ybot.fbx";
    private final String mModelPathOne = "Eva/Eva.dae";
    private final String mBoneMapPath = "animation/mixamo/pet_map.txt";
    private final String[] mAnimationPaths =  {
            "Eva/bvhExport_GRAB_BONE.bvh",
             "Eva/bvhExport_RUN.bvh",
             "Eva/bvhExport_WALK"

    };
    private static final String TAG = "AVATAR";
    private GVRContext      mContext;
    private GVRScene        mScene;
    private GVRAvatar       mAvatar;
    private GVRAvatar       mAvatarOne;
    private GVRActivity     mActivity;
    private int             mNumAnimsLoaded = 0;
    private String          mBoneMap;
    private GVRSceneObject model = null;
    GVRTransform t;

    public AvatarMain(GVRActivity activity) {
        mActivity = activity;
    }

    private GVRAvatar.IAvatarEvents mAvatarListenerone = new GVRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final GVRSceneObject avatarRoot, String filePath, String errors)
        {
            if (avatarRoot.getParent() == null)
            {
                mContext.runOnGlThread(new Runnable()
                {
                    public void run()
                    {
                        centerModel(avatarRoot, t);
                        avatarRoot.getTransform().setPosition(0,-0.01f,-0.15f);
                       // avatarRoot.getTransform().setRotationByAxis(-180,0,1,0);
                        avatarRoot.getTransform().setScale(0.0004f,0.0004f,0.0004f);
                        mScene.addSceneObject(avatarRoot);
                    }
                });
            }
            loadNextAnimation(mAvatarOne, mBoneMap);

        }

        @Override
        public void onAnimationLoaded(GVRAnimator animation, String filePath, String errors)
        {
            animation.setRepeatMode(GVRRepeatMode.ONCE);
            animation.setSpeed(1f);
            ++mNumAnimsLoaded;
            if (!mAvatarOne.isRunning())
            {
                mAvatarOne.startAll(GVRRepeatMode.REPEATED);
            }
            else
            {
                mAvatarOne.start(animation.getName());
            }
            if (mNumAnimsLoaded < mAnimationPaths.length)
            {
                loadNextAnimation(mAvatarOne, mBoneMap);
            }
        }

        public void onModelLoaded(final GVRSceneObject avatarRoot, String filePath, String errors) {

        }

        public void onAnimationFinished(GVRAnimator animator, GVRAnimation animation) { }

        public void onAnimationStarted(GVRAnimator animator) { }
    };

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        GVRCameraRig rig = mScene.getMainCameraRig();
        GVRDirectLight topLight = new GVRDirectLight(gvrContext);
        topLight.setAmbientIntensity(0.5f,0.5f,0.5f,1f);
        GVRSceneObject topLightObj = new GVRSceneObject(gvrContext);

        t = mScene.getMainCameraRig().getTransform();
        topLightObj.attachComponent(topLight);
        topLightObj.getTransform().rotateByAxis(-45, 1, 0, 0);
       // mScene.addSceneObject(topLightObj);
        rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
        rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
        mAvatarOne = new GVRAvatar(gvrContext, "pet");
        mAvatarOne.getEventReceiver().addListener(mAvatarListenerone);
        mBoneMap = readFile(mBoneMapPath);

        try
        {
            mAvatarOne.loadModel(new GVRAndroidResource(gvrContext, mModelPathOne));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
        }
        gvrContext.getInputManager().selectController();

        try
        {
            model = mContext.getAssetLoader().loadModel("environment/environment_test_2.fbx");
        }
        catch (IOException ex)
        {
        }
        centerModel(model, t);
        model.getTransform().setPositionZ(-1.25f);
        mScene.addSceneObject(model);


    }
    private void loadNextAnimation(GVRAvatar avatar, String bonemap)
    {
        try
        {
            GVRAndroidResource res = new GVRAndroidResource(mContext, mAnimationPaths[mNumAnimsLoaded]);
            avatar.loadAnimation(res, bonemap);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "Animation could not be loaded from " + mAnimationPaths[mNumAnimsLoaded]);
        }
    }
    @Override
    public void onStep() {

    }


    private String readFile(String filePath)
    {
        try
        {
            GVRAndroidResource res = new GVRAndroidResource(getGVRContext(), filePath);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            return s;
        }
        catch (IOException ex)
        {
            return null;
        }
    }

    public void onSingleTapUp(MotionEvent event) {
   // mScene.removeSceneObject(model);

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
        model.getTransform()
                .setPosition(x , y , z );
        model.getTransform().setRotation(1f, 0.1f,0.4f,0f);
    }


}