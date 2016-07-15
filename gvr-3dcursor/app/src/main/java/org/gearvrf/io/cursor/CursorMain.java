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
package org.gearvrf.io.cursor;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;

import com.gearvrf.io.gearwear.GearWearableDevice;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.ZipLoader;
import org.gearvrf.io.cursor.AssetHolder.AssetObjectTuple;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorActivationListener;
import org.gearvrf.io.cursor3d.CursorEvent;
import org.gearvrf.io.cursor3d.CursorEventListener;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.CursorTheme;
import org.gearvrf.io.cursor3d.CursorType;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRTextView;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class CursorMain extends GVRMain {
    private static final String TAG = CursorMain.class.getSimpleName();

    private static final float CUBE_WIDTH = 200.0f;

    private static final float SCENE_DEPTH = -35.0f;
    private static final float SCENE_HEIGHT = 3.0f;
    private static final float SPACE_OBJECT_MARGIN = 30.0f;
    private static final float BUTTON_XORIENTATION = 30.0f;
    private static final float OBJECT_CURSOR_RESET_FACTOR = 0.8f;
    private static final float LASER_CURSOR_RESET_FACTOR = 0.4f;

    private final String RESET_TEXT;
    private final String SETTINGS_TEXT;

    private static final int NUM_TEXT_VIEWS = 11;
    private static final int INDEX_ROCKET = 0;
    private static final int INDEX_ASTRONAUT = 1;
    private static final int INDEX_STAR = 2;
    private static final int INDEX_LASER_BUTTON = 3;
    private static final int INDEX_CUBES_1 = 4;
    private static final int INDEX_CUBES_2 = 5;
    private static final int INDEX_HANDS_BUTTON = 6;
    private static final int INDEX_SWORDS = 7;
    private static final int INDEX_GEARS2_BUTTON = 8;
    private static final int INDEX_SPACE_COLLEC_1 = 9;
    private static final int INDEX_SPACE_COLLEC_2 = 10;

    private static final int STANDARD_TEXT_SIZE = 10;
    private static final int CIRCLE_TEXT_SIZE = 10;
    private static final int TEXT_VIEW_HEIGHT = 200;
    private static final int TEXT_VIEW_WIDTH = 400;
    private static final int CIRCLE_TEXT_VIEW_HEIGHT = 100;
    private static final int CIRCLE_TEXT_VIEW_WIDTH = 100;
    private static final float CIRCLE_TEXT_QUAD_HEIGHT = 2.0f;
    private static final float CIRCLE_TEXT_QUAD_WIDTH = 2.0f;
    private static final float TEXT_QUAD_HEIGHT = 4.0f;
    private static final float TEXT_QUAD_WIDTH = 8.0f;
    private static final float TEXT_POSITION_X = 0.0f;
    private static final float TEXT_POSITION_Y = -2.0f;
    private static final float TEXT_POSITION_Z = SCENE_DEPTH;
    private static final int TEXT_VIEW_V_PADDING = 10;
    private static final int TEXT_VIEW_H_PADDING = 20;
    private static final float FIRST_TEXT_VIEW_ROTATION = 60;
    private static final float CIRCLE_TEXT_ROTATION_OFFSET = 9f;
    private static final String GEARS2_DEVICE_ID = "gearwearable";
    private static final String GEARVR_DEVICE_ID = "gearvr";
    private static final String CRYSTAL_THEME = "crystal_sphere";
    private static final float SETTINGS_ROTATION_X = 10.0f;
    private static final float SETTINGS_ROTATION_Y = -40.0f;
    private static final float STAR_Y_ORIENTATION = -20.0f;
    private static final float CUBE_Y_ORIENTATION = -10.0f;
    final float SETTINGS_TEXT_OFFSET = -9.0f;
    private static int LIGHT_BLUE_COLOR;

    private static final String MESH_FILE = "meshes.zip";
    private static final String TEXTURE_FILE = "textures.zip";

    private static final float[] Y_AXIS = {0.0f, 1.0f, 0.0f};
    private final String[] STAR_MESHES, STAR_TEXTURES, ASTRONAUT_MESHES, ASTRONAUT_TEXTURES,
            ROCKET_SHIP_MESHES, ROCKET_SHIP_TEXTURES, SETTING_MESHES, SETTING_TEXTURES, CUBE_MESHES,
            CUBE_TEXTURES, CLOUD_1_MESHES, CLOUD_2_MESHES, CLOUD_3_MESHES, CLOUD_TEXTURES,
            BUTTON_MESHES, BUTTON_TEXTURES, SWORD_MESHES, SWORD_TEXTURES;

    private GVRContext gvrContext = null;
    private GVRScene mainScene;

    // FPS variables
    private int frames = 0;
    private long startTimeMillis = 0;
    private final long interval = 100;

    private CursorManager cursorManager;
    private final List<GVRTextView> textViewList;
    private final List<GVRTextView> circleTextViewList;
    private final GVRTextView resetTextView;
    private final GVRTextView settingsTextView;

    private Map<String, SpaceObject> objects;
    private List<CursorTheme> laserCursorThemes;
    private List<CursorTheme> pointCursorThemes;
    private CursorType currentType = CursorType.UNKNOWN;
    private List<Cursor> cursors;
    private String[] textViewStrings;
    private Map<String, Future<GVRMesh>> meshMap;
    private Map<String, Future<GVRTexture>> textureMap;
    private GearWearableDevice gearWearableDevice;

    public CursorMain(GVRActivity gvrActivity) {
        Resources resources = gvrActivity.getResources();
        LIGHT_BLUE_COLOR = resources.getColor(R.color.LIGHT_BLUE);
        objects = new HashMap<String, SpaceObject>();
        textViewList = new ArrayList<GVRTextView>(NUM_TEXT_VIEWS);
        for (int i = 0; i < NUM_TEXT_VIEWS; i++) {
            addGVRTextView(gvrActivity, i);
        }

        circleTextViewList = new ArrayList<GVRTextView>(NUM_TEXT_VIEWS);
        addCircleGVRTextViews(gvrActivity, resources);

        resetTextView = getTextView(gvrActivity, 0);
        resetTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        resetTextView.setGravity(Gravity.CENTER);
        settingsTextView = getTextView(gvrActivity, 0);
        settingsTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        settingsTextView.setGravity(Gravity.CENTER);

        textViewStrings = new String[]{
                resources.getString(R.string.activity_rocket),
                resources.getString(R.string.activity_astronaut),
                resources.getString(R.string.activity_star),
                resources.getString(R.string.activity_laser_button),
                resources.getString(R.string.activity_cubes_1),
                resources.getString(R.string.activity_cubes_2),
                resources.getString(R.string.activity_hand_button),
                resources.getString(R.string.activity_swords),
                resources.getString(R.string.activity_gears2),
                resources.getString(R.string.activity_space_collection_1),
                resources.getString(R.string.activity_space_collection_2)
        };

        RESET_TEXT = resources.getString(R.string.reset);
        SETTINGS_TEXT = resources.getString(R.string.settingsTitle);

        STAR_MESHES = resources.getStringArray(R.array.star_meshes);
        STAR_TEXTURES = resources.getStringArray(R.array.star_textures);
        CUBE_MESHES = resources.getStringArray(R.array.cube_meshes);
        CUBE_TEXTURES = resources.getStringArray(R.array.cube_textures);
        SETTING_MESHES = resources.getStringArray(R.array.setting_meshes);
        SETTING_TEXTURES = resources.getStringArray(R.array.setting_textures);
        ASTRONAUT_MESHES = resources.getStringArray(R.array.astronaut_meshes);
        ASTRONAUT_TEXTURES = resources.getStringArray(R.array.astronaut_textures);
        ROCKET_SHIP_MESHES = resources.getStringArray(R.array.rocket_ship_meshes);
        ROCKET_SHIP_TEXTURES = resources.getStringArray(R.array.rocket_ship_textures);
        CLOUD_1_MESHES = resources.getStringArray(R.array.cloud_1_meshes);
        CLOUD_2_MESHES = resources.getStringArray(R.array.cloud_2_meshes);
        CLOUD_3_MESHES = resources.getStringArray(R.array.cloud_3_meshes);
        CLOUD_TEXTURES = resources.getStringArray(R.array.cloud_textures);
        BUTTON_MESHES = resources.getStringArray(R.array.button_meshes);
        BUTTON_TEXTURES = resources.getStringArray(R.array.button_textures);
        SWORD_MESHES = resources.getStringArray(R.array.sword_meshes);
        SWORD_TEXTURES = resources.getStringArray(R.array.sword_textures);
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        mainScene = gvrContext.getNextMainScene();
        meshMap = new HashMap<String, Future<GVRMesh>>();
        textureMap = new HashMap<String, Future<GVRTexture>>();
        addSurroundings(gvrContext, mainScene);
        try {
            ZipLoader.load(gvrContext, MESH_FILE, new ZipLoader
                    .ZipEntryProcessor<Future<GVRMesh>>() {
                @Override
                public Future<GVRMesh> getItem(GVRContext context, GVRAndroidResource resource) {
                    Future<GVRMesh> mesh = context.loadFutureMesh(resource);
                    meshMap.put(resource.getResourceFilename(), mesh);
                    return mesh;
                }
            });

            ZipLoader.load(gvrContext, TEXTURE_FILE, new
                    ZipLoader
                            .ZipEntryProcessor<Future<GVRTexture>>() {

                        @Override
                        public Future<GVRTexture> getItem(GVRContext context, GVRAndroidResource
                                resource) {
                            Future<GVRTexture> texture = context.loadFutureTexture(resource);
                            textureMap.put(resource.getResourceFilename(), texture);
                            return texture;
                        }
                    });
        } catch (IOException e) {
            Log.e(TAG, "Error Loading textures/meshes");
        }

        AssetHolder starAssetHolder = getStarAssetHolder();
        AssetHolder cubeAssetHolder = getCubeAssetHolder();
        AssetHolder astronautAssetHolder = getAstronautAssetHolder();
        AssetHolder rocketShipAssetHolder = getRocketShipAssetHolder();
        AssetHolder settingAssetHolder = getSettingAssetHolder();
        AssetHolder cloud1AssetHolder = getCloud1AssetHolder();
        AssetHolder cloud2AssetHolder = getCloud2AssetHolder();
        AssetHolder cloud3AssetHolder = getCloud3AssetHolder();
        AssetHolder buttonAssetHolder = getButtonAssetHolder();
        AssetHolder swordAssetHolder = getSwordAssetHolder();

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
        gearWearableDevice = new GearWearableDevice(gvrContext, GEARS2_DEVICE_ID, "Gear Wearable");
        devices.add(gearWearableDevice);

        cursorManager = new CursorManager(gvrContext, mainScene, devices);
        List<CursorTheme> themes = cursorManager.getCursorThemes();
        laserCursorThemes = new ArrayList<CursorTheme>();
        pointCursorThemes = new ArrayList<CursorTheme>();
        for (CursorTheme cursorTheme : themes) {
            CursorType cursorType = cursorTheme.getCursorType();
            switch (cursorType) {
                case LASER:
                    laserCursorThemes.add(cursorTheme);
                    break;
                case OBJECT:
                    pointCursorThemes.add(cursorTheme);
                    break;
                default:
                    Log.d(TAG, "Theme with unknown CursorType");
                    break;
            }
        }

        for (int i = 0; i < NUM_TEXT_VIEWS; i++) {
            setTextOnMainThread(i, textViewStrings[i]);
            createTextViewSceneObject(i);
            createCircleTextViewSceneObject(i);
        }

        setTextOnMainThread(resetTextView, RESET_TEXT);
        setTextOnMainThread(settingsTextView, SETTINGS_TEXT);

        Vector3f position = new Vector3f();
        position.set(0.0f, SCENE_HEIGHT + 2.0f, SCENE_DEPTH);
        addSpaceObject(new SpaceObject(gvrContext, rocketShipAssetHolder, "rocketship", position,
                1.5f, getRotationFromIndex(INDEX_ROCKET), 0.0f));

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH + 5.0f);
        addSpaceObject(new MovableObject(gvrContext, starAssetHolder, "star1", position, 0.75f,
                getRotationFromIndex(INDEX_STAR), 0.0f, 0.0f, STAR_Y_ORIENTATION, 0.0f));
        position.set(0.0f, SCENE_HEIGHT - 0.5f, SCENE_DEPTH);
        addSpaceObject(new MovableObject(gvrContext, starAssetHolder, "star2", position, 1.5f,
                getRotationFromIndex(INDEX_STAR) - 1, 0.0f, 0.0f, STAR_Y_ORIENTATION, 0.0f));
        position.set(0.0f, SCENE_HEIGHT + 0.5f, SCENE_DEPTH - 5.0f);
        addSpaceObject(new MovableObject(gvrContext, starAssetHolder, "star3", position, 2.25f,
                getRotationFromIndex(INDEX_STAR) - 2, 0.0f, 0.0f, STAR_Y_ORIENTATION, 0.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new MovableObject(gvrContext, astronautAssetHolder, "astronaut", position,
                1.0f, getRotationFromIndex(INDEX_ASTRONAUT), 0.0f));

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH);
        addSpaceObject(new LaserCursorButton(gvrContext, buttonAssetHolder, "laser_button",
                position, 2f, getRotationFromIndex(INDEX_LASER_BUTTON), 0.0f));

        position.set(0.0f, SCENE_HEIGHT - 1.50f, SCENE_DEPTH);
        MovableObject object = new MovableObject(gvrContext, cubeAssetHolder, "cube1", position,
                1.8f, getRotationFromIndex(INDEX_CUBES_1) + 1.0f, 0, 0.0f, CUBE_Y_ORIENTATION,
                0.0f);
        addSpaceObject(object);

        position.set(0.0f, SCENE_HEIGHT - 0.85f, SCENE_DEPTH - 3.0f);
        object = new MovableObject(gvrContext, cubeAssetHolder, "cube2", position, 2.5f,
                getRotationFromIndex(INDEX_CUBES_1) - 0.2f, 0, 0.0f, CUBE_Y_ORIENTATION, 0.0f);
        addSpaceObject(object);

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH - 8.0f);
        object = new MovableObject(gvrContext, cubeAssetHolder, "cube3", position, 3.75f,
                getRotationFromIndex(INDEX_CUBES_1) - 1.3f, 0, 0.0f, CUBE_Y_ORIENTATION, 0.0f);
        addSpaceObject(object);

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH);
        addSpaceObject(new HandCursorButton(gvrContext, buttonAssetHolder, "hands_button",
                position, 2f, getRotationFromIndex(INDEX_HANDS_BUTTON), 0.0f));

        position.set(0, SCENE_HEIGHT, SCENE_DEPTH + 15.0f);
        MovableObject rightSword = new MovableObject(gvrContext, swordAssetHolder, "sword_1",
                position, 1.5f, getRotationFromIndex(INDEX_SWORDS) - 6.5f, 0, 0.0f, 80.0f, -5.5f);
        addSpaceObject(rightSword);

        position.set(0, SCENE_HEIGHT, SCENE_DEPTH + 15.0f);
        MovableObject leftSword = new MovableObject(gvrContext, swordAssetHolder, "sword_2",
                position, 1.5f, getRotationFromIndex(INDEX_SWORDS) + 6.5f, 0, 0.0f, -140.0f, -5.5f);
        addSpaceObject(leftSword);

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH);
        addSpaceObject(new GearS2Button(gvrContext, buttonAssetHolder, "gears2_button", position,
                2f, getRotationFromIndex(INDEX_GEARS2_BUTTON), -1.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new MovableObject(gvrContext, astronautAssetHolder,
                "collection_astronaut", position, 1.0f, getRotationFromIndex
                (INDEX_SPACE_COLLEC_1) + 3.5f, 0.0f));

        position.set(0.0f, SCENE_HEIGHT + 2.0f, SCENE_DEPTH - 2.5f);
        addSpaceObject(new MovableObject(gvrContext, rocketShipAssetHolder,
                "collection_rocketship", position, 1.25f, getRotationFromIndex
                (INDEX_SPACE_COLLEC_1), 0.0f));

        position.set(0.0f, SCENE_HEIGHT + 3.0f, SCENE_DEPTH - 5.0f);
        addSpaceObject(new MovableObject(gvrContext, starAssetHolder, "collection_star",
                position, 3f, getRotationFromIndex(INDEX_SPACE_COLLEC_1) - 5.0f, 0.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH - 15.0f);
        addSpaceObject(new MovableObject(gvrContext, cloud1AssetHolder, "cloud1", position,
                1.0f, 0.0f, +10.0f));

        addSpaceObject(new MovableObject(gvrContext, cloud2AssetHolder, "cloud2", position,
                1.0f, -15.0f, +3.0f));

        addSpaceObject(new MovableObject(gvrContext, cloud3AssetHolder, "cloud3", position,
                1.0f, +15.0f, +3.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new ResetButton(gvrContext, buttonAssetHolder, "reset", position, 2.5f,
                SETTINGS_ROTATION_X, SETTINGS_ROTATION_Y - 2.0f));
        GVRViewSceneObject resetText = new GVRViewSceneObject(gvrContext, resetTextView,
                gvrContext.createQuad(TEXT_QUAD_WIDTH, TEXT_QUAD_HEIGHT));
        resetText.getTransform().setPosition(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        resetText.getTransform().rotateByAxisWithPivot(SETTINGS_ROTATION_X, 0, 1, 0, 0, 0, 0);
        resetText.getTransform().rotateByAxisWithPivot(SETTINGS_ROTATION_Y +
                SETTINGS_TEXT_OFFSET, 1, 0, 0, 0, 0, 0);
        mainScene.addSceneObject(resetText);

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new SettingsObject(gvrContext, settingAssetHolder, "settings", position,
                2.0f, -SETTINGS_ROTATION_X, SETTINGS_ROTATION_Y, 0.0f));
        GVRViewSceneObject settingsText = new GVRViewSceneObject(gvrContext, settingsTextView,
                gvrContext.createQuad(TEXT_QUAD_WIDTH, TEXT_QUAD_HEIGHT));
        settingsText.getTransform().setPosition(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        settingsText.getTransform().rotateByAxisWithPivot(-SETTINGS_ROTATION_X, 0, 1, 0, 0, 0, 0);
        settingsText.getTransform().rotateByAxisWithPivot(SETTINGS_ROTATION_Y +
                SETTINGS_TEXT_OFFSET, 1, 0, 0, 0, 0, 0);
        mainScene.addSceneObject(settingsText);

        cursors = new ArrayList<Cursor>();
        cursorManager.addCursorActivationListener(activationListener);
    }

    private void createTextViewSceneObject(int index) {
        GVRViewSceneObject text = new GVRViewSceneObject(gvrContext, textViewList.get(index),
                gvrContext.createQuad(TEXT_QUAD_WIDTH, getTextQuadHeightFromIndex(index)));
        text.getTransform().setPosition(TEXT_POSITION_X, TEXT_POSITION_Y +
                getTextViewYOffsetFromIndex(index), TEXT_POSITION_Z);
        rotateTextViewSceneObject(text, getRotationFromIndex(index));
        mainScene.addSceneObject(text);
    }

    private void rotateTextViewSceneObject(GVRViewSceneObject text, float rotationX) {
        text.getTransform().rotateByAxisWithPivot(rotationX, Y_AXIS[0], Y_AXIS[1], Y_AXIS[2],
                0.0f, 0.0f, 0.0f);
        text.getRenderData().setRenderingOrder(10002);
    }

    private void createCircleTextViewSceneObject(int index) {
        GVRViewSceneObject text = new GVRViewSceneObject(gvrContext, circleTextViewList.get(index),
                gvrContext.createQuad(CIRCLE_TEXT_QUAD_WIDTH, CIRCLE_TEXT_QUAD_HEIGHT));
        text.getTransform().setPosition(TEXT_POSITION_X, TEXT_POSITION_Y +
                getTextViewYOffsetFromIndex(index), TEXT_POSITION_Z);
        rotateTextViewSceneObject(text, getRotationFromIndex(index) + CIRCLE_TEXT_ROTATION_OFFSET);
        mainScene.addSceneObject(text);
    }

    private float getRotationFromIndex(int index) {
        if (index > INDEX_SPACE_COLLEC_1) {
            index -= 2;
        } else if (index > INDEX_CUBES_1) {
            index--;
        }

        return FIRST_TEXT_VIEW_ROTATION - index * SPACE_OBJECT_MARGIN;
    }

    private float getTextViewYOffsetFromIndex(int index) {
        if (index == INDEX_CUBES_2 || index == INDEX_SPACE_COLLEC_2) {
            return -5.0f;
        } else if (index == INDEX_HANDS_BUTTON || index == INDEX_SWORDS) {
            return -2.0f;
        } else {
            return 0.0f;
        }
    }

    private int getTextViewHeightFromIndex(int index) {
        if (index == INDEX_HANDS_BUTTON) {
            return TEXT_VIEW_HEIGHT * 2;
        } else if (index == INDEX_CUBES_2 || index == INDEX_GEARS2_BUTTON) {
            return TEXT_VIEW_HEIGHT * 5 / 4;
        } else {
            return TEXT_VIEW_HEIGHT;
        }
    }

    private float getTextQuadHeightFromIndex(int index) {
        if (index == INDEX_HANDS_BUTTON) {
            return TEXT_QUAD_HEIGHT * 2;
        } else if (index == INDEX_CUBES_2 || index == INDEX_GEARS2_BUTTON) {
            return TEXT_QUAD_HEIGHT * 5 / 4;
        } else {
            return TEXT_QUAD_HEIGHT;
        }
    }

    private void addGVRTextView(GVRActivity gvrActivity, int index) {
        textViewList.add(getTextView(gvrActivity, index));
    }

    private GVRTextView getTextView(GVRActivity gvrActivity, int index) {
        Resources resources = gvrActivity.getResources();
        GVRTextView textView = new GVRTextView(gvrActivity, TEXT_VIEW_WIDTH,
                getTextViewHeightFromIndex(index));
        textView.setTextSize(STANDARD_TEXT_SIZE);
        textView.setTextColor(LIGHT_BLUE_COLOR);
        setTextViewProperties(textView, resources);
        return textView;
    }

    private void addCircleGVRTextViews(GVRActivity gvrActivity, Resources resources) {
        for (int count = 0; count < NUM_TEXT_VIEWS; count++) {
            GVRTextView textView = new GVRTextView(gvrActivity, CIRCLE_TEXT_VIEW_WIDTH,
                    CIRCLE_TEXT_VIEW_HEIGHT);
            textView.setTextSize(CIRCLE_TEXT_SIZE);
            textView.setTextColor(Color.BLACK);
            textView.setBackground(resources.getDrawable(R.drawable.circle));
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(count + 1));
            circleTextViewList.add(textView);
        }
    }

    private void setTextViewProperties(GVRTextView textView, Resources resources) {
        textView.setBackground(resources.getDrawable(R.drawable.rounded_corner));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setPadding(TEXT_VIEW_H_PADDING, TEXT_VIEW_V_PADDING, TEXT_VIEW_V_PADDING,
                TEXT_VIEW_V_PADDING);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
    }

    private void addSpaceObject(SpaceObject spaceObject) {
        GVRSceneObject sceneObject = spaceObject.getSceneObject();
        mainScene.addSceneObject(sceneObject);
        cursorManager.addSelectableObject(sceneObject);
        objects.put(sceneObject.getName(), spaceObject);
    }

    private void setTextOnMainThread(final int position, final String text) {
        setTextOnMainThread(textViewList.get(position), text);
    }

    private void setTextOnMainThread(final GVRTextView textView, final String text) {
        gvrContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    private CursorActivationListener activationListener = new CursorActivationListener() {

        @Override
        public void onDeactivated(Cursor cursor) {
            Log.d(TAG, "Cursor DeActivated:" + cursor.getName());
            synchronized (cursors) {
                cursors.remove(cursor);
            }
            cursor.removeCursorEventListener(cursorEventListener);
            for (SpaceObject spaceObject : objects.values()) {
                spaceObject.onCursorDeactivated(cursor);
            }
        }

        @Override
        public void onActivated(Cursor newCursor) {
            synchronized (cursors) {
                cursors.add(newCursor);
            }

            for (SpaceObject spaceObject : objects.values()) {
                spaceObject.onCursorActivated(newCursor);
            }
            newCursor.addCursorEventListener(cursorEventListener);
            List<IoDevice> ioDevices = newCursor.getAvailableIoDevices();
            if (currentType != newCursor.getCursorType()) {
                currentType = newCursor.getCursorType();
            }
            Log.d(TAG, "Available Io devices for Cursor are: ");
            for (int i = 0; i < ioDevices.size(); i++) {
                Log.d(TAG, "IO Device:" + ioDevices.get(i).getDeviceId());
            }
        }
    };

    private CursorEventListener cursorEventListener = new CursorEventListener() {

        @Override
        public void onEvent(CursorEvent event) {
            GVRSceneObject sceneObject = event.getObject();
            SpaceObject spaceObject = objects.get(sceneObject.getName());
            spaceObject.handleCursorEvent(event);
        }
    };

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
        frontFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        scene.addSceneObject(frontFace);
        frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        backFace.getRenderData().setMaterial(cubemapMaterial);
        backFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        leftFace.getRenderData().setMaterial(cubemapMaterial);
        leftFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        rightFace.getRenderData().setMaterial(cubemapMaterial);
        rightFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureQuadMesh,
                futureCubemapTexture);
        topFace.getRenderData().setMaterial(cubemapMaterial);
        topFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext,
                futureQuadMesh, futureCubemapTexture);
        bottomFace.getRenderData().setMaterial(cubemapMaterial);
        bottomFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
    }

    void close() {
        if (cursorManager != null) {
            cursorManager.close();
        }
        if (gearWearableDevice != null) {
            gearWearableDevice.close();
        } else {
            Log.d(TAG, "close: gearWearableDevice is null");
        }
        //_VENDOR_TODO_ close the devices here
        //device1.close();
        //device2.close();
    }

    private class SettingsObject extends SpaceObject {
        public SettingsObject(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY, float orientationX) {
            super(gvrContext, holder, name, position, scale, rotationX, rotationY, orientationX,
                    0.0f, 0.0f);
        }

        @Override
        void handleClickReleased(CursorEvent event) {
            Log.d(TAG, "handleClickReleased: show settings menu");
            cursorManager.setSettingsMenuVisibility(event.getCursor(), true);
        }
    }

    private void setOffsetPositionFromCursor(Vector3f position, Cursor cursor) {
        position.set(cursor.getPositionX(),cursor.getPositionY(),cursor.getPositionZ());
        if(cursor.getCursorType() == CursorType.OBJECT) {
            position.mul(OBJECT_CURSOR_RESET_FACTOR);
        } else {
            position.mul(LASER_CURSOR_RESET_FACTOR);
        }
    }

    private void setCursorPosition(Vector3f position, Cursor cursor) {
        if (cursor.getIoDevice()!= null && !cursor.getIoDevice().getDeviceId().equals
                (GEARVR_DEVICE_ID)) {
            cursor.setPosition(position.x, position.y, position.z);
        }
    }

    private class HandCursorButton extends SpaceObject {
        private List<Cursor> handCursors;
        private boolean currentIoDeviceUsed;
        private IoDevice currentIoDevice;
        private static final String RIGHT_HAND = "right_hand";
        private static final String LEFT_HAND = "left_hand";
        private CursorTheme rightHandTheme, leftHandTheme;
        private Vector3f leftCursorPosition, rightCursorPosition;

        public HandCursorButton(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(gvrContext, holder, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION, 0.0f, 0.0f);
            handCursors = new LinkedList<Cursor>();
            rightCursorPosition = new Vector3f();
            leftCursorPosition = new Vector3f();
            for (CursorTheme theme : cursorManager.getCursorThemes()) {
                if (theme.getId().equals(RIGHT_HAND)) {
                    rightHandTheme = theme;
                } else if (theme.getId().equals(LEFT_HAND)) {
                    leftHandTheme = theme;
                } else if (rightHandTheme != null && leftHandTheme != null) {
                    break;
                }
            }
        }

        @Override
        void handleClickReleased(CursorEvent event) {
            Cursor currentCursor = event.getCursor();
            currentIoDevice = currentCursor.getIoDevice();
            currentIoDeviceUsed = false;
            handCursors.clear();
            setOffsetPositionFromCursor(rightCursorPosition,currentCursor);
            leftCursorPosition.set(rightCursorPosition);

            if (currentCursor.getCursorType() == CursorType.LASER) ;
            {
                currentCursor.setEnable(false);
            }

            List<Cursor> activeCursors;
            synchronized (cursors) {
                activeCursors = new ArrayList<Cursor>(cursors);
            }
            enablePointCursors(activeCursors);
            if (handCursors.size() < 2) {
                enablePointCursors(cursorManager.getInactiveCursors());
            }

            boolean rightThemeAttached = false;
            if (currentIoDeviceUsed) {
                for (Cursor cursor : handCursors) {
                    if (cursor.getIoDevice() != null) {
                        if (cursor.getIoDevice() == currentIoDevice) {
                            setUpRightCursor(cursor);
                        } else {
                            setUpLeftCursor(cursor);
                        }
                    }
                }
            } else {
                for (Cursor cursor : handCursors) {
                    if (cursor.getCompatibleIoDevices().contains(currentIoDevice)) {
                        try {
                            cursor.attachIoDevice(currentIoDevice);
                        } catch (IOException e) {
                            Log.e(TAG, "IO device " + currentIoDevice.getName() + " cannot be " +
                                    "attached");
                        }
                        currentIoDeviceUsed = true;
                    }
                    if (cursor.getIoDevice() != null) {
                        if (!rightThemeAttached) {
                            setUpRightCursor(cursor);
                            rightThemeAttached = true;
                        } else {
                            setUpLeftCursor(cursor);
                        }
                    }
                }
            }
        }

        private void setUpLeftCursor(Cursor cursor) {
            cursor.setCursorTheme(leftHandTheme);
            setCursorPosition(leftCursorPosition, cursor);
        }

        private void setUpRightCursor(Cursor cursor) {
            cursor.setCursorTheme(rightHandTheme);
            setCursorPosition(rightCursorPosition, cursor);
        }

        private void enablePointCursors(List<Cursor> cursors) {
            for (Cursor cursor : cursors) {
                if (cursor.getCursorType() == CursorType.OBJECT) {
                    cursor.setEnable(true);
                    if (cursor.getIoDevice() == currentIoDevice) {
                        currentIoDeviceUsed = true;
                    }
                    handCursors.add(cursor);
                } else if (cursor.getCursorType() == CursorType.LASER) {
                    cursor.setEnable(false);
                }
            }
        }
    }

    private class LaserCursorButton extends SpaceObject {
        public LaserCursorButton(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(gvrContext, holder, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION, 0.0f, 0.0f);
        }

        @Override
        void handleClickReleased(CursorEvent event) {
            Cursor currentCursor = event.getCursor();
            Cursor otherCursor = null, laserCursor = null;
            if (currentCursor.getCursorType() == CursorType.LASER) {
                return;
            }

            IoDevice targetIoDevice = currentCursor.getIoDevice();
            List<Cursor> enabledCursors = cursorManager.getActiveCursors();
            for(Cursor inactiveCursor:cursorManager.getInactiveCursors()) {
                if(inactiveCursor.isEnabled()) {
                    enabledCursors.add(inactiveCursor);
                }
            }

            for (Cursor cursor : enabledCursors) {
                if (cursor.getCursorType() == CursorType.OBJECT && cursor.getIoDevice() == null) {
                    otherCursor = cursor;
                } else if (cursor.getCursorType() == CursorType.LASER) {
                    laserCursor = cursor;
                }
            }

            if (laserCursor == null) {
                for (Cursor cursor : cursorManager.getInactiveCursors()) {
                    if (cursor.getCursorType() == CursorType.LASER) {
                        laserCursor = cursor;
                        break;
                    }
                }
                if (laserCursor == null) {
                    Log.d(TAG, "No Laser cursor found");
                    return;
                }
            }

            if (!laserCursor.getCompatibleIoDevices().contains(targetIoDevice)) {
                Log.d(TAG, "Target IoDevice is not compatible with the laser cursor");
                return;
            }

            if (otherCursor != null) {
                otherCursor.setEnable(false);
            }
            currentCursor.setEnable(false);
            if (!laserCursor.isEnabled()) {
                laserCursor.setEnable(true);
            }

            if (laserCursor.getIoDevice() == targetIoDevice) {
                return;
            }

            if (!laserCursor.getAvailableIoDevices().contains(targetIoDevice)) {
                Log.d(TAG, "IoDevice not available in for the laser cursor");
                currentCursor.setEnable(true);
                if (otherCursor != null) {
                    otherCursor.setEnable(true);
                }
                return;
            }

            try {
                laserCursor.attachIoDevice(targetIoDevice);
            } catch (IOException e) {
                Log.e(TAG, "IO Device " + targetIoDevice.getName() + "cannot be attached");
            }
            if (otherCursor != null) {
                otherCursor.setEnable(true);
            }
        }
    }

    private class GearS2Button extends SpaceObject {
        Vector3f cursorPosition;
        public GearS2Button(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(gvrContext, holder, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION, 0.0f, 0.0f);
            cursorPosition = new Vector3f();
        }

        @Override
        void handleClickReleased(CursorEvent event) {
            Cursor currentCursor = event.getCursor();
            Cursor targetCursor = null;
            CursorTheme crystalTheme = null;
            setOffsetPositionFromCursor(cursorPosition, currentCursor);

            for (CursorTheme theme : cursorManager.getCursorThemes()) {
                if (theme.getId().equals(CRYSTAL_THEME)) {
                    crystalTheme = theme;
                }
            }

            if (currentCursor.getIoDevice().equals(gearWearableDevice)) {
                if(currentCursor.getCursorType() == CursorType.OBJECT) {
                    currentCursor.setCursorTheme(crystalTheme);
                }
                return;
            }

            List<Cursor> activeCursors;
            synchronized (cursors) {
                activeCursors = new ArrayList<Cursor>(cursors);
            }

            for (Cursor cursor : activeCursors) {
                if (cursor.isEnabled() && cursor.getCursorType() == CursorType.OBJECT &&
                        targetCursor == null) {
                    targetCursor = cursor;
                } else {
                    cursor.setEnable(false);
                }
            }

            if (targetCursor == null) {
                for (Cursor cursor : cursorManager.getInactiveCursors()) {
                    if (cursor.getCursorType() == CursorType.OBJECT) {
                        targetCursor = cursor;
                        targetCursor.setEnable(true);
                        break;
                    }
                }
            }

            if (targetCursor.getIoDevice() != gearWearableDevice && targetCursor
                    .getAvailableIoDevices().contains(gearWearableDevice)) {
                try {
                    targetCursor.attachIoDevice(gearWearableDevice);
                } catch (IOException e) {
                    Log.e(TAG, "IO Device " + gearWearableDevice.getName() + "cannot be attached");
                }
            }
            targetCursor.setCursorTheme(crystalTheme);
            setCursorPosition(cursorPosition, targetCursor);
        }
    }

    private class ResetButton extends SpaceObject {
        private static final String RESET_CURSOR_NAME = "Right Cursor";

        public ResetButton(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(gvrContext, holder, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION + 10.0f, 0.0f, 0.0f);
        }

        @Override
        void handleClickReleased(CursorEvent event) {
            for (SpaceObject spaceObject : objects.values()) {
                spaceObject.reset();
            }

            CursorTheme crystalTheme = null;

            for (CursorTheme theme : cursorManager.getCursorThemes()) {
                if (theme.getId().equals(CRYSTAL_THEME)) {
                    crystalTheme = theme;
                }
            }

            for (Cursor cursor : cursorManager.getActiveCursors()) {
                cursor.setEnable(false);
            }

            for (Cursor cursor : cursorManager.getInactiveCursors()) {
                if (cursor.getCursorType() == CursorType.OBJECT && cursor.getName().equals
                        (RESET_CURSOR_NAME)) {
                    cursor.setEnable(true);
                    cursor.setCursorTheme(crystalTheme);
                    break;
                }
            }
        }
    }

    class MovableObject extends SpaceObject {

        private Vector3f prevCursorPosition;
        private Quaternionf rotation;
        private Vector3f cross;

        private GVRSceneObject sceneObject;
        private GVRSceneObject selected;
        private GVRSceneObject selectedParent;
        private GVRSceneObject cursorSceneObject;
        private Cursor cursor;

        public MovableObject(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            this(gvrContext, holder, name, position, scale, rotationX, rotationY, 0.0f, 0.0f, 0.0f);
        }

        public MovableObject(GVRContext gvrContext, AssetHolder holder, String name, Vector3f
                position, float scale, float rotationX, float rotationY, float orientationX,
                             float orientationY, float orientationZ) {
            super(gvrContext, holder, name, position, scale, rotationX, rotationY, orientationX,
                    orientationY, orientationZ);
            initialize();
        }

        private void initialize() {
            prevCursorPosition = new Vector3f();
            rotation = new Quaternionf();
            cross = new Vector3f();
            sceneObject = getSceneObject();
        }

        @Override
        void handleClickEvent(CursorEvent event) {
            if (selected != null && cursor != event.getCursor()) {
                // We have a selected object but not the correct cursor
                return;
            }

            cursor = event.getCursor();
            cursorSceneObject = event.getCursor().getSceneObject();
            prevCursorPosition.set(cursor.getPositionX(), cursor.getPositionY(), cursor
                    .getPositionZ());
            selected = getSceneObject();
            selectedParent = selected.getParent();
            if (cursor.getCursorType() == CursorType.OBJECT) {
                Vector3f position = new Vector3f(cursor.getPositionX(), cursor.getPositionY(),
                        cursor
                                .getPositionZ());

                selected.getTransform().setPosition(-position.x + selected.getTransform()
                        .getPositionX(), -position.y + selected.getTransform()
                        .getPositionY(), -position.z + selected.getTransform().getPositionZ());
                selectedParent.removeChildObject(selected);
                cursorSceneObject.addChildObject(selected);
            }
        }

        @Override
        void handleDragEvent(CursorEvent event) {
            if (cursor.getCursorType() == CursorType.LASER && cursor == event.getCursor()) {
                Cursor cursor = event.getCursor();
                Vector3f cursorPosition = new Vector3f(cursor.getPositionX(), cursor.getPositionY
                        (), cursor.getPositionZ());
                rotateObjectToFollowCursor(cursorPosition);
                prevCursorPosition = cursorPosition;
            }
        }

        @Override
        void handleCursorLeave(CursorEvent event) {
            if (event.isActive() && cursor == event.getCursor()) {
                if (cursor.getCursorType() == CursorType.LASER) {
                    Vector3f cursorPosition = new Vector3f(cursor.getPositionX(), cursor
                            .getPositionY(), cursor.getPositionZ());
                    rotateObjectToFollowCursor(cursorPosition);
                    prevCursorPosition = cursorPosition;
                } else if (cursor.getCursorType() == CursorType.OBJECT) {
                    Log.d(TAG, "handleCursorLeave");
                    handleClickReleased(event);
                }
            }
        }

        @Override
        void handleClickReleased(CursorEvent event) {

            if (selected != null && cursor != event.getCursor()) {
                // We have a selected object but not the correct cursor
                return;
            }

            if (selected != null && cursor.getCursorType() == CursorType.OBJECT) {
                Vector3f position = new Vector3f(cursor.getPositionX(), cursor.getPositionY
                        (), cursor.getPositionZ());
                cursorSceneObject.removeChildObject(selected);
                selectedParent.addChildObject(selected);
                selected.getTransform().setPosition(+position.x + selected.getTransform()
                        .getPositionX(), +position.y + selected.getTransform()
                        .getPositionY(), +position.z + selected.getTransform().getPositionZ());
            }
            selected = null;
            selectedParent = null;
            // object has been moved, invalidate all other cursors to check for events
            for (Cursor remaining : cursorManager.getActiveCursors()) {
                if (cursor != remaining) {
                    remaining.invalidate();
                }
            }
        }

        private void rotateObjectToFollowCursor(Vector3f cursorPosition) {
            computeRotation(prevCursorPosition, cursorPosition);
            sceneObject.getTransform().rotateWithPivot(rotation.w, rotation.x, rotation.y,
                    rotation.z, 0,
                    0, 0);
            sceneObject.getTransform().setRotation(1, 0, 0, 0);
        }

        /*
        formulae for quaternion rotation taken from
        http://lolengine.net/blog/2014/02/24/quaternion-from-two-vectors-final
        */
        private void computeRotation(Vector3f start, Vector3f end) {
            float norm_u_norm_v = (float) Math.sqrt(start.dot(start) * end.dot(end));
            float real_part = norm_u_norm_v + start.dot(end);

            if (real_part < 1.e-6f * norm_u_norm_v) {
        /* If u and v are exactly opposite, rotate 180 degrees
         * around an arbitrary orthogonal axis. Axis normalisation
         * can happen later, when we normalise the quaternion. */
                real_part = 0.0f;
                if (Math.abs(start.x) > Math.abs(start.z)) {
                    cross = new Vector3f(-start.y, start.x, 0.f);
                } else {
                    cross = new Vector3f(0.f, -start.z, start.y);
                }
            } else {
                /* Otherwise, build quaternion the standard way. */
                start.cross(end, cross);
            }
            rotation.set(cross.x, cross.y, cross.z, real_part).normalize();
        }
    }

    private AssetHolder getStarAssetHolder() {
        return getAssetHolder(STAR_MESHES, STAR_TEXTURES);
    }

    private AssetHolder getCubeAssetHolder() {
        return getAssetHolder(CUBE_MESHES, CUBE_TEXTURES);
    }

    private AssetHolder getAstronautAssetHolder() {
        return getAssetHolder(ASTRONAUT_MESHES, ASTRONAUT_TEXTURES);
    }

    private AssetHolder getRocketShipAssetHolder() {
        return getAssetHolder(ROCKET_SHIP_MESHES, ROCKET_SHIP_TEXTURES);
    }

    private AssetHolder getSettingAssetHolder() {
        return getAssetHolder(SETTING_MESHES, SETTING_TEXTURES);
    }

    private AssetHolder getCloud1AssetHolder() {
        return getAssetHolder(CLOUD_1_MESHES, CLOUD_TEXTURES);
    }

    private AssetHolder getCloud2AssetHolder() {
        return getAssetHolder(CLOUD_2_MESHES, CLOUD_TEXTURES);
    }

    private AssetHolder getCloud3AssetHolder() {
        return getAssetHolder(CLOUD_3_MESHES, CLOUD_TEXTURES);
    }

    private AssetHolder getButtonAssetHolder() {
        return getAssetHolder(BUTTON_MESHES, BUTTON_TEXTURES);
    }

    private AssetHolder getSwordAssetHolder() {
        return getAssetHolder(SWORD_MESHES, SWORD_TEXTURES);
    }

    private AssetHolder getAssetHolder(String[] meshes, String[] textures) {
        AssetHolder assetHolder = new AssetHolder();

            int state = SpaceObject.INIT;
            assetHolder.setTuple(state, new AssetObjectTuple(meshMap.get(meshes[state]),
                    textureMap.get(textures[state])));

            state = SpaceObject.CLICKED;
            assetHolder.setTuple(state, new AssetObjectTuple(meshMap.get(meshes[state]),
                    textureMap.get(textures[state])));

            state = SpaceObject.OVER;
            assetHolder.setTuple(state, new AssetObjectTuple(meshMap.get(meshes[state]),
                    textureMap.get(textures[state])));

            state = SpaceObject.WIRE;
            assetHolder.setTuple(state, new AssetObjectTuple(meshMap.get(meshes[state]),
                    textureMap.get(textures[state])));

        return assetHolder;
    }

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.mipmap.ic_launcher);
        // return the correct splash screen bitmap
        return new GVRBitmapTexture(gvrContext, bitmap);
    }
}
