package org.gearvrf.gvrmeshanimation;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRPose;
import org.gearvrf.GVRSceneObject;

import org.gearvrf.GVRSkeleton;
import org.gearvrf.animation.GVRAnimation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

class CustomAnimation extends GVRAnimation {
    private GVRSkeleton skeleton;
    private GVRPose pose;

    private static final String HAND_PREFIX = "r_";
    private static final String PINKY = "pinky_";
    private static final String RING = "ring_";
    private static final String MIDDLE = "middle_";
    private static final String INDEX = "index_";
    private static final String THUMB = "thumb_";

    private static final String DISTAL = "4";
    private static final String INTERMEDIATE = "3";
    private static final String PROXIMAL = "2";

    private static final String PALM = HAND_PREFIX + "palm";

    private static final int NUM_BONES = 14;

    private static String[] BONES = new String[NUM_BONES];
    private static int[] BONE_INDICES = new int[NUM_BONES];

    private Quaternionf scratchRotation = new Quaternionf();
    private int palmIndex;

    static {
        String[] hand = new String[5];
        hand[0] = THUMB;
        hand[1] = INDEX;
        hand[2] = MIDDLE;
        hand[3] = RING;
        hand[4] = PINKY;

        String[] bone = new String[3];
        bone[0] = PROXIMAL;
        bone[1] = INTERMEDIATE;
        bone[2] = DISTAL;

        int count = 0;
        // exclude thumb
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                BONES[count] = HAND_PREFIX + hand[i] + bone[j];
                count++;
            }
        }

        //Thumbs
        BONES[count] = HAND_PREFIX + THUMB + "2";
        count++;
        BONES[count] = HAND_PREFIX + THUMB + "3";
    }

    private Map<String, Quaternionf> boneRotations = new HashMap<String, Quaternionf>();

    CustomAnimation(GVRContext gvrContext, GVRSceneObject sceneObject, float
            duration) {
        super(sceneObject, duration);
        skeleton = new GVRSkeleton(gvrContext);
        sceneObject.attachComponent(skeleton);

        pose = skeleton.getPose();

        for (int i = 0; i < NUM_BONES; i++) {
            Quaternionf q = new Quaternionf();
            BONE_INDICES[i] = skeleton.getBoneIndex(BONES[i]);
            pose.getLocalMatrix(BONE_INDICES[i]).getUnnormalizedRotation(q);
            boneRotations.put(BONES[i], q);
        }
        palmIndex = skeleton.getBoneIndex(PALM);
    }

    @Override
    protected void animate(GVRHybridObject gvrHybridObject, float ratio) {
        Quaternionf palmRotation = new Quaternionf();
        palmRotation.rotateZ((float) Math.toRadians(180.0f));
        pose.setWorldPosition(palmIndex, new Vector3f(0.0f, -10.0f, -30.0f));
        pose.setWorldRotation(palmIndex, palmRotation);
        float boneRotationAngle = 45.0f * ratio;
        Quaternionf q = new Quaternionf();
        q.rotateZ((float) Math.toRadians(boneRotationAngle));

        for (int i = 0; i < NUM_BONES; i++) {

            Quaternionf boneRotation = boneRotations.get(BONES[i]);
            scratchRotation.identity();
            boneRotation.mul(q, scratchRotation);
            pose.setLocalRotation(BONE_INDICES[i], scratchRotation);
        }
        skeleton.update();
    }
}
