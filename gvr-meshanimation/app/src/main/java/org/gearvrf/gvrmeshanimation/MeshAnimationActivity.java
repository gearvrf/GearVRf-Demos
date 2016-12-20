package org.gearvrf.gvrmeshanimation;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class MeshAnimationActivity extends GVRActivity {

    private MeshAnimationMain mMain;
    private long lastDownTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new MeshAnimationMain(this);
        setMain(mMain, "gvr.xml");
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
                // pass it as a tap to the Main
                mMain.onTap();
            }
        }

        return true;
    }
}
