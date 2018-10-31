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

package org.gearvrf.events;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRApplication;

public class EventsActivity extends Activity {
    private GVRApplication application;
    private EventsMain main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        main = new EventsMain();
        application = new GVRApplication(this, main, "gvr.xml");


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        main.onKeyDown(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        application.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        application.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        application.destroy();
        super.onDestroy();
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (application.dispatchGenericMotionEvent(event)) {
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (application.dispatchTouchEvent(event)) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

}