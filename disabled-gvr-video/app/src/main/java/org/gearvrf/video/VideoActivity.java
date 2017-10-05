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
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;

public class VideoActivity extends GVRActivity {
    private VideoMain mMain = null;
    private long lastDownTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMain = new VideoMain(this);
        setMain(mMain, "gvr.xml");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMain.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        mMain.onTouch();
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (event.getEventTime() - lastDownTime < 200) {
                mMain.onTap();
            }
        }
        return true;
    }
}
