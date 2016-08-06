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

package org.gearvrf.gvr360Photo;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;

public class Minimal360PhotoActivity extends GVRActivity implements
        VRTouchPadGestureDetector.OnTouchPadGestureListener
{
    /** Called when the activity is first created. */
    private VRTouchPadGestureDetector mDetector = null;
    private Minimal360PhotoScript script = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        script = new Minimal360PhotoScript();
        mDetector = new VRTouchPadGestureDetector(this);

        setScript(script, "gvr.xml");
    }

    @Override
    public boolean onSingleTap(MotionEvent e) {
        Log.i(TAG, "onSingleTap");
        script.onSingleTap(e);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress");
    }

    @Override
    public boolean onSwipe(MotionEvent e, VRTouchPadGestureDetector.SwipeDirection swipeDirection,
                           float velocityX, float velocityY) {
        Log.i(TAG, "onSwipe");
        script.onSwipe(e, swipeDirection, velocityX, velocityY);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        script.onScroll(arg0, arg1, arg2, arg3);
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
