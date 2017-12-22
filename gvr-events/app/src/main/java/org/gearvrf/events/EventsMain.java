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

import android.widget.FrameLayout;

import org.gearvrf.GVRContext;

import org.gearvrf.io.GVRCursorController;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.util.EnumSet;

public class EventsMain extends GVRMain {
    private static final String TAG = EventsMain.class.getSimpleName();
    private static final int KEY_EVENT = 1;
    private static final int MOTION_EVENT = 2;

    private GVRViewSceneObject layoutSceneObject;
    private GVRContext context;
    private GVRScene mainScene;

    private static final float QUAD_X = 1.0f;
    private static final float QUAD_Y = 1.0f;
    private static final float DEPTH = -1.5f;
    private final FrameLayout frameLayout;


    public EventsMain(EventsActivity activity, final FrameLayout frameLayout) {
        this.frameLayout = frameLayout;
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        context = gvrContext;

        layoutSceneObject = new GVRViewSceneObject(gvrContext, frameLayout, QUAD_X, QUAD_Y);
        mainScene = gvrContext.getMainScene();
        mainScene.addSceneObject(layoutSceneObject);
        layoutSceneObject.getTransform().setPosition(0.0f, 0.0f, DEPTH);
        gvrContext.getInputManager().selectController();
    }

    @Override
    public void onStep() {
        // unused
    }
}
