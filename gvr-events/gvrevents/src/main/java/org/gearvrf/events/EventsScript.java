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

import android.os.Handler;
import android.os.Message;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.widget.TextView;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;

public class EventsScript extends GVRScript {
    private static final String TAG = EventsScript.class.getSimpleName();
    private static final int KEY_EVENT = 1;

    private GVRViewSceneObject layoutSceneObject;
    private GVRContext context;

    private static final float QUAD_X = 1.0f;
    private static final float QUAD_Y = 1.0f;
    private static final float HALF_QUAD_X = QUAD_X / 2.0f;
    private static final float HALF_QUAD_Y = QUAD_Y / 2.0f;
    private static final float DEPTH = -1.5f;

    private final GVRFrameLayout frameLayout;

    private int frameWidth;
    private int frameHeight;

    private GVRScene mainScene;
    private Handler mainThreadHandler;
    private final static PointerProperties[] pointerProperties;
    private final static PointerCoords[] pointerCoordsArray;
    private final static PointerCoords pointerCoords;
    private GVRSceneObject cursor;

    static {
        PointerProperties properties = new PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        pointerProperties = new PointerProperties[]{properties};
        pointerCoords = new PointerCoords();
        pointerCoordsArray = new PointerCoords[]{pointerCoords};
    }

    public EventsScript(EventsActivity activity,
                        final GVRFrameLayout frameLayout, final TextView keyTextView) {
        this.frameLayout = frameLayout;
        final String keyPressed = activity.getResources()
                .getString(R.string.keyCode);

        mainThreadHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // dispatch motion event
                MotionEvent motionEvent = (MotionEvent) msg.obj;
                frameLayout.dispatchTouchEvent(motionEvent);
                frameLayout.invalidate();
                motionEvent.recycle();

                if (msg.what == KEY_EVENT) {
                    int keyCode = msg.arg1;
                    keyTextView.setText(String.format("%s %s ", keyPressed,
                            KeyEvent.keyCodeToString(keyCode)));
                }
            }
        };
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        context = gvrContext;

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
        layoutSceneObject.getEventReceiver().addListener(eventListener);
        layoutSceneObject.setSensor(sensor);
    }

    private ISensorEvents eventListener = new ISensorEvents() {
        private static final float SCALE = 10.0f;
        private float savedMotionEventX, savedMotionEventY, savedHitPointX,
                savedHitPointY;

        @Override
        public void onSensorEvent(SensorEvent event) {

            final MotionEvent motionEvent = event.getCursorController()
                    .getMotionEvent();
            if (motionEvent != null
                    && motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                pointerCoords.x = savedHitPointX
                        + ((motionEvent.getX() - savedMotionEventX) * SCALE);
                pointerCoords.y = savedHitPointY
                        + ((motionEvent.getY() - savedMotionEventY) * SCALE);

                final MotionEvent clone = MotionEvent.obtain(
                        motionEvent.getDownTime(), motionEvent.getEventTime(),
                        motionEvent.getAction(), 1, pointerProperties,
                        pointerCoordsArray, 0, 0, 1f, 1f, 0, 0,
                        InputDevice.SOURCE_TOUCHSCREEN, 0);

                Message message = Message.obtain(mainThreadHandler, 0, 0, 0,
                        clone);
                mainThreadHandler.sendMessage(message);
            } else {
                KeyEvent keyEvent = event.getCursorController().getKeyEvent();

                if (keyEvent == null) {
                    return;
                }

                float[] hitPoint = event.getHitPoint();
                pointerCoords.x = ((hitPoint[0] + HALF_QUAD_X) / QUAD_X) * frameWidth;
                pointerCoords.y = (-(hitPoint[1] - HALF_QUAD_Y) / QUAD_Y) * frameHeight;

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (motionEvent != null) {
                        // save the co ordinates on down
                        savedMotionEventX = motionEvent.getX();
                        savedMotionEventY = motionEvent.getY();
                    }
                    savedHitPointX = pointerCoords.x;
                    savedHitPointY = pointerCoords.y;
                }

                MotionEvent clone = getMotionEvent(keyEvent.getDownTime(),
                        keyEvent.getAction());

                Message message = Message.obtain(mainThreadHandler, KEY_EVENT,
                        keyEvent.getKeyCode(), 0, clone);
                mainThreadHandler.sendMessage(message);
            }
        }

        private MotionEvent getMotionEvent(long time, int action) {
            MotionEvent event = MotionEvent.obtain(time, time, action, 1,
                    pointerProperties, pointerCoordsArray, 0, 0, 1f, 1f, 0, 0,
                    InputDevice.SOURCE_TOUCHSCREEN, 0);
            return event;
        }
    };

    private CursorControllerListener listener = new CursorControllerListener() {

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                if (cursor != null) {
                    mainScene.getMainCameraRig().removeChildObject(cursor);
                }
                controller.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {
            // Only allow only gaze
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                cursor = new GVRSceneObject(context,
                        new FutureWrapper<GVRMesh>(context.createQuad(0.1f, 0.1f)),
                        context.loadFutureTexture(new GVRAndroidResource(context, R.raw.cursor)));
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mainScene.getMainCameraRig().addChildObject(cursor);
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

    @Override
    public void onStep() {
        // unused
    }
}
