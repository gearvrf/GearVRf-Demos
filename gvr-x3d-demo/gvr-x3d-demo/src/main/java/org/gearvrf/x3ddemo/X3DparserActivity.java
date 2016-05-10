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

package org.gearvrf.x3ddemo;

import org.gearvrf.GVRActivity;
import org.gearvrf.animation.GVRAnimation;
//import org.gearvrf.util.VRTouchPadGestureDetector;
//import org.gearvrf.util.VRTouchPadGestureDetector.OnTouchPadGestureListener;
//import org.gearvrf.util.VRTouchPadGestureDetector.SwipeDirection;
import org.gearvrf.x3ddemo.VRTouchPadGestureDetector.OnTouchPadGestureListener;
import org.gearvrf.x3ddemo.VRTouchPadGestureDetector.SwipeDirection;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class X3DparserActivity extends GVRActivity implements OnTouchPadGestureListener {

    private VRTouchPadGestureDetector mVRTouchPadDetector = null;
    X3DparserScript script = new X3DparserScript();

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setScript(script, "gvr_note4.xml");
        mVRTouchPadDetector = new VRTouchPadGestureDetector(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      mVRTouchPadDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            script.captureScreen(0, "screenshot_center");
            script.captureScreen(1, "screenshot_left");
            script.captureScreen(2, "screenshot_right");
            script.captureScreen3D("screenshot3d");
        }
        return super.onTouchEvent(event);
    }

  public boolean onSingleTap(MotionEvent e) {
    script.tappedObject = true;
    if ( script.currentPickedObject != null) {
      //String text = "Tapped " + script.currentPickedObject.getName();
          //script.myTouchSensor(text, 4);

            for (GVRAnimation animation : script.mAnimations) {
                animation.start(script.mAnimationEngine);
            }
            //script.mAnimations = null;

    }
    else {
      String text = "Last action: TAP over non-interactive";
          script.myTouchSensor(text, 1);
    }
        //GazeController.enableInteractiveCursor();
    return false;
  }

  public void onLongPress(MotionEvent e) {
    String text = "Long Press " + e.getAction();
        script.myTouchSensor(text, 2);
        Log.v("", "onLongPress");
  }

  @Override
  public boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection, float velocityX, float velocityY) {
    String text = "Swipe; direction " + swipeDirection + ", " + e.getAction();
        script.myTouchSensor(text, 3);
        Log.v("", "onSwipe");
    return false;
  }
}
