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

package org.gearvrf.ragdoll;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.physics.GVRPhysicsLoader;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.utility.Log;

import java.util.EnumSet;

public class RagdollMain extends GVRMain {
    private final static String TAG = "RagDoll";
    private final static float CURSOR_DEPTH = 6.0f;

    private GVRCursorController mCursorController = null;
    private GVRSceneObject mCursor = null;
    private TouchHandler mTouchHandler = null;
    private GVRWorld mWorld = null;

    public RagdollMain() {}

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        super.onInit(gvrContext);

        final GVRScene scene = gvrContext.getMainScene();

        scene.setBackgroundColor(0,0,0,0);

        Log.d(TAG, "Loading Rag Doll mesh...");
        GVRSceneObject model = gvrContext.getAssetLoader().loadModel("models/ragdoll.fbx", scene);

        model.getTransform().setPosition(0,0, -3);
        scene.addSceneObject(model);

        mWorld = new GVRWorld(gvrContext);
        mWorld.setGravity(0f, -1f, 0f);
        scene.getRoot().attachComponent(mWorld);

        Log.d(TAG, "Loading Rag Doll physics...");
        GVRPhysicsLoader.loadPhysicsFile(gvrContext,
                "models/ragdoll.bullet", true, scene);

        initCursorController(gvrContext);
    }

    /**
     * Initialize GearVR controller handler.
     *
     * @param gvrContext GVRf context.
     */
    private void initCursorController(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getMainScene();
        mTouchHandler = new TouchHandler();

        scene.getEventReceiver().addListener(mTouchHandler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * CURSOR_DEPTH,
                        0.2f * CURSOR_DEPTH),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mTouchHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-CURSOR_DEPTH);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    private class TouchHandler extends GVREventListeners.TouchEvents {
        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchStart(sceneObj, collision);

            mWorld.startDrag(sceneObj,
                    collision.hitLocation[0], collision.hitLocation[1], collision.hitLocation[2]);
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);
            mWorld.stopDrag();
        }
    }
}
