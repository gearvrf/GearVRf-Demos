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
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.animation.keyframe.GVRSkeletonAnimation;

import android.graphics.Color;
import android.util.Log;

public class AvatarMain extends GVRMain
{
    private final String mModelPath = "YBot/ybot.fbx";
     //  private final String[] mAnimationPaths =  { "animation/mixamo/Ybot_SambaDancing.bvh" };
 //  private final String mBoneMapPath = "animation/mixamo/bonemap.txt";

    private final String[] mAnimationPaths =  {
            "animation/captured/Video1_BVH.bvh",
            "animation/captured/Video2_BVH.bvh",
            "animation/captured/Video3_BVH.bvh",
            "animation/captured/Video4_BVH.bvh",
            "animation/captured/Video5_BVH.bvh",
            "animation/captured/Video6_BVH.bvh"
    };
    private final String mBoneMapPath = "animation/captured/bonemap.txt";
    private String mBoneMap;

    private static final String TAG = "AVATAR";

    private GVRContext      mContext;
    private GVRScene        mScene;
    private GVRAvatar       mAvatar;
    private GVRActivity     mActivity;
    private GVRSceneObject  mSkeletonGeometry;
    private int             mNumAnimsLoaded = 0;
    private int             mCurrentAnimIndex = -1;

    public AvatarMain(GVRActivity activity) {
        mActivity = activity;
    }

    private GVRAvatar.IAvatarEvents mAvatarListener = new GVRAvatar.IAvatarEvents()
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
                        mAvatar.centerModel(avatarRoot);
                        mScene.addSceneObject(avatarRoot);
                    }
                });
            }
            if ((mNumAnimsLoaded == 0) &&
                    (mAvatar.getSkeleton() != null))
            {
                loadNextAnimation(mAvatar, mBoneMap);
            }

        }

        @Override
        public void onModelLoaded(final GVRSceneObject avatarRoot, String filePath, String errors) { }

        @Override
        public void onAnimationLoaded(GVRAnimator animation, String filePath, String errors)
        {
            GVRSkeletonAnimation skelAnim = (GVRSkeletonAnimation) animation.getAnimation(0);
            GVRSkeleton skel = skelAnim.getSkeleton();

            animation.setRepeatMode(GVRRepeatMode.ONCE);
            animation.setSpeed(1f);
            ++mNumAnimsLoaded;
            if (mSkeletonGeometry == null)
            {
                mSkeletonGeometry = new GVRSceneObject(mContext);
                mSkeletonGeometry.setName("SkeletonGeometry");
                mSkeletonGeometry.getTransform().setPosition(-5, -4, -10);
                skel.createSkeletonGeometry(mSkeletonGeometry);
                mScene.addSceneObject(mSkeletonGeometry);
            }
            else
            {
                mSkeletonGeometry.detachComponent(GVRSkeleton.getComponentType());
                mSkeletonGeometry.attachComponent(skel);
                skel.createSkeletonGeometry(mSkeletonGeometry);
            }
            if (!mAvatar.isRunning())
            {
                mCurrentAnimIndex = -1;
                startNextAnimation();
            }
        }

        public void onAnimationFinished(GVRAnimator animator, GVRAnimation animation)
        {
            startNextAnimation();
        }

        public void onAnimationStarted(GVRAnimator animator)
        {
            loadNextAnimation(mAvatar, mBoneMap);
        }

    };


    @Override
    public void onInit(GVRContext gvrContext)
    {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        GVRCameraRig rig = mScene.getMainCameraRig();
        GVRDirectLight topLight = new GVRDirectLight(gvrContext);
        GVRSceneObject topLightObj = new GVRSceneObject(gvrContext);

        topLightObj.attachComponent(topLight);
        topLightObj.getTransform().rotateByAxis(-90, 1, 0, 0);
        mScene.addSceneObject(topLightObj);
        rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
        rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
        rig.getOwnerObject().attachComponent(new GVRDirectLight(mContext));

        mAvatar = new GVRAvatar(gvrContext, "YBot");
        mAvatar.getEventReceiver().addListener(mAvatarListener);
        mBoneMap = readFile(mBoneMapPath);
        try
        {
            mAvatar.loadModel(new GVRAndroidResource(gvrContext, mModelPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
        }
        gvrContext.getInputManager().selectController();
    }


    private int startNextAnimation()
    {
        if (++mCurrentAnimIndex >= mAvatar.getAnimationCount())
        {
            mCurrentAnimIndex = 0;
        }
        if (mAvatar.getAnimationCount() > 0)
        {
            mAvatar.start(mCurrentAnimIndex);
        }
        return mCurrentAnimIndex;
    }

    private void loadNextAnimation(GVRAvatar avatar, String bonemap)
    {
        if (mNumAnimsLoaded < mAnimationPaths.length)
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



}