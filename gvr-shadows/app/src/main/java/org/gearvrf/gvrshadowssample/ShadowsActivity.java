package org.gearvrf.gvrshadowssample;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class ShadowsActivity extends GVRActivity {
    ShadowsMain main = new ShadowsMain();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMain(main);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        main.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
