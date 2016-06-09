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

package org.gearvrf.sample.remote_scripting;

import java.lang.Runnable;
import java.util.concurrent.Future;
import java.io.IOException;
import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRRenderData;

public class CursorUtils {
    private GVRContext gvrContext;
    private GVRSceneObject cursor;

    public CursorUtils(GVRContext context) {
        gvrContext = context;
    }

    public void show() {
        gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    // set up the input manager for the main scene
                    GVRInputManager inputManager = gvrContext.getInputManager();
                    inputManager.addCursorControllerListener(listener);
                    for (GVRCursorController cursor : inputManager.getCursorControllers()) {
                        listener.onCursorControllerAdded(cursor);
                    }
                }
            });
    }

    public void hide() {
        gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    // set up the input manager for the main scene
                    GVRInputManager inputManager = gvrContext.getInputManager();
                    inputManager.removeCursorControllerListener(listener);
                    for (GVRCursorController cursor : inputManager.getCursorControllers()) {
                        listener.onCursorControllerRemoved(cursor);
                    }
                }
            });
    }

    private CursorControllerListener listener = new CursorControllerListener() {

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                controller.resetSceneObject();
                controller.setEnable(false);
                gvrContext.getMainScene().getMainCameraRig().removeChildObject(cursor);
            }
        }

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {
            float DEPTH = -1.5f;

            // Only allow only gaze
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                if(cursor != null) {
                    gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
                    controller.setEnable(true);
                    controller.setPosition(0.0f, 0.0f, DEPTH);
                    controller.setNearDepth(DEPTH);
                    controller.setFarDepth(DEPTH);
                    return;
                }
                cursor = new GVRSceneObject(gvrContext, 
                        new FutureWrapper<GVRMesh>(gvrContext.createQuad(0.1f, 0.1f)),
                        gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.raw.cursor)));
                cursor.setName("cursor");
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                controller.setPosition(0.0f, 0.0f, DEPTH);
                controller.setNearDepth(DEPTH);
                controller.setFarDepth(DEPTH);
            } else {
                // disable all other types
                controller.setEnable(false);
            }
        }
    };

}


