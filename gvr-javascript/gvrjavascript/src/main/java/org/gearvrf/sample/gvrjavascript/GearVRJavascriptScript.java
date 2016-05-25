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

package org.gearvrf.sample.gvrjavascript;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

public class GearVRJavascriptScript extends GVRScript {
    private static final String TAG = GearVRJavascriptScript.class.getSimpleName();
    private static final float DEPTH = -1.5f;

    private GVRContext context;
    private CustomShaderManager shaderManager;
    private GVRScene mainScene;

    @Override
    public void onInit(GVRContext gvrContext) {
        // The onInit function in script.js will be invoked

        gvrContext.startDebugServer();        

        context = gvrContext;
        mainScene = gvrContext.getNextMainScene();

        // shader for cursor
        shaderManager = new CustomShaderManager(gvrContext);

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(listener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            listener.onCursorControllerAdded(cursor);
        }
    }

    @Override
    public void onStep() {
        // The onStep function in script.js will be invoked
    }

    private CursorControllerListener listener = new CursorControllerListener() {
        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                controller.resetSceneObject();
                controller.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {
            // Only allow only gaze
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                GVRSceneObject cursor = new GVRSphereSceneObject(context);
                GVRRenderData cursorRenderData = cursor.getRenderData();
                GVRMaterial material = cursorRenderData.getMaterial();
                material.setShaderType(shaderManager.getShaderId());
                material.setVec4(CustomShaderManager.COLOR_KEY, 1.0f, 0.0f,
                        0.0f, 0.5f);
                mainScene.addSceneObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                controller.setSceneObject(cursor);
                controller.setPosition(0.0f, 0.0f, DEPTH);
                controller.setNearDepth(DEPTH);
                controller.setFarDepth(DEPTH);
                cursor.getTransform().setScale(-0.015f, -0.015f, -0.015f);
            } else {
                // disable all other types
                controller.setEnable(false);
            }
        }
    };
}
