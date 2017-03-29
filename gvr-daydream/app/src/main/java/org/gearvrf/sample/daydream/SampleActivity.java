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

package org.gearvrf.sample.daydream;

import android.os.Bundle;

import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.ControllerManager;

import org.gearvrf.GVRActivity;

public class SampleActivity extends GVRActivity {

    private ControllerManager controllerManager;
    private Controller controller;
    private SampleMain main;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        EventListener listener = new EventListener();
        controllerManager = new ControllerManager(this, listener);

        controller = controllerManager.getController();
        controller.setEventListener(listener);
        main = new SampleMain(this, controller);
        setMain(main, "gvr.xml");
    }

    @Override
    protected void onStart() {
        super.onStart();
        controllerManager.start();
    }

    @Override
    protected void onStop() {
        controllerManager.stop();
        super.onStop();
    }

    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener {

        @Override
        public void onApiStatusChanged(int i) {
        }

        @Override
        public void onRecentered() {
        }

        @Override
        public void onUpdate() {
            controller.update();
            main.updatePivot();
        }
    }
}
