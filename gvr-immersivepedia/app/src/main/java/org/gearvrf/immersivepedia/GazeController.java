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

package org.gearvrf.immersivepedia;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSwitch;

public class GazeController {
    private GVRSceneObject cursorRoot;
    private GVRSceneObject cursor;
    private GVRSceneObject highlightCursor;
    private GVRSwitch cursorSelector;

    private static float NORMAL_CURSOR_SIZE = 0.5f;
    private static float HIGHLIGHT_CURSOR_SIZE = 0.6f;
    private static float CURSOR_Z_POSITION = -9.0f;

    private static int CURSOR_RENDER_ORDER = GVRRenderData.GVRRenderingOrder.OVERLAY + 10;
    private static GazeController mSingleton = null;

    public  GazeController(GVRCursorController controller) {
        GVRContext gvrContext = controller.getGVRContext();

        cursorRoot = new GVRSceneObject(gvrContext);
        cursorSelector = new GVRSwitch(gvrContext);
        cursorRoot.attachComponent(cursorSelector);
        cursor = new GVRSceneObject(gvrContext,
                                    gvrContext.createQuad(NORMAL_CURSOR_SIZE,
                                                          NORMAL_CURSOR_SIZE),
                                    gvrContext.getAssetLoader().loadTexture(
                                            new GVRAndroidResource(gvrContext,
                                                                   R.drawable.head_tracker)));
        cursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().disableLight();
        cursor.getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);
        cursor.getRenderData().setAlphaBlend(true);
        cursor.setName("CursorModel");
        cursorRoot.addChildObject(cursor);
        highlightCursor = new GVRSceneObject(gvrContext,
                                             gvrContext.createQuad(HIGHLIGHT_CURSOR_SIZE,
                                                                   HIGHLIGHT_CURSOR_SIZE),
                                             gvrContext.getAssetLoader().loadTexture(
                                                     new GVRAndroidResource(gvrContext,
                                                                            R.drawable.highlightcursor)));
        highlightCursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        highlightCursor.getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);
        highlightCursor.getRenderData().setDepthTest(false);
        highlightCursor.getRenderData().setAlphaBlend(true);
        highlightCursor.getRenderData().disableLight();
        highlightCursor.setName("Highlight");
        highlightCursor.getRenderData().getMaterial().setOpacity(0f);
        cursorRoot.addChildObject(highlightCursor);
        cursorRoot.setName("CursorRoot");
        controller.setCursor(cursorRoot);
        mSingleton = this;
    }

    public static GazeController get() {
        return mSingleton;
    }

    public GVRSceneObject getCursor()
    {
       return cursorRoot;
    }

    public void enableInteractiveCursor() {
        cursorSelector.setSwitchIndex(1);
    }

    public void disableInteractiveCursor() {
        cursorSelector.setSwitchIndex(0);
    }

    public void enableGaze() {
        cursorRoot.setEnable(true);
    }

    public void disableGaze() {
        cursorRoot.setEnable(false);
    }

}
