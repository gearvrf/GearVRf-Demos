/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.videoplayer.focus;

import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

public class PickEventHandler extends GVREventListeners.PickEvents {

    private static final String TAG = PickEventHandler.class.getSimpleName();

    @Override
    public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        if (sceneObject instanceof Focusable) {
            Log.d(TAG, "onEnter: " + sceneObject.getName());
            ((Focusable) sceneObject).gainFocus();
        }
    }

    @Override
    public void onExit(GVRSceneObject sceneObject) {

        if (sceneObject instanceof Focusable) {
            Log.d(TAG, "onExit: " + sceneObject.getName());
            ((Focusable) sceneObject).loseFocus();
        }
    }
}