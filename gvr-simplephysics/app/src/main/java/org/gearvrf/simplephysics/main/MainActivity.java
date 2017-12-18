package org.gearvrf.simplephysics.main;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.io.GVRTouchPadGestureListener;

public class MainActivity extends GVRActivity
{
    private MainScript main = null;
    private GestureDetector touchPadGestureDetector;

    private GVRTouchPadGestureListener swipeListener = new GVRTouchPadGestureListener()
    {
        @Override
        public boolean onSwipe(MotionEvent e, Action action, float vx, float vy) {
            main.onSwipe(action, vx);
            return MainActivity.super.onTouchEvent(e);
        }
     };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new MainScript();
        touchPadGestureDetector = new GestureDetector(getBaseContext(), swipeListener);
        setMain(main, "gvr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        touchPadGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
