package com.samsung.accessibility.main;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.samsung.accessibility.focus.VRTouchPadGestureDetector;
import com.samsung.accessibility.focus.VRTouchPadGestureDetector.SwipeDirection;

public class MainActivity extends GVRActivity implements
		VRTouchPadGestureDetector.OnTouchPadGestureListener {

	private static final int BUTTON_INTERVAL = 500;
	private static final int TAP_INTERVAL = 300;
	private long mLatestButton = 0;
	private long mLatestTap = 0;
	private MainScript mScript = null;
	private VRTouchPadGestureDetector mDetector = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mScript = new MainScript();

		setScript(mScript, "gvr.xml");
		mDetector = new VRTouchPadGestureDetector(this, this);
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() > mLatestButton + BUTTON_INTERVAL) {
			mLatestButton = System.currentTimeMillis();
		}
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mLatestButton = System.currentTimeMillis();
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onSingleTap(MotionEvent e) {
		Log.d(MainActivity.class.getSimpleName(), "onSingleTap");
		if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
			mLatestTap = System.currentTimeMillis();
			mScript.onSingleTap(e);
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Log.d(MainActivity.class.getSimpleName(), "onLongPress");
	}

	@Override
	public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
			float velocityX, float velocityY) {
		Log.d(MainActivity.class.getSimpleName(), "onSwipe");

		return false;
	}

}
