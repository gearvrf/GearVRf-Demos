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
package org.gearvrf.io.cursorsimple;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.io.cursor3d.MovableBehavior;
import org.gearvrf.io.cursor3d.SelectableBehavior;
import org.gearvrf.io.cursor3d.SelectableBehavior.ObjectState;
import org.gearvrf.io.cursor3d.SelectableBehavior.StateChangedListener;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
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
public class CursorMain extends GVRMain {
    private static final String TAG = CursorMain.class.getSimpleName();
    private static final String ASTRONAUT_MODEL = "Astronaut.fbx";
    private static final String ROCKET_MODEL = "Rocket.fbx";
    private GVRScene mainScene;
    private CursorManager cursorManager;
    private GVRSceneObject rocket;
    private GVRSceneObject astronaut;

    @Override
    public void onInit(GVRContext gvrContext) {
        mainScene = gvrContext.getMainScene();
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.BLACK);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.BLACK);
        List<IoDevice> devices = new ArrayList<IoDevice>();

        //_VENDOR_TODO_ register the devices with Cursor Manager here.
        /*
        TemplateDevice device1 = new TemplateDevice(gvrContext, "template_1", "Right controller");
        TemplateDevice device2 = new TemplateDevice(gvrContext, "template_2", "Left controller");
        devices.add(device1);
        devices.add(device2);
        */

        /*
        HandTemplateDevice device = new HandTemplateDevice(gvrContext, mainScene);
        devices.addAll(device.getDeviceList());
        */

        cursorManager = new CursorManager(gvrContext, mainScene, devices);
        GVRModelSceneObject astronautModel, rocketModel;

        float[] position = new float[]{5.0f, 0.0f, -20.0f};
        try {
            astronautModel = gvrContext.getAssetLoader().loadModel(ASTRONAUT_MODEL);
            rocketModel = gvrContext.getAssetLoader().loadModel(ROCKET_MODEL);
        } catch (IOException e) {
            Log.e(TAG, "Could not load the assets:", e);
            return;
        }

        astronaut = astronautModel.getChildByIndex(0);
        astronaut.getTransform().setPosition(position[0], position[1], position[2]);
        SelectableBehavior selectableBehavior = new SelectableBehavior(cursorManager, true);
        astronaut.attachComponent(selectableBehavior);
        astronautModel.removeChildObject(astronaut);
        mainScene.addSceneObject(astronaut);

        position[0] = -5.0f;
        MovableBehavior movableRocketBehavior = new MovableBehavior(cursorManager, new ObjectState[]{
                ObjectState.DEFAULT, ObjectState.BEHIND, ObjectState.COLLIDING, ObjectState.CLICKED});
        rocket = rocketModel.getChildByIndex(0);
        rocket.getTransform().setPosition(position[0], position[1], position[2]);
        rocket.attachComponent(movableRocketBehavior);
        rocketModel.removeChildObject(rocket);
        mainScene.addSceneObject(rocket);

        position[0] = 2.0f;
        position[1] = 2.0f;
        GVRCubeSceneObject cubeSceneObject = new GVRCubeSceneObject(gvrContext, true, gvrContext
                .getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,R.mipmap.ic_launcher)));
        cubeSceneObject.getTransform().setPosition(position[0], position[1], position[2]);
        MovableBehavior movableCubeBehavior = new MovableBehavior(cursorManager);
        cubeSceneObject.attachComponent(movableCubeBehavior);
        mainScene.addSceneObject(cubeSceneObject);

        movableCubeBehavior.setStateChangedListener(new StateChangedListener() {
            @Override
            public void onStateChanged(SelectableBehavior selectableBehavior, ObjectState prev,
                                       ObjectState current, Cursor cursor) {
                if (current == ObjectState.CLICKED) {
                    GVRTransform transform = astronaut.getTransform();
                    transform.setPositionZ(transform.getPositionZ() - 1);
                }
            }
        });

        addCustomMovableCube(gvrContext);
    }

    @Override
    public void onStep() {
    }

    void close() {
        if (cursorManager != null) {
            cursorManager.close();
        }

        //_VENDOR_TODO_ close the devices here
        //device.close();
        //device1.close();
        //device2.close();

    }

    private void addCustomMovableCube(GVRContext gvrContext) {
        GVRSceneObject root = new GVRSceneObject(gvrContext);
        GVRMaterial red = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        GVRMaterial blue = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        GVRMaterial green = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        GVRMaterial alphaRed = new GVRMaterial(gvrContext, new GVRShaderId(GVRPhongShader.class));
        red.setDiffuseColor(1, 0, 0, 1);
        blue.setDiffuseColor(0, 0, 1, 1);
        green.setDiffuseColor(0, 1, 0, 1);
        alphaRed.setDiffuseColor(1, 0, 0, 0.5f);

        GVRCubeSceneObject cubeDefault = new GVRCubeSceneObject(gvrContext, true, red);
        root.addChildObject(cubeDefault);

        GVRMesh cubeMesh = cubeDefault.getRenderData().getMesh();

        GVRSceneObject cubeColliding = new GVRSceneObject(gvrContext, cubeMesh);
        cubeColliding.getRenderData().setMaterial(blue);
        root.addChildObject(cubeColliding);

        GVRSceneObject cubeClicked = new GVRSceneObject(gvrContext, cubeMesh);
        cubeClicked.getRenderData().setMaterial(green);
        root.addChildObject(cubeClicked);

        GVRSceneObject cubeBehind = new GVRSceneObject(gvrContext, cubeMesh);
        cubeBehind.getRenderData().setMaterial(alphaRed);
        cubeBehind.getRenderData().getMaterial().setOpacity(0.5f);
        cubeBehind.getRenderData().setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        root.addChildObject(cubeBehind);

        MovableBehavior movableBehavior = new MovableBehavior(cursorManager, new ObjectState[] {
                ObjectState.DEFAULT, ObjectState.COLLIDING, ObjectState.CLICKED, ObjectState.BEHIND
        });
        float[] position = new float[]{-2, 2, -10};
        root.getTransform().setPosition(position[0], position[1], position[2]);
        root.attachComponent(movableBehavior);
        mainScene.addSceneObject(root);

        movableBehavior.setStateChangedListener(new StateChangedListener() {
            @Override
            public void onStateChanged(SelectableBehavior selectableBehavior, ObjectState prev,
                                       ObjectState current, Cursor cursor) {
                if(current == ObjectState.CLICKED) {
                    GVRTransform transform = astronaut.getTransform();
                    transform.setPositionZ(transform.getPositionZ() + 1);
                }
            }
        });
    }

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.mipmap.ic_launcher);
        // return the correct splash screen bitmap
        GVRTexture tex = new GVRTexture(gvrContext);
        tex.setImage(new GVRBitmapTexture(gvrContext, bitmap));
        return tex;
    }
}
