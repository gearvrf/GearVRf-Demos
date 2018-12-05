package org.gearvrf.avatardemo;

import java.io.IOException;

import org.gearvrf.GVRActivity;
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


public class AvatarMain extends GVRMain
{
    private GVRContext mContext;
    private GVRScene mScene;
    private GVRActivity mActivity;
    private AvatarManager mAvManager;

    public AvatarMain(GVRActivity activity)
    {
        mActivity = activity;
    }

    private GVRAvatar.IAvatarEvents mAvatarListener = new GVRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final GVRAvatar avatar, final GVRSceneObject avatarRoot, String filePath, String errors)
        {
            if (avatarRoot.getParent() == null)
            {
                mScene.addSceneObject(avatarRoot);
                mContext.runOnGlThreadPostRender(1, new Runnable()
                {
                    public void run()
                    {
                        GVRTransform t = avatarRoot.getTransform();
                        GVRSceneObject.BoundingVolume bv = avatarRoot.getBoundingVolume();
                        float sf = 1 / bv.radius;
                        t.setScale(sf, sf, sf);
                        bv = avatarRoot.getBoundingVolume();
                        t.setPosition(-bv.center.x, -0.5f - bv.center.y, -bv.center.z - 1);
                    }
                });
            }
            mAvManager.loadNextAnimation();
        }

        @Override
        public void onAnimationLoaded(GVRAvatar avatar, GVRAnimator animation, String filePath, String errors)
        {
            animation.setRepeatMode(GVRRepeatMode.ONCE);
            animation.setSpeed(1f);
            if (!avatar.isRunning())
            {
                avatar.startAll(GVRRepeatMode.REPEATED);
            }
            else
            {
                avatar.start(animation.getName());
            }
            mAvManager.loadNextAnimation();
        }

        public void onModelLoaded(GVRAvatar avatar, GVRSceneObject avatarRoot, String filePath, String errors)
        {
        }

        public void onAnimationFinished(GVRAvatar avatar, GVRAnimator animator, GVRAnimation animation)
        {
        }

        public void onAnimationStarted(GVRAvatar avatar, GVRAnimator animator)
        {
        }
    };

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mAvManager = new AvatarManager(gvrContext, mAvatarListener);
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        GVRCameraRig rig = mScene.getMainCameraRig();
        GVRDirectLight topLight = new GVRDirectLight(gvrContext);
        GVRDirectLight headLight = new GVRDirectLight(gvrContext);
        GVRSceneObject environment = null;

        headLight.setAmbientIntensity(0, 0, 0, 1f);
        headLight.setDiffuseIntensity(0.3f, 0.3f, 0.3f, 1f);
        headLight.setSpecularIntensity(0.3f, 0.3f, 0.3f, 1f);
        topLight.setSpecularIntensity(0.3f, 0.3f, 0.3f, 1f);
        topLight.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1f);
        topLight.setDiffuseIntensity(0.5f, 0.5f, 0.5f, 1f);
        GVRSceneObject topLightObj = new GVRSceneObject(gvrContext);
        topLightObj.attachComponent(topLight);
        topLightObj.getTransform().rotateByAxis(-45, 1, 0, 0);
        mScene.addSceneObject(topLightObj);
        rig.getOwnerObject().attachLight(headLight);
        rig.getTransform().setPositionY(-0.5f);
        rig.getRightCamera().setBackgroundColor(Color.BLUE);
        rig.getLeftCamera().setBackgroundColor(Color.BLUE);
        rig.getCenterCamera().setBackgroundColor(Color.BLUE);
        mAvManager.selectAvatar("YBOT");

        try
        {
            environment = mContext.getAssetLoader().loadModel("environment/environment_test_2.fbx", mScene);
            environment.getTransform().rotateByAxis(180, 1, 0, 0);
            environment.getTransform().setScale(2, 2, 2);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            mActivity.finish();
            mActivity = null;
        }
        mAvManager.loadModel();
        gvrContext.getInputManager().selectController();
    }

    @Override
    public void onStep()
    {
    }


}