
package com.samsung.accessibility;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

import com.samsung.accessibility.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import com.samsung.accessibility.VRTouchPadGestureDetector.SwipeDirection;

public class MainActivity extends GVRActivity implements OnTouchPadGestureListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScript(new MainScript(), "gvr.xml");
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        return false;
    }
}
