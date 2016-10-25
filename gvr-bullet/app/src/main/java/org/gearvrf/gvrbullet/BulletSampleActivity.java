package org.gearvrf.gvrbullet;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class BulletSampleActivity extends GVRActivity {

    private BulletSampleMain main = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new BulletSampleMain();
        setMain(main, "gvr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            main.touchEvent();
        }

        return super.onTouchEvent(event);
    }

}
