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
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRRepeatMode;

import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

public class AvatarMain extends GVRMain {
    private final String mModelPath = "YBot/ybot.fbx";
    //private final String mModelPath = "Andromeda/Andromeda.dae";
    //private final String[] mAnimationPaths =  { "Andromeda/HipHopDancing.dae", "Andromeda/Bellydancing.dae", "Andromeda/Boxing.dae" };
//    private final String[] mAnimationPaths = {"YBot/Zombie_Stand_Up_mixamo.com.bvh", "YBot/Football_Hike_mixamo.com.bvh","YBot/Football_Hike_mixamo.com.bvh"};
    private final String[] mAnimationPaths = {"test.bvh"};

    private static final String TAG = "AVATAR";
    private GVRContext mContext;
    private GVRScene mScene;
    private GVRAvatar mAvatar;
    private GVRActivity mActivity;
    private int mNumAnimsLoaded = 0;
    private int mCurrentAnimIndex = -1;
    private String mBoneMap;

    public AvatarMain(GVRActivity activity) {
        mActivity = activity;
    }

    private GVRAvatar.IAvatarEvents mAvatarListener = new GVRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final GVRSceneObject avatarRoot, String filePath, String errors)
        {
            mBoneMap = readFile("YBot/arcsoft_ybot.txt");
            if (avatarRoot.getParent() == null)
            {
                mContext.runOnGlThread(new Runnable() {
                    public void run() {
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
                animation.setRepeatMode(GVRRepeatMode.ONCE);
                animation.setSpeed(1f);
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

        public void onModelLoaded(GVRSceneObject obj, String path, String errors) { }

        public void onAnimationStarted(GVRAnimator animator) { }
    };


    @Override
    public void onInit(GVRContext gvrContext)
    {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        GVRCameraRig rig = mScene.getMainCameraRig();
        rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
        rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
        rig.getHeadTransformObject().attachComponent(new GVRDirectLight(mContext));
        mAvatar = new GVRAvatar(gvrContext, "YBot");
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
        mAvatar.loadAnimation(new GVRAndroidResource(mContext, animPath), mBoneMap);
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