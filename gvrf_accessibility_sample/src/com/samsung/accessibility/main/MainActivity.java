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
	private long latestButton = 0;
	private long latestTap = 0;
	private MainScript script = null;
	private VRTouchPadGestureDetector detector = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		script = new MainScript();

		setScript(script, "gvr.xml");
		detector = new VRTouchPadGestureDetector(this, this);
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() > latestButton + BUTTON_INTERVAL) {
			latestButton = System.currentTimeMillis();
		}
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			latestButton = System.currentTimeMillis();
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onSingleTap(MotionEvent e) {
		Log.d(MainActivity.class.getSimpleName(), "onSingleTap");
		if (System.currentTimeMillis() > latestTap + TAP_INTERVAL) {
			latestTap = System.currentTimeMillis();
			script.onSingleTap(e);
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
