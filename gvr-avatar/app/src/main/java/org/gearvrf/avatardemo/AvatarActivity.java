package org.gearvrf.avatardemo;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class AvatarActivity extends GVRActivity {
    AvatarMain mMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new AvatarMain(this);
        setMain(mMain, "gvr.xml");
        enableGestureDetector();
    }
}
