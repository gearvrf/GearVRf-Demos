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
import org.gearvrf.GVRMain;
import org.gearvrf.GVRShaderId;
import org.gearvrf.IScriptEvents;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.script.GVRScriptBundle;
import org.gearvrf.script.GVRScriptException;
import org.gearvrf.script.GVRScriptManager;

import java.io.IOException;

public class GearVRJavascriptMain extends GVRMain {
    private static final String TAG = GearVRJavascriptMain.class.getSimpleName();
    private static final float DEPTH = -1.5f;

    private GVRContext context;
    private GVRMaterial mShaderMaterial;
    private GVRShaderId mShaderID = null;
    private GVRSceneObject cursorModel = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        // The onInit function in script.js will be invoked

        gvrContext.startDebugServer();        

        context = gvrContext;
        gvrContext.getInputManager().selectController(listener);
    }

    @Override
    public void onStep() {
        // The onStep function in script.js will be invoked
    }

    private GVRSceneObject createCursor()
    {
        GVRSceneObject cursor = new GVRSphereSceneObject(context);
        GVRRenderData cursorRenderData = cursor.getRenderData();

        mShaderID = new GVRShaderId(CustomShaderManager.class);
        mShaderMaterial = new GVRMaterial(getGVRContext(), mShaderID);
        mShaderMaterial.setVec4(CustomShaderManager.COLOR_KEY, 1.0f, 0.0f, 0.0f, 0.5f);
        cursor.getTransform().setScale(-0.015f, -0.015f, -0.015f);
        cursorRenderData.setMaterial(mShaderMaterial);
        cursorRenderData.setDepthTest(false);
        cursorRenderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        cursorModel = cursor;
        return cursor;
    }

    private GVRInputManager.ICursorControllerSelectListener listener = new GVRInputManager.ICursorControllerSelectListener() {
        @Override
        public void onCursorControllerSelected(GVRCursorController controller, GVRCursorController oldController) {
            if (cursorModel == null)
            {
                createCursor();
            }
            controller.setCursor(cursorModel);
            controller.setCursorDepth(DEPTH);
            controller.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
        }
    };
}
