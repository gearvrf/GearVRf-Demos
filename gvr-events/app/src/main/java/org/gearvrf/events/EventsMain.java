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
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IActivityEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;

public class EventsMain extends GVRMain {
    private static final int KEY_EVENT = 1;
    private static final int MOTION_EVENT = 2;

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

    public EventsMain(EventsActivity activity,
                        final GVRFrameLayout frameLayout, final TextView keyTextView) {
        this.frameLayout = frameLayout;
        final String keyPressed = activity.getResources()
                .getString(R.string.keyCode);

        mainThreadHandler = new Handler(activity.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // dispatch motion event

                if (msg.what == MOTION_EVENT) {
                    MotionEvent motionEvent = (MotionEvent) msg.obj;
                    frameLayout.dispatchTouchEvent(motionEvent);
                    frameLayout.invalidate();
                    motionEvent.recycle();
                }

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
        mainScene = gvrContext.getMainScene();
        mainScene.addSceneObject(layoutSceneObject);

        layoutSceneObject.getTransform().setPosition(0.0f, 0.0f, DEPTH);

        frameWidth = frameLayout.getWidth();
        frameHeight = frameLayout.getHeight();

        // set up the input manager for the main scene
        gvrContext.getInputManager().addCursorControllerListener(listener);

        GVRBaseSensor sensor = new GVRBaseSensor(gvrContext);
        layoutSceneObject.attachComponent(sensor);

        mPicker = new GVRPicker(gvrContext, gvrContext.getMainScene());
        mPicker.getEventReceiver().addListener(GVRBaseSensor.getPickHandler());
        mPicker.getEventReceiver().addListener(mTouchManager);
        gvrContext.getActivity().getEventReceiver().addListener(mActivityEventsHandler);
    }

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
                        context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.raw.cursor)));
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mainScene.getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                controller.setPosition(0.0f, 0.0f, DEPTH);
                controller.setNearDepth(DEPTH);
                controller.setFarDepth(DEPTH);
                controller.setEnable(true);
            } else {
                // disable all other types
                controller.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerActive(GVRCursorController controller) {
        }
        @Override
        public void onCursorControllerInactive(GVRCursorController controller) {
        }
    };

    private IActivityEvents mActivityEventsHandler = new GVREventListeners.ActivityEvents()
    {
        @Override
        public void dispatchTouchEvent(MotionEvent event)
        {
            int action = event.getAction();
            boolean touched = (action == MotionEvent.ACTION_DOWN) ||
                    (action == MotionEvent.ACTION_MOVE);
            mPicker.processPick(touched, event);
        }
    };

    private ITouchEvents mTouchManager = new ITouchEvents()
    {
        @Override
        public void onTouchStart(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo)
        {
            sendEvent(pickInfo);
        }
        @Override
        public void onTouchEnd(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo)
        {
            sendEvent(pickInfo);
        }
        public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo)
        {
        }
        @Override
        public void onExit(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo)
        {
        }
        @Override
        public void onInside(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickInfo)
        {
        }
    };

    private void sendEvent(GVRPicker.GVRPickedObject pickInfo) {
        MotionEvent motionEvent = pickInfo.motionEvent;
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            pointerCoords.x = mSavedHitPointX
                    + ((motionEvent.getX() - mSavedMotionEventX) * SCALE);
            pointerCoords.y = mSavedHitPointY
                    + ((motionEvent.getY() - mSavedMotionEventY) * SCALE);
        } else {
            float[] hitPoint = pickInfo.getHitLocation();
            pointerCoords.x = ((hitPoint[0] + HALF_QUAD_X) / QUAD_X) * frameWidth;
            pointerCoords.y = (-(hitPoint[1] - HALF_QUAD_Y) / QUAD_Y) * frameHeight;

            if (motionEvent.getAction() == KeyEvent.ACTION_DOWN) {
                // save the co ordinates on down
                mSavedMotionEventX = motionEvent.getX();
                mSavedMotionEventY = motionEvent.getY();

                mSavedHitPointX = pointerCoords.x;
                mSavedHitPointY = pointerCoords.y;
            }
        }

        final MotionEvent clone = MotionEvent.obtain(
                motionEvent.getDownTime(), motionEvent.getEventTime(),
                motionEvent.getAction(), 1, pointerProperties,
                pointerCoordsArray, 0, 0, 1f, 1f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0);

        Message message = Message.obtain(mainThreadHandler, MOTION_EVENT, 0, 0, clone);
        mainThreadHandler.sendMessage(message);
    }

    private GVRPicker mPicker;
    private float mSavedMotionEventX, mSavedMotionEventY, mSavedHitPointX, mSavedHitPointY;

    private static final float SCALE = 10.0f;
}
