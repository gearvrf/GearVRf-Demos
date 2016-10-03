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

package org.gearvrf.nonglthreadupdate;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class SampleActivity extends GVRActivity implements OnGestureListener{
    
    private GestureDetectorCompat detector;
    private SampleMain mMain;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(mMain = new SampleMain(), "gvr.xml");
        detector = new GestureDetectorCompat(this, this);        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e){
        detector.onTouchEvent(e);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        mMain.onTapUp();
        return true;
    }
}
