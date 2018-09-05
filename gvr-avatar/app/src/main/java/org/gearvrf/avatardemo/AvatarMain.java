package org.gearvrf.avatardemo;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.keyframe.TRSImporter;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRPose;
import org.gearvrf.animation.GVRPoseMapper;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.animation.keyframe.GVRSkeletonAnimation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import android.util.Log;
import android.view.MotionEvent;

public class AvatarMain extends GVRMain
{
//    private final String mModelPath = "DeepMotion/sahithi_character_skin.fbx";
//    private final String mAnimationPath = "bodyturn.txt";
//    private final String mModelPath = "Andromeda/Andromeda.dae";
//    private final String[] mAnimationPaths =  { "Andromeda/HipHopDancing.dae", "Andromeda/Bellydancing.dae", "Andromeda/Boxing.dae" };
//    private final String[] mAnimationPaths = { "DeepMotion/animation_baked.fbx" };
//    private final String mModelPath = "Lily/female_outfitJ.fbx";
//    private final String[] mAnimationPaths = { "Lily/Idle.fbx" };
    private final String mModelPath = "astroboy/astro_boy.dae";
    private final String[] mAnimationPaths =  { "astroboy/astro_boy.dae" };

    private static final String TAG = "AVATAR";

    private GVRContext      mContext;
    private GVRScene        mScene;
    private GVRAvatar       mAvatar;
    private GVRActivity     mActivity;
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
        }

        @Override
        public void onAnimationLoaded(GVRAnimator animation, String filePath, String errors)
        {
            if (!mAvatar.isRunning())
            {
                mCurrentAnimIndex = 0;
                mAvatar.start(0);
            }
        }

        public void onAnimationFinished(GVRAnimator animator, GVRAnimation animation)
        {
            if (++mCurrentAnimIndex >= mAvatar.getAnimationCount())
            {
                mCurrentAnimIndex = 0;
            }
            mAvatar.start(mCurrentAnimIndex);
        }

        public void onAnimationStarted(GVRAnimator animator) { }
    };


    @Override
    public void onInit(GVRContext gvrContext)
    {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        mScene.getMainCameraRig().getHeadTransformObject().attachComponent(new GVRDirectLight(mContext));
        mAvatar = new GVRAvatar(gvrContext, "Andromeda");
        mAvatar.getEventReceiver().addListener(mAvatarListener);
        try
        {
            mAvatar.loadModel(new GVRAndroidResource(gvrContext, mModelPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
        gvrContext.getInputManager().selectController();
    }


    private void loadAnimation(String animPath) throws IOException
    {
        mAvatar.loadAnimation(new GVRAndroidResource(mContext, animPath));
    }

    @Override
    public void onStep() {
    }

    public void onSingleTapUp(MotionEvent event)
    {
        if (mNumAnimsLoaded < mAnimationPaths.length)
        {
            try
            {
                loadAnimation(mAnimationPaths[mNumAnimsLoaded++]);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        else if (mAvatar.isRunning())
        {
            mAvatar.stop();
        }
        else
        {
            mCurrentAnimIndex = 0;
            mAvatar.start(mCurrentAnimIndex);
        }
    }
}
