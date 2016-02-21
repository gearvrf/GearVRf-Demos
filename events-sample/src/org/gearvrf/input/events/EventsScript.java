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

package org.gearvrf.input.events;

import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRCursorType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;
import org.gearvrf.utility.Log;

import android.os.Handler;
import android.os.Message;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.widget.TextView;

public class EventsScript extends GVRScript {
    private static final String TAG = EventsScript.class.getSimpleName();
    private final EventsActivity activity;
    private GVRViewSceneObject layoutSceneObject;
    private CustomShaderManager shaderManager;
    private GVRContext context;

    private static final float QUAD_X = 1.0f;
    private static final float QUAD_Y = 1.0f;
    private static final float HALF_QUAD_X = QUAD_X / 2.0f;
    private static final float HALF_QUAD_Y = QUAD_Y / 2.0f;
    private static final float DEPTH = -2.0f;

    private final GVRFrameLayout frameLayout;
    private int frameWidth;
    private int frameHeight;
    private GVRScene mainScene;
    private Handler mainThreadHandler;
    private final static PointerProperties[] pointerProperties;
    private final static PointerCoords[] pointerCoordsArray;
    private final static PointerCoords pointerCoords;

    static {
        PointerProperties properties = new PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        pointerProperties = new PointerProperties[] { properties };
        pointerCoords = new PointerCoords();
        pointerCoordsArray = new PointerCoords[] { pointerCoords };
    }

    public EventsScript(EventsActivity activity,
            final GVRFrameLayout frameLayout, final TextView keyTextView) {
        this.activity = activity;
        this.frameLayout = frameLayout;
        final String keyPressed = activity.getResources()
                .getString(R.string.keyCode);
        mainThreadHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // dispatch motion event
                MotionEvent motionEvent = (MotionEvent) msg.obj;
                int keyCode = msg.arg1;
                frameLayout.dispatchTouchEvent(motionEvent);
                motionEvent.recycle();
                keyTextView.setText(String.format("%s %s ", keyPressed,
                        KeyEvent.keyCodeToString(keyCode)));
            }
        };

    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        context = gvrContext;
        shaderManager = new CustomShaderManager(gvrContext);

        layoutSceneObject = new GVRViewSceneObject(gvrContext, frameLayout,
                context.createQuad(QUAD_X, QUAD_Y));
        mainScene = gvrContext.getNextMainScene();
        mainScene.addSceneObject(layoutSceneObject);

        layoutSceneObject.getTransform().setPosition(0.0f, 0.0f, DEPTH);

        frameWidth = frameLayout.getWidth();
        frameHeight = frameLayout.getHeight();

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(listener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            listener.onCursorControllerAdded(cursor);
        }
        GVRBaseSensor sensor = new GVRBaseSensor(gvrContext);
        sensor.registerSensorEventListener(eventListener);
        layoutSceneObject.setSensor(sensor);
    }

    private ISensorEvents eventListener = new ISensorEvents() {

        @Override
        public void onSensorEvent(SensorEvent event) {
            KeyEvent keyEvent = event.getCursorController().getKeyEvent();
            if (keyEvent == null) {
                return;
            }
            MotionEvent motionEvent = getMotionEvent(event.getHitPoint(),
                    keyEvent.getDownTime(), keyEvent.getAction());
            Message message = Message.obtain(mainThreadHandler, 0,
                    keyEvent.getKeyCode(), 0, motionEvent);
            mainThreadHandler.sendMessage(message);
        }
    };

    private MotionEvent getMotionEvent(float[] hitPoint, long time,
            int action) {
        pointerCoords.x = (hitPoint[0] + HALF_QUAD_X) * frameWidth;
        pointerCoords.y = -(hitPoint[1] - HALF_QUAD_Y) * frameHeight;
        MotionEvent event = MotionEvent.obtain(time, time, action, 1,
                pointerProperties, pointerCoordsArray, 0, 0, 1f, 1f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0);
        return event;
    }

    private CursorControllerListener listener = new CursorControllerListener() {

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getCursorType() == GVRCursorType.GAZE) {
                controller.resetSceneObject();
                controller.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {
            // Only allow only gaze
            if (controller.getCursorType() == GVRCursorType.GAZE) {
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

    @Override
    public void onStep() {
        // unused
    }
}
