package org.gearvrf.avatardemo;

import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.animation.GVRRepeatMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AvatarManager extends GVRMain
{
    private static final String TAG = "AVATAR";
    private final String[] YBOT = new String[] { "YBot/ybot.fbx", "animation/mixamo/mixamo_map.txt", "YBot/Football_Hike_mixamo.com.bvh", "YBot/Zombie_Stand_Up_mixamo.com.bvh" };

    private final String[] EVA = { "Eva/Eva.dae", "Eva/pet_map.txt", "Eva/bvhExport_GRAB_BONE.bvh", "Eva/bvhExport_RUN.bvh", "Eva/bvhExport_WALK.bvh" };

    private final String[] CAT = { "Cat/Cat.fbx", "animation/mixamo/pet_map.txt", "Cat/defaultAnim_SitDown.bvh", "Cat/defaultAnim_StandUp.bvh", "Cat/defaultAnim_Walk.bvh" };

    private final String[] HLMODEL = new String[] { "/sdcard/hololab.ply" };

    private final List<String[]> mAvatarFiles = new ArrayList<String[]>();
    private final List<GVRAvatar> mAvatars = new ArrayList<GVRAvatar>();
    private int mAvatarIndex = -1;
    private int mNumAnimsLoaded = 0;
    private String mBoneMap = null;
    private GVRContext mContext;
    private GVRAvatar.IAvatarEvents mEventHandler;

    AvatarManager(GVRContext ctx, GVRAvatar.IAvatarEvents handler)
    {
        mContext = ctx;
        if (handler == null)
        {
            handler = mAvatarListener;
        }
        mEventHandler = handler;
        mAvatarFiles.add(0, EVA);
        mAvatarFiles.add(1, YBOT);
        mAvatarFiles.add(2, CAT);
        mAvatars.add(0, new GVRAvatar(ctx, "EVA"));
        mAvatars.add(1, new GVRAvatar(ctx, "YBOT"));
        mAvatars.add(2, new GVRAvatar(ctx, "CAT"));
        selectAvatar("EVA");
    }

    public GVRAvatar selectAvatar(String name)
    {
        for (int i = 0; i < mAvatars.size(); ++i)
        {
            GVRAvatar avatar = mAvatars.get(i);
            if (name.equals(avatar.getName()))
            {
                if (mAvatarIndex == i)
                {
                    return avatar;
                }
                unselectAvatar();
                mAvatarIndex = i;
                mNumAnimsLoaded = avatar.getAnimationCount();
                if ((avatar.getSkeleton() == null) &&
                        (mEventHandler != null))
                {
                    avatar.getEventReceiver().addListener(mEventHandler);
                }
                if (mNumAnimsLoaded == 0)
                {
                    String mapFile = getMapFile();
                    if (mapFile != null)
                    {
                        mBoneMap = readFile(mapFile);
                    }
                    else
                    {
                        mBoneMap = null;
                    }
                }
                return avatar;
            }
        }
        return null;
    }

    private void unselectAvatar()
    {
        if (mAvatarIndex >= 0)
        {
            GVRAvatar avatar = getAvatar();
            avatar.stop();
            mNumAnimsLoaded = 0;
            mBoneMap = null;
        }
    }

    public GVRAvatar getAvatar()
    {
        return mAvatars.get(mAvatarIndex);
    }

    public String getModelFile()
    {
        return mAvatarFiles.get(mAvatarIndex)[0];
    }

    public String getMapFile()
    {
        String[] files = mAvatarFiles.get(mAvatarIndex);
        if (files.length < 2)
        {
            return null;
        }
        return files[1];
    }

    public String getAnimFile(int animIndex)
    {
        String[] files = mAvatarFiles.get(mAvatarIndex);
        if (animIndex + 2 > files.length)
        {
            return null;
        }
        return files[2 + animIndex];
    }

    public int getAvatarIndex(GVRAvatar avatar)
    {
        return mAvatars.indexOf(avatar);
    }

    public GVRAvatar getAvatar(String name)
    {
        for (GVRAvatar a : mAvatars)
        {
            if (name.equals(a.getName()))
            {
                return a;
            }
        }
        return null;
    }

    public boolean loadModel()
    {
        GVRAndroidResource res = null;
        GVRAvatar avatar = getAvatar();

        try
        {
            res = new GVRAndroidResource(mContext, getModelFile());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
        avatar.loadModel(res);
        return true;
    }

    public boolean loadNextAnimation()
    {
        String animFile = getAnimFile(mNumAnimsLoaded);
        if ((animFile == null) || (mBoneMap == null))
        {
            return false;
        }
        try
        {
            GVRAndroidResource res = new GVRAndroidResource(mContext, animFile);
            ++mNumAnimsLoaded;
            getAvatar().loadAnimation(res, mBoneMap);
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, "Animation could not be loaded from " + animFile);
            return false;
        }
    }

    private String readFile(String filePath)
    {
        try
        {
            GVRAndroidResource res = new GVRAndroidResource(mContext, filePath);
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

    public  GVRAvatar.IAvatarEvents mAvatarListener = new GVRAvatar.IAvatarEvents()
    {
        @Override
        public void onAvatarLoaded(final GVRAvatar avatar, final GVRSceneObject avatarRoot, String filePath, String errors)
        {
            GVRSceneObject.BoundingVolume bv = avatarRoot.getBoundingVolume();
            float scale = 0.3f / bv.radius;
            avatarRoot.getTransform().setScale(scale, scale, scale);
            loadNextAnimation();
        }

        @Override
        public void onAnimationLoaded(GVRAvatar avatar, GVRAnimator animation, String filePath, String errors)
        {
            if (animation != null)
            {
                animation.setRepeatMode(GVRRepeatMode.ONCE);
                animation.setSpeed(1f);
            }
        }

        public void onModelLoaded(GVRAvatar avatar, GVRSceneObject avatarRoot, String filePath, String errors) { }

        public void onAnimationFinished(GVRAvatar avatar, GVRAnimator animator, GVRAnimation animation) { }

        public void onAnimationStarted(GVRAvatar avatar, GVRAnimator animator) { }
    };
}
