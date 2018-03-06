/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.main;

import org.gearvrf.GVRActivity;
import org.gearvrf.io.GVRTouchPadGestureListener;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class MainActivity extends GVRActivity
{

    private static final int BUTTON_INTERVAL = 500;
    private static final int TAP_INTERVAL = 300;
    private long mLatestButton = 0;
    private long mLatestTap = 0;
    private Main mMain = null;
    private GestureDetector mDetector = null;
    private GVRTouchPadGestureListener swipeListener = new GVRTouchPadGestureListener()
    {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(MainActivity.class.getSimpleName(), "onSingleTap");
            if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
                mLatestTap = System.currentTimeMillis();
                mMain.onSingleTap(e);
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new Main();
        mDetector = new GestureDetector(getBaseContext(), swipeListener);
        setMain(mMain, "gvr.xml");
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

}
