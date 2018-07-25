package org.gearvrf.avatardemo;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRSkeleton;

import android.util.Log;

public class AvatarMain extends GVRMain
{
    //private final String mModelPath = "TRex_NoGround.fbx";
    private final String mModelPath = "JazzDance/JazzDance.dae";
    private static final String TAG = "AVATAR";

    private GVRContext mGVRContext;
    private GVRSceneObject mCharacter;
    private GVRAnimator mAnimator = null;
    private GVRActivity mActivity;
    private GVRSkeleton mAvatarSkeleton;
    private GVRSkeleton mDeepMotionSkeleton;

    private String[] mDeepMotionBoneNames =
    {
            "Pelvis", "HipRight", "KneeRight", "AnkleRight", "HipLeft",
            "KneeLeft", "AnkleLeft", "SpineShoulder", "ShoulderRight",
            "ElbowRight", "WristRight", "ShoulderLeft", "ElbowLeft",
            "WristLeft", "Neck", "HeadOrient"
    };


    private int[] mDeepMotionBoneParents =
        {
                -1, 0, 1, 2, 0, 4, 5, 0, 7, 8, 9, 7, 11, 12, 7, 14
        };

    private String[] mMixamoBoneNames = 
    {
        "mixamorig_Hips",
        "mixamorig_Spine",
        "mixamorig_Spine1",
        "mixamorig_Spine2",
        "mixamorig_Neck 3",
        "mixamorig_Head 4",
        "mixamorig_RightEye",
        "mixamorig_LeftEye",
        "mixamorig_LeftShoulder",
        "mixamorig_LeftArm",
        "mixamorig_LeftForeArm",
        "mixamorig_LeftHand",
        "mixamorig_LeftHandMiddle1",
        "mixamorig_LeftHandMiddle2",
        "mixamorig_LeftHandMiddle3",
        "mixamorig_LeftHandMiddle4",
        "mixamorig_LeftHandThumb1",
        "mixamorig_LeftHandThumb2",
        "mixamorig_LeftHandThumb3",
        "mixamorig_LeftHandThumb4",
        "mixamorig_LeftHandIndex1",
        "mixamorig_LeftHandIndex2",
        "mixamorig_LeftHandIndex3",
        "mixamorig_LeftHandIndex4",
        "mixamorig_LeftHandRing1",
        "mixamorig_LeftHandRing2",
        "mixamorig_LeftHandRing3",
        "mixamorig_LeftHandRing4",
        "mixamorig_LeftHandPinky1",
        "mixamorig_LeftHandPinky2",
        "mixamorig_LeftHandPinky3",
        "mixamorig_LeftHandPinky4",
        "mixamorig_RightShoulder",
        "mixamorig_RightArm",
        "mixamorig_RightForeArm",
        "mixamorig_RightHand",
        "mixamorig_RightHandMiddle1",
        "mixamorig_RightHandMiddle2",
        "mixamorig_RightHandMiddle3",
        "mixamorig_RightHandMiddle4",
        "mixamorig_RightHandThumb1",
        "mixamorig_RightHandThumb2",
        "mixamorig_RightHandThumb3",
        "mixamorig_RightHandThumb4",
        "mixamorig_RightHandIndex1",
        "mixamorig_RightHandIndex2",
        "mixamorig_RightHandIndex3",
        "mixamorig_RightHandIndex4",
        "mixamorig_RightHandRing1",
        "mixamorig_RightHandRing2",
        "mixamorig_RightHandRing3",
        "mixamorig_RightHandRing4",
        "mixamorig_RightHandPinky1",
        "mixamorig_RightHandPinky2",
        "mixamorig_RightHandPinky3",
        "mixamorig_RightHandPinky4",
        "mixamorig_LeftUpLeg",
        "mixamorig_LeftLeg",
        "mixamorig_LeftFoot",
        "mixamorig_LeftToeBase",
        "mixamorig_LeftToe_End",
        "mixamorig_RightUpLeg",
        "mixamorig_RightLeg",
        "mixamorig_RightFoot",
        "mixamorig_RightToeBase",
        "mixamorig_RightToe_End",
    };

    private int[] mMixamoBoneParents =
    {
        -1, 0, 1, 2, 3, 4, 5, 5,
        3, 8, 9, 10, 11, 12, 13, 14,
        11, 16, 17, 18, 11, 20, 21, 22,
        11, 24, 25, 26, 11, 28, 29, 30,
        3, 32, 33, 34, 35, 36, 37, 38,
        35, 40, 41, 42, 35, 44, 45, 46,
        35, 48, 49, 50, 35, 52, 53, 54,
        0, 56, 57, 58, 59, 0, 61, 62,
        63, 64
    };

    private int[] mMixamoBoneMap =
    {
        0, -1, -1, -1,
        14, -1  -1, -1,
        11, 12, -1, 13,
        -1, -1, -1, -1,
        -1, -1, -1, -1,
        -1, -1, -1, -1,
        -1, -1, -1, -1,
        -1, -1, -1,  8,
        9, -1, 10,  -1,
        -1, -1, -1, -1,
        -1, -1, -1, -1,
        -1, -1, -1, -1,
        -1, -1, -1, -1,
        -1, -1, -1, 4,
        5, 6, -1, -1,
        1, 2, 3, -1,
        -1
    };

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
        model.getTransform().setPosition(x - bv.center.x, y - bv.center.y, z - bv.center.z - 1.5f * bv.radius);
    }

    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        GVRScene mainScene = gvrContext.getMainScene();
        mDeepMotionSkeleton = new GVRSkeleton(gvrContext, mDeepMotionBoneParents);
        mDeepMotionSkeleton.setBoneNames(mDeepMotionBoneNames);

        try
        {
            EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedMorphSettings();
            mCharacter = gvrContext.getAssetLoader().loadModel(mModelPath, settings, false, mainScene);
            centerModel(mCharacter, mainScene.getMainCameraRig().getTransform());
            List<GVRComponent> components = mCharacter.getAllComponents(GVRSkeleton.getComponentType());
            String[] boneNames = null;

            if (components.size() > 0)
            {
                mAvatarSkeleton = (GVRSkeleton) components.get(0);
                boneNames = mAvatarSkeleton.getBoneNames();
                String s = "";
                for (int i = 0; i < boneNames.length; ++i)
                {
                    Integer boneParent = mAvatarSkeleton.getParentBoneIndex(i);
                    s += boneNames[i] + " " + boneParent.toString() + "\n";
                }
                Log.v(TAG, s);
            }
            else
            {
                Log.e(TAG, "Avatar skeleton not found");
            }
            mAnimator = (GVRAnimator) mCharacter.getComponent(GVRAnimator.getComponentType());
            mAnimator.setRepeatMode(GVRRepeatMode.REPEATED);
            mAnimator.setRepeatCount(-1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            mActivity.finish();
            mActivity = null;
            Log.e(TAG, "One or more assets could not be loaded.");
        }
        gvrContext.getInputManager().selectController();
        mAnimator.start();
    }

    @Override
    public void onStep() {
    }
}
