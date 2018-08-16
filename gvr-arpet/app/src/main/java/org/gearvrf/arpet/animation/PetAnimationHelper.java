package org.gearvrf.arpet.animation;

import android.support.annotation.IntDef;

import org.gearvrf.GVRContext;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRepeatMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class PetAnimationHelper {
    @IntDef({PetAnimation.IDLE, PetAnimation.HYDRANT_IN,
            PetAnimation.HYDRANT_OUT, PetAnimation.HYDRANT_LOOP,
            PetAnimation.DRINK_IN, PetAnimation.DRINK_OUT,
            PetAnimation.DRINK_LOOP, PetAnimation.SLEEP_IN,
            PetAnimation.SLEEP_OUT, PetAnimation.SLEEP_LOOP,
            PetAnimation.WALK, PetAnimation.RUN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PetAnimation {
        int IDLE = 0;
        int HYDRANT_IN = 1;
        int HYDRANT_OUT = 2;
        int HYDRANT_LOOP = 3;
        int DRINK_IN = 4;
        int DRINK_OUT = 5;
        int DRINK_LOOP = 6;
        int SLEEP_IN = 7;
        int SLEEP_OUT = 8;
        int SLEEP_LOOP = 9;
        int WALK = 10;
        int RUN = 11;
    }

    private GVRContext mContext;
    private List<GVRAnimation> mAnimationList;
    private GVRAnimator mAnimator;
    private GVROnFinish mCycleFinish;

    public PetAnimationHelper(GVRContext gvrContext) {
        mContext = gvrContext;
    }

    public void setAnimations(List<GVRAnimation> animations, GVRAnimator animator) {
        mAnimationList = animations;
        mAnimator = animator;
        mCycleFinish = null;
        for (GVRAnimation anim : animations) {
            anim.setRepeatMode(GVRRepeatMode.REPEATED);
        }
    }

    public void setCycleFinish(GVROnFinish finishCallback) {
        mCycleFinish = finishCallback;
    }

    public void play(@PetAnimation int animation, int repeatCount) {
        GVRAnimation anim = mAnimationList.get(animation);
        anim.setRepeatCount(repeatCount);

        mAnimator.stop();
        anim.start(mContext.getAnimationEngine());
    }

    public void stop(@PetAnimation int animation) {
        GVRAnimation anim = mAnimationList.get(animation);
        mContext.getAnimationEngine().stop(anim);
    }

    private void playCycle(final GVRAnimation animIn, final GVRAnimation animOut,
                           final GVRAnimation animLoop, int loopCount) {

        animIn.setRepeatCount(1);
        animOut.setRepeatCount(1);
        animLoop.setRepeatCount(loopCount);

        animIn.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                mContext.getAnimationEngine().stop(gvrAnimation);
                animLoop.start(mContext.getAnimationEngine());
            }
        });

        animLoop.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                mContext.getAnimationEngine().stop(gvrAnimation);
                animOut.start(mContext.getAnimationEngine());
            }
        });

        animOut.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation gvrAnimation) {
                mContext.getAnimationEngine().stop(gvrAnimation);
                if (mCycleFinish != null) {
                    mCycleFinish.finished(gvrAnimation);
                }
            }
        });

        mAnimator.stop();
        animIn.start(mContext.getAnimationEngine());
    }

    public void playHydrantCycle() {
        final GVRAnimation animIn = mAnimationList.get(PetAnimation.HYDRANT_IN);
        final GVRAnimation animOut = mAnimationList.get(PetAnimation.HYDRANT_OUT);
        final GVRAnimation animLoop = mAnimationList.get(PetAnimation.HYDRANT_LOOP);

        // TODO: rotate pet so its right side is facing the hydrant
        playCycle(animIn, animOut, animLoop, 3);
    }

    public void playDrinkCycle() {
        final GVRAnimation animIn = mAnimationList.get(PetAnimation.DRINK_IN);
        final GVRAnimation animOut = mAnimationList.get(PetAnimation.DRINK_OUT);
        final GVRAnimation animLoop = mAnimationList.get(PetAnimation.DRINK_LOOP);

        // TODO: position the pet right in front of the water bowl
        playCycle(animIn, animOut, animLoop, 3);
    }

    public void playSleepCycle() {
        final GVRAnimation animIn = mAnimationList.get(PetAnimation.SLEEP_IN);
        final GVRAnimation animOut = mAnimationList.get(PetAnimation.SLEEP_OUT);
        final GVRAnimation animLoop = mAnimationList.get(PetAnimation.SLEEP_LOOP);

        // TODO: play position animation alongside the first animation
        playCycle(animIn, animOut, animLoop, 3);
    }
}
