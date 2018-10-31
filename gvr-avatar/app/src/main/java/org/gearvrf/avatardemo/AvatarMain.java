package org.gearvrf.avatardemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRRenderData;
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
                mContext.runOnGlThread(new Runnable()
                {
                    public void run()
                    {

                        GVRTransform t = avatarRoot.getTransform();
                        avatar.centerModel(avatarRoot);
                        t.setPosition(0, -0.01f, -0.15f);
                        // avatarRoot.getTransform().setRotationByAxis(-180,0,1,0);
                        t.setScale(0.0004f, 0.0004f, 0.0004f);
                        mScene.addSceneObject(avatarRoot);
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
        topLight.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1f);
        GVRSceneObject topLightObj = new GVRSceneObject(gvrContext);
        topLightObj.attachComponent(topLight);
        topLightObj.getTransform().rotateByAxis(-45, 1, 0, 0);
        // mScene.addSceneObject(topLightObj);
        rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
        rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
        GVRSceneObject environment = null;
        GVRAvatar avatar = mAvManager.selectAvatar("YBOT");

        try
        {
            environment = mContext.getAssetLoader().loadModel("environment/environment_test_2.fbx", mScene);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            mActivity.finish();
            mActivity = null;
        }
        avatar.centerModel(environment);
        environment.getTransform().setPositionZ(-1.25f);
        mAvManager.loadModel();
        gvrContext.getInputManager().selectController();
    }

    @Override
    public void onStep()
    {
    }


}