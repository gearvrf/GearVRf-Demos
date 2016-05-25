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
package org.gearvrf.io;

import java.util.Random;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRTextView;
import org.gearvrf.utility.Log;

public class InputScript extends GVRScript implements CursorControllerListener {
    private static final String TAG = InputScript.class.getSimpleName();

    private static final String SELECT_TEXT = "Move the cursor till the cube turns red";
    private static final String BUTTON_SELECT_TEXT = "Press and hold down a button to select";
    private static final String MOVE_CUBE_TEXT = "Now move the cube";

    private static final float[] INITIAL_CURSOR_POSITION = { 0.0f, 0.0f,
            -7.0f };
    private static final float NEAR_DEPTH = -2.0f;
    private static final float FAR_DEPTH = -30.0f;

    private static final float CUBE_WIDTH = 200.0f;

    private GVRContext gvrContext = null;
    private CustomShaderManager shaderManager;
    private GVRScene mainScene;

    // FPS variables
    private int frames = 0;
    private long startTimeMillis = 0;
    private final long interval = 100;

    private Random random;
    private final GVRTextView textView;

    public InputScript(GVRTextView textView) {
        this.textView = textView;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        random = new Random();
        mainScene = gvrContext.getNextMainScene();
        shaderManager = new CustomShaderManager(gvrContext);
        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f,
                1.0f, 1.0f, 1.0f);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f,
                1.0f, 1.0f, 1.0f);

        addSurroundings(gvrContext, mainScene);

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();

        inputManager.addCursorControllerListener(this);

        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            onCursorControllerAdded(cursor);
        }

        // set the default text
        setTextOnMainThread(SELECT_TEXT);

        GVRViewSceneObject text = new GVRViewSceneObject(gvrContext, textView,
                gvrContext.createQuad(30.0f, 20.0f));
        text.getTransform().setPosition(0.0f, 17.0f, -35.0f);
        text.getRenderData().setRenderingOrder(10002);

        mainScene.addSceneObject(text);

        GVRSceneObject cube1 = new Cube(gvrContext, "Cube 1", shaderManager);
        cube1.getTransform().setPosition(0.0f, 1.0f, -10.0f);
        mainScene.addSceneObject(cube1);
        // add the sensor to the cube
        addSensor(cube1);

        GVRSceneObject cube2 = new Cube(gvrContext, "Cube 2", shaderManager);
        cube2.getTransform().setPosition(0.0f, 1.0f, -12.0f);
        cube2.getTransform().rotateByAxisWithPivot(-15.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        cube2.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        mainScene.addSceneObject(cube2);
        // add the sensor to the cube
        addSensor(cube2);

        GVRSceneObject cube3 = new Cube(gvrContext, "Cube 3", shaderManager);
        cube3.getTransform().setPosition(0.0f, 1.0f, -15.0f);
        cube3.getTransform().rotateByAxisWithPivot(15.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        cube3.getTransform().rotateByAxisWithPivot(15.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f);
        cube3.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        mainScene.addSceneObject(cube3);
        // add the sensor to the cube
        addSensor(cube3);

    }

    private void addSensor(GVRSceneObject sceneObject) {
        CubeSensor sensor = new CubeSensor(gvrContext);
        sceneObject.setSensor(sensor);
        sceneObject.getEventReceiver().addListener(sensor);
    }

    private class CubeSensor extends GVRBaseSensor implements ISensorEvents {
        private GVRSceneObject selected;
        private int selectedCursorId;

        public CubeSensor(GVRContext gvrContext) {
            super(gvrContext);
        }

        @Override
        public void onSensorEvent(SensorEvent event) {
            GVRSceneObject cursor = event.getCursorController()
                    .getSceneObject();

            int id = event.getCursorController().getId();
            // Safe to assume that the returned object is a cube
            Cube cube = (Cube) event.getObject();
            if (cursor == null
                    || (selected != null && selectedCursorId != id)) {
                Log.d(TAG, "onSensorEvent Return null");
                return;
            }

            if (event.isActive() == false && selected != null
                    && id == selectedCursorId) {
                cursor.removeChildObject(selected);
                selected.getTransform().setPosition(
                        +cursor.getTransform().getPositionX()
                                + selected.getTransform().getPositionX(),
                        +cursor.getTransform().getPositionY()
                                + selected.getTransform().getPositionY(),
                        +cursor.getTransform().getPositionZ()
                                + selected.getTransform().getPositionZ());
                selected = null;
                selectedCursorId = -1;
            }

            if (event.isActive()) {
                if (cube.isColliding(cursor) && selected == null) {
                    selected = cube;
                    selectedCursorId = id;
                    event.getObject().getTransform()
                            .setPosition(-cursor.getTransform().getPositionX()
                                    + selected.getTransform().getPositionX(),
                            -cursor.getTransform().getPositionY()
                                    + selected.getTransform().getPositionY(),
                            -cursor.getTransform().getPositionZ()
                                    + selected.getTransform().getPositionZ());
                    cursor.addChildObject(selected);

                    cube.setGreen();
                    setTextOnMainThread(MOVE_CUBE_TEXT);
                }
            } else if (event.isOver()) {
                if (cube.isColliding(cursor)) {
                    cube.setRed();
                    setTextOnMainThread(BUTTON_SELECT_TEXT);
                } else {
                    cube.setGrey();
                    setTextOnMainThread(SELECT_TEXT);
                }
            } else {
                cube.setGrey();
                setTextOnMainThread(SELECT_TEXT);
            }
        }
    }

    @Override
    public void onStep() {
        // tick(); uncomment for FPS
    }

    private void tick() {
        ++frames;
        if (System.currentTimeMillis() - startTimeMillis >= interval) {
            Log.d(TAG, "FPS : " + frames / (interval / 1000.0f));
            frames = 0;
            startTimeMillis = System.currentTimeMillis();
        }
    }

    // The assets for the Cubemap are taken from the Samsung Developers website:
    // http://www.samsung.com/us/samsungdeveloperconnection/developer-resources/
    // gear-vr/apps-and-games/exercise-2-creating-the-splash-scene.html
    private void addSurroundings(GVRContext gvrContext, GVRScene scene) {
        FutureWrapper<GVRMesh> futureQuadMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));
        Future<GVRTexture> futureCubemapTexture = gvrContext
                .loadFutureCubemapTexture(
                        new GVRAndroidResource(gvrContext, R.raw.earth));

        GVRMaterial cubemapMaterial = new GVRMaterial(gvrContext,
                GVRMaterial.GVRShaderType.Cubemap.ID);
        cubemapMaterial.setMainTexture(futureCubemapTexture);

        // surrounding cube
        GVRSceneObject frontFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        frontFace.getRenderData().setMaterial(cubemapMaterial);
        frontFace.setName("front");
        scene.addSceneObject(frontFace);
        frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        backFace.getRenderData().setMaterial(cubemapMaterial);
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        leftFace.getRenderData().setMaterial(cubemapMaterial);
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        rightFace.getRenderData().setMaterial(cubemapMaterial);
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        topFace.getRenderData().setMaterial(cubemapMaterial);
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        bottomFace.getRenderData().setMaterial(cubemapMaterial);
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
    }

    @Override
    public void onCursorControllerAdded(final GVRCursorController controller) {
        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext);
        Future<GVRTexture> texture = gvrContext.loadFutureTexture(
                new GVRAndroidResource(gvrContext, R.raw.earthmap1k));

        GVRMaterial material = new GVRMaterial(gvrContext,
                shaderManager.getShaderId());

        // stay away from the darker colors for the cursor
        float r = random.nextFloat() / 2.0f + 0.5f;
        float g = random.nextFloat() / 2.0f + 0.5f;
        float b = random.nextFloat() / 2.0f + 0.5f;

        material.setVec3(CustomShaderManager.COLOR_KEY, r, g, b);
        material.setTexture(CustomShaderManager.TEXTURE_KEY, texture);
        material.setMainTexture(texture);
        Future<GVRMesh> futureCursorMesh = gvrContext.loadFutureMesh(
                new GVRAndroidResource(gvrContext, R.raw.cursor));

        GVRRenderData cursorRenderData = new GVRRenderData(gvrContext);
        cursorRenderData.setMesh(futureCursorMesh);
        cursorRenderData.setMaterial(material);
        sceneObject.attachRenderData(cursorRenderData);

        mainScene.addSceneObject(sceneObject);
        controller.setSceneObject(sceneObject);

        // set the initial cursor position
        controller.setPosition(INITIAL_CURSOR_POSITION[0],
                INITIAL_CURSOR_POSITION[1], INITIAL_CURSOR_POSITION[2]);

        // set the min cursor depth
        controller.setNearDepth(NEAR_DEPTH);

        // set the max cursor depth
        controller.setFarDepth(FAR_DEPTH);
    }

    @Override
    public void onCursorControllerRemoved(GVRCursorController controller) {
        GVRSceneObject object = controller.getSceneObject();
        mainScene.removeSceneObject(object);
    }

    void close() {
        gvrContext.getInputManager().removeCursorControllerListener(this);
    }

    private static class Cube extends GVRCubeSceneObject {
        public Cube(GVRContext gvrContext, String name,
                CustomShaderManager shaderManager) {
            super(gvrContext, true);
            Future<GVRTexture> texture = gvrContext.loadFutureTexture(
                    new GVRAndroidResource(gvrContext, R.raw.cube_texture));
            GVRMaterial material = getRenderData().getMaterial();
            material.setShaderType(shaderManager.getShaderId());
            setColor(material, 0.7f, 0.7f, 0.7f);
            material.setTexture(CustomShaderManager.TEXTURE_KEY, texture);
            getTransform().setScale(2.0f, 2.0f, 2.0f);
            setName(name);
        }

        void setRed() {
            setColor(1.0f, 0.0f, 0.0f);
        }

        void setGreen() {
            setColor(0.0f, 1.0f, 0.0f);
        }

        void setGrey() {
            setColor(0.7f, 0.7f, 0.7f);
        }

        void setColor(float r, float g, float b) {
            GVRMaterial material = getRenderData().getMaterial();
            material.setVec3(CustomShaderManager.COLOR_KEY, r, g, b);
        }

        void setColor(GVRMaterial material, float r, float g, float b) {
            material.setVec3(CustomShaderManager.COLOR_KEY, r, g, b);
        }
    }

    private static class CustomShaderManager {
        private final GVRCustomMaterialShaderId shaderId;
        public static final String TEXTURE_KEY = "texture";
        public static final String COLOR_KEY = "color";

        public CustomShaderManager(GVRContext gvrContext) {
            final GVRMaterialShaderManager shaderManager = gvrContext
                    .getMaterialShaderManager();
            shaderId = shaderManager.addShader(R.raw.vertex, R.raw.fragment);
            GVRMaterialMap customShader = shaderManager.getShaderMap(shaderId);
            customShader.addUniformVec3Key("u_color", COLOR_KEY);
            customShader.addTextureKey("u_texture", TEXTURE_KEY);
        }

        public GVRCustomMaterialShaderId getShaderId() {
            return shaderId;
        }
    }

    private void setTextOnMainThread(final String text) {
        gvrContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }
}