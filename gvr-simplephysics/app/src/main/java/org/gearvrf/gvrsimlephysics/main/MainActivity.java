package org.gearvrf.gvrsimlephysics.main;

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.gvrsimlephysics.util.VRTouchPadGestureDetector;
import org.gearvrf.io.GVRTouchPadGestureDetector;

public class MainActivity extends GVRActivity implements GVRTouchPadGestureDetector.OnTouchPadGestureListener {

    private MainScript main = null;
    private GVRTouchPadGestureDetector touchPadGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new MainScript();
        touchPadGestureDetector = new GVRTouchPadGestureDetector(this);
        setMain(main, "gvr.xml");

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchPadGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onSwipe(MotionEvent e, GVRTouchPadGestureDetector.SwipeDirection swipeDirection, float velocityX, float velocityY) {
        main.onSwipe(swipeDirection, velocityX);
        return super.onTouchEvent(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    }
