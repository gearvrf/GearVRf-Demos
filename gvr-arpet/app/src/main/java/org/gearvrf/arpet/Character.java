package org.gearvrf.arpet;

import org.gearvrf.GVRDrawFrameListener;

public class Character implements GVRDrawFrameListener {

    private enum PetAction {
        IDLE,
        TO_BALL,
        TO_SCREEN,
        TO_FOOD,
        TO_TOILET,
        TO_BED
    }

    private PetAction mCurrentAction;

    public Character() {
        mCurrentAction = PetAction.IDLE;
    }

    public void goToBall() {
        mCurrentAction = PetAction.TO_BALL;
    }

    public void goToScreen() {
        mCurrentAction = PetAction.TO_SCREEN;
    }

    public void goToFood() {
        mCurrentAction = PetAction.TO_FOOD;
    }

    public void goToToilet() {
        mCurrentAction = PetAction.TO_TOILET;
    }

    public void goToBed() {
        mCurrentAction = PetAction.TO_BED;
    }

    private void moveToBall() {

    }

    private void moveToScreen() {

    }

    @Override
    public void onDrawFrame(float v) {

    }
}
