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

package org.gearvrf.sample.controller;

import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;

public class SampleActivity extends GVRActivity {
    private SampleMain main;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        main = new SampleMain(this);
        setMain(main, "gvr.xml");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        main.dispatchTouch(event);
        return super.dispatchTouchEvent(event);
    }
}
