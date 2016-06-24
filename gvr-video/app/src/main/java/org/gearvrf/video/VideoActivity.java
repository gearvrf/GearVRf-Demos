/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.video;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.util.VRTouchPadGestureDetector;
import org.gearvrf.util.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import org.gearvrf.util.VRTouchPadGestureDetector.SwipeDirection;

public class VideoActivity extends GVRActivity implements
        OnTouchPadGestureListener {
    private static final int TAP_INTERVAL = 300;
    private VideoMain mMain = null;
    private VRTouchPadGestureDetector mDetector = null;
    private long mLatestTap = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new VideoMain(this);
        mDetector = new VRTouchPadGestureDetector(this);
        setMain(mMain, "gvr.xml");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMain.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        mMain.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        Log.v("", "onSingleTap");
        if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
            mLatestTap = System.currentTimeMillis();
            mMain.onSingleTap(e);
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v("", "onLongPress");
    }

    @Override
    public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
            float velocityX, float velocityY) {
        Log.v("", "onSwipe");
        return false;
    }
}
