package org.gearvrf.avatardemo;

import java.io.IOException;
import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.GVRSceneObject;
import android.graphics.Color;
import android.util.Log;


public class AvatarMain extends GVRMain {
    private final String[] mAnimationPaths = {"YBot/Zombie_Stand_Up_mixamo.com.bvh"};
    private static final String TAG = "AVATAR";
    private GVRContext mContext;
    private GVRScene mScene;
    private GVRAvatar mAvatar;
    private GVRActivity mActivity;
    private GVRAndroidResource res;

    public AvatarMain(GVRActivity activity) {
        mActivity = activity;
    }


    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        GVRCameraRig rig = mScene.getMainCameraRig();
        rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
        rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
        rig.getHeadTransformObject().attachComponent(new GVRDirectLight(mContext));
        mAvatar = new GVRAvatar(gvrContext, "BVHSkeleton");

        try {
               GVRSceneObject root = new GVRSceneObject(mContext);

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


    @Override
    public void onStep() {


    }


}