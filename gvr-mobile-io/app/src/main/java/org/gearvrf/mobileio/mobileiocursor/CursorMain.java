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
package org.gearvrf.mobileio.mobileiocursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.gvrf.io.gvrmobileiodevice.GVRMobileIoDevice;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorEvent;
import org.gearvrf.io.cursor3d.CursorEventListener;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.io.cursor3d.MovableBehavior;
import org.gearvrf.io.cursor3d.OutputEvent;
import org.gearvrf.io.cursor3d.SelectableBehavior;
import org.gearvrf.io.cursor3d.SelectableBehavior.ObjectState;
import org.gearvrf.io.cursor3d.SelectableBehavior.StateChangedListener;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample can be used with a Laser Cursor as well as an Object Cursor. By default the Object
 * Cursor is enabled. To switch to a Laser Cursor simply rename the "laser_cursor_settings.xml"
 * in the assets directory to "settings.xml"
 */
public class CursorMain extends GVRMain implements CursorEventListener {
    private static final String TAG = CursorMain.class.getSimpleName();
    private static final String ROCKET_MODEL = "Rocket.fbx";
    private GVRScene mainScene;
    private CursorManager cursorManager;
    private GVRSceneObject rocket;
    private GVRMobileIoDevice gvrMobileIoDevice;

    @Override
    public void onInit(GVRContext gvrContext) {
        mainScene = gvrContext.getNextMainScene();
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.BLACK);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.BLACK);
        gvrMobileIoDevice = new GVRMobileIoDevice(gvrContext);
        List<IoDevice> devices = new ArrayList<>(1);
        devices.add(gvrMobileIoDevice);
        cursorManager = new CursorManager(gvrContext, mainScene, devices);
        GVRModelSceneObject rocketModel;

        float[] position = new float[3];
        try {
            rocketModel = gvrContext.loadModel(ROCKET_MODEL);
        } catch (IOException e) {
            Log.e(TAG, "Could not load the assets:", e);
            return;
        }

        position[0] = -1.0f;
        position[1] = 0.0f;
        position[2] = -15.0f;
        MovableBehavior movableRocketBehavior = new MovableBehavior(cursorManager, new ObjectState[]{
                ObjectState.DEFAULT, ObjectState.BEHIND, ObjectState.COLLIDING, ObjectState.CLICKED});
        rocket = rocketModel.getChildByIndex(0);
        rocket.getTransform().setPosition(position[0], position[1], position[2]);
        rocket.attachComponent(movableRocketBehavior);
        rocketModel.removeChildObject(rocket);
        mainScene.addSceneObject(rocket);
        movableRocketBehavior.setStateChangedListener(new StateChangedListener() {
            @Override
            public void onStateChanged(SelectableBehavior selectableBehavior, ObjectState objectState, ObjectState objectState1, Cursor cursor) {
                if(objectState1 == ObjectState.COLLIDING)
                {
                    OutputEvent outputEvent = new OutputEvent(GVRMobileIoDevice.Actions.ACTION_VIBRATE.ordinal());
                    cursor.handleOutputEvent(outputEvent);
                }
            }
        });

    }

    @Override
    public void onStep() {
    }

    void close() {
        if (cursorManager != null) {
            cursorManager.close();
        }
    }


    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.mipmap.ic_launcher);
        // return the correct splash screen bitmap
        return new GVRBitmapTexture(gvrContext, bitmap);
    }

    @Override
    public void onEvent(CursorEvent cursorEvent) {

    }
}
