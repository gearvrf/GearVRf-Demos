package org.gearvrf.avatardemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.gearvrf.animation.keyframe.BVHImporter;
import org.gearvrf.animation.keyframe.GVRSkeletonAnimation;
import org.gearvrf.utility.Log;

import android.graphics.Color;
import android.view.MotionEvent;


public class AvatarMain extends GVRMain {

    private final String[] mAnimationPaths = {"YBot/Zombie_Stand_Up_mixamo.com.bvh"};
    private static final String TAG = "AVATAR";
    private GVRContext mContext;
    private GVRScene mScene;
    private GVRAvatar mAvatar;
    private GVRActivity mActivity;
    private GVRAndroidResource res;
    private GVRAnimator mAnimator;
    GVRSceneObject root;


    public AvatarMain(GVRActivity activity) {
        mActivity = activity;
    }
    private GVRAvatar.IAvatarEvents mAvatarListener = new GVRAvatar.IAvatarEvents()
    {
        @Override
        public void onModelLoaded(GVRSceneObject avatarRoot, String filePath, String errors) {

        }

        @Override
        public void onAvatarLoaded(final GVRSceneObject avatarRoot, String filePath, String errors)
        {

        }

        @Override
        public void onAnimationLoaded(GVRAnimator animation, String filePath, String errors)
        {
            GVRSkeletonAnimation skelAnim = (GVRSkeletonAnimation) animation.getAnimation(0);
           // root.attachComponent(skelAnim);
            mAnimator = animation;
            if (mAnimator == null)
            {
                mAnimator = new GVRAnimator(root.getGVRContext());
                root.attachComponent(mAnimator);
            }
            mAnimator.setRepeatMode(GVRRepeatMode.REPEATED);
            mAnimator.setRepeatCount(-1);
            mAnimator.start();
        }

        public void onAnimationFinished(GVRAnimator animator, GVRAnimation animation)
        {

        }

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
        mAvatar = new GVRAvatar(gvrContext, "BVHSkeleton");
        mAvatar.getEventReceiver().addListener(mAvatarListener);
        try {
                root = new GVRSceneObject(mContext);

               res = new GVRAndroidResource(mContext, mAnimationPaths[0]);

               root = mAvatar.createSkeletonGeometry(root,mContext,res);

               mAvatar.centerModel(root);
               mScene.addSceneObject(root);

        } catch (IOException e) {

            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
                   }

        gvrContext.getInputManager().selectController();
    }
    private void loadAnimation() throws IOException {
        mAvatar.loadAnimation(res, null);
    }

    @Override
    public void onStep() {


    }
    public void onSingleTapUp(MotionEvent event) {

        try {
            loadAnimation();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


}