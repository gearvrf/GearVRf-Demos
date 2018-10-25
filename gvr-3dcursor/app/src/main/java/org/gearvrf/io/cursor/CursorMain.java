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
import android.view.ViewGroup;
import android.widget.TextView;

import com.gearvrf.io.gearwear.GearWearableDevice;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRApplication;
import org.gearvrf.GVRBitmapImage;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ZipLoader;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.CursorTheme;
import org.gearvrf.io.cursor3d.CursorType;
import org.gearvrf.io.cursor3d.ICursorActivationListener;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.io.cursor3d.MovableBehavior;
import org.gearvrf.io.cursor3d.SelectableBehavior;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CursorMain extends GVRMain {
    private static final String TAG = CursorMain.class.getSimpleName();

    private static final float CUBE_WIDTH = 200.0f;
    private static final float DEFAULT_CURSOR_DEPTH = 10;
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
    private final List<TextView> textViewList;
    private final List<TextView> circleTextViewList;
    private final TextView resetTextView;
    private final TextView settingsTextView;

    private Map<String, SpaceObject> objects;
    private List<CursorTheme> laserCursorThemes;
    private List<CursorTheme> pointCursorThemes;
    private CursorType currentType = CursorType.UNKNOWN;
    private List<Cursor> cursors;
    private String[] textViewStrings;
    private Map<String, GVRMesh> meshMap;
    private Map<String, GVRMaterial> materialMap;
    private GearWearableDevice gearWearableDevice;

    public CursorMain(GVRApplication application) {
        Resources resources = application.getActivity().getResources();
        LIGHT_BLUE_COLOR = resources.getColor(R.color.LIGHT_BLUE);
        objects = new HashMap<String, SpaceObject>();
        textViewList = new ArrayList<TextView>(NUM_TEXT_VIEWS);
        for (int i = 0; i < NUM_TEXT_VIEWS; i++) {
            addGVRTextView(application, i);
        }

        circleTextViewList = new ArrayList<TextView>(NUM_TEXT_VIEWS);
        addCircleGVRTextViews(application, resources);

        resetTextView = getTextView(application, 0);
        resetTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        resetTextView.setGravity(Gravity.CENTER);
        settingsTextView = getTextView(application, 0);
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
        mainScene = gvrContext.getMainScene();
        meshMap = new HashMap<String, GVRMesh>();
        materialMap = new HashMap<String, GVRMaterial>();
        addSurroundings(gvrContext, mainScene);
        try {
            ZipLoader.load(gvrContext, MESH_FILE, new ZipLoader
                    .ZipEntryProcessor<GVRMesh>() {
                @Override
                public GVRMesh getItem(GVRContext context, GVRAndroidResource resource) {
                    GVRMesh mesh = context.getAssetLoader().loadMesh(resource);
                    meshMap.put(resource.getResourceFilename(), mesh);
                    if (mesh == null) {
                        throw new IllegalArgumentException("Mesh " + resource.getResourceFilename() + " cannot be loaded");
                    }
                    return mesh;
                }
            });

            ZipLoader.load(gvrContext, TEXTURE_FILE, new
                    ZipLoader
                            .ZipEntryProcessor<GVRTexture>() {

                        @Override
                        public GVRTexture getItem(GVRContext context, GVRAndroidResource resource)
                        {
                            GVRTexture texture = context.getAssetLoader().loadTexture(resource);
                            GVRMaterial mtl = new GVRMaterial(context);
                            mtl.setMainTexture(texture);
                            materialMap.put(resource.getResourceFilename(), mtl);
                            return texture;
                        }
                    });
        } catch (IOException e) {
            Log.e(TAG, "Error Loading textures/meshes");
        }

        mainScene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.BLACK);
        mainScene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.BLACK);

        GVRInputManager inputManager = gvrContext.getInputManager();
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

        gearWearableDevice = new GearWearableDevice(gvrContext, GEARS2_DEVICE_ID, "Gear Wearable");
        devices.add(gearWearableDevice);
        inputManager.getEventReceiver().addListener(
                new GVRInputManager.ICursorControllerSelectListener()
                {
                    @Override
                    public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
                    {
                        newController.setCursorDepth(DEFAULT_CURSOR_DEPTH);
                    }
                });

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
        addSpaceObject(new SpaceObject(cursorManager, getRocketShipAsset(), "rocketship", position,
                1.5f, getRotationFromIndex(INDEX_ROCKET), 0.0f));

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH + 5.0f);
        addSpaceObject(new MovableObject(cursorManager, getStarAsset(), "star1", position, 0.75f,
                getRotationFromIndex(INDEX_STAR), 0.0f, 0.0f, STAR_Y_ORIENTATION, 0.0f));
        position.set(0.0f, SCENE_HEIGHT - 0.5f, SCENE_DEPTH);
        addSpaceObject(new MovableObject(cursorManager, getStarAsset(), "star2", position, 1.5f,
                getRotationFromIndex(INDEX_STAR) - 1, 0.0f, 0.0f, STAR_Y_ORIENTATION, 0.0f));
        position.set(0.0f, SCENE_HEIGHT + 0.5f, SCENE_DEPTH - 5.0f);
        addSpaceObject(new MovableObject(cursorManager, getStarAsset(), "star3", position, 2.25f,
                getRotationFromIndex(INDEX_STAR) - 2, 0.0f, 0.0f, STAR_Y_ORIENTATION, 0.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new MovableObject(cursorManager, getAstronautAsset(), "astronaut", position,
                1.0f, getRotationFromIndex(INDEX_ASTRONAUT), 0.0f));

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH);
        addSpaceObject(new LaserCursorButton(cursorManager, getButtonAsset(), "laser_button",
                position, 2f, getRotationFromIndex(INDEX_LASER_BUTTON), 0.0f));

        position.set(0.0f, SCENE_HEIGHT - 1.50f, SCENE_DEPTH);
        MovableObject object = new MovableObject(cursorManager, getCubeAsset(), "cube1", position,
                1.8f, getRotationFromIndex(INDEX_CUBES_1) + 1.0f, 0, 0.0f, CUBE_Y_ORIENTATION,
                0.0f);
        addSpaceObject(object);

        position.set(0.0f, SCENE_HEIGHT - 0.85f, SCENE_DEPTH - 3.0f);
        object = new MovableObject(cursorManager, getCubeAsset(), "cube2", position, 2.5f,
                getRotationFromIndex(INDEX_CUBES_1) - 0.2f, 0, 0.0f, CUBE_Y_ORIENTATION, 0.0f);
        addSpaceObject(object);

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH - 8.0f);
        object = new MovableObject(cursorManager, getCubeAsset(), "cube3", position, 3.75f,
                getRotationFromIndex(INDEX_CUBES_1) - 1.3f, 0, 0.0f, CUBE_Y_ORIENTATION, 0.0f);
        addSpaceObject(object);

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH);
        addSpaceObject(new HandCursorButton(cursorManager, getButtonAsset(), "hands_button",
                position, 2f, getRotationFromIndex(INDEX_HANDS_BUTTON), 0.0f));

        position.set(0, SCENE_HEIGHT, SCENE_DEPTH + 15.0f);
        MovableObject rightSword = new MovableObject(cursorManager, getSwordAsset(), "sword_1",
                position, 1.5f, getRotationFromIndex(INDEX_SWORDS) - 6.5f, 0, 0.0f, 80.0f, -5.5f);
        addSpaceObject(rightSword);

        position.set(0, SCENE_HEIGHT, SCENE_DEPTH + 15.0f);
        MovableObject leftSword = new MovableObject(cursorManager, getSwordAsset(), "sword_2",
                position, 1.5f, getRotationFromIndex(INDEX_SWORDS) + 6.5f, 0, 0.0f, -140.0f, -5.5f);
        addSpaceObject(leftSword);

        position.set(0.0f, SCENE_HEIGHT - 1.5f, SCENE_DEPTH);
        addSpaceObject(new GearS2Button(cursorManager, getButtonAsset(), "gears2_button", position,
                2f, getRotationFromIndex(INDEX_GEARS2_BUTTON), -1.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new MovableObject(cursorManager, getAstronautAsset(),
                "collection_astronaut", position, 1.0f, getRotationFromIndex
                (INDEX_SPACE_COLLEC_1) + 3.5f, 0.0f));

        position.set(0.0f, SCENE_HEIGHT + 2.0f, SCENE_DEPTH - 2.5f);
        addSpaceObject(new MovableObject(cursorManager, getRocketShipAsset(),
                "collection_rocketship", position, 1.25f, getRotationFromIndex
                (INDEX_SPACE_COLLEC_1), 0.0f));

        position.set(0.0f, SCENE_HEIGHT + 3.0f, SCENE_DEPTH - 5.0f);
        addSpaceObject(new MovableObject(cursorManager, getStarAsset(), "collection_star",
                position, 3f, getRotationFromIndex(INDEX_SPACE_COLLEC_1) - 5.0f, 0.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH - 15.0f);
        addSpaceObject(new MovableObject(cursorManager, getCloud1Asset(), "cloud1", position,
                1.0f, 0.0f, +10.0f));

        addSpaceObject(new MovableObject(cursorManager, getCloud2Asset(), "cloud2", position,
                1.0f, -15.0f, +3.0f));

        addSpaceObject(new MovableObject(cursorManager, getCloud3Asset(), "cloud3", position,
                1.0f, +15.0f, +3.0f));

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new ResetButton(cursorManager, getButtonAsset(), "reset", position, 2.5f,
                SETTINGS_ROTATION_X, SETTINGS_ROTATION_Y - 2.0f));
        GVRViewSceneObject resetText = new GVRViewSceneObject(gvrContext, resetTextView,
                gvrContext.createQuad(TEXT_QUAD_WIDTH, TEXT_QUAD_HEIGHT));
        resetText.getTransform().setPosition(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        resetText.getTransform().rotateByAxisWithPivot(SETTINGS_ROTATION_X, 0, 1, 0, 0, 0, 0);
        resetText.getTransform().rotateByAxisWithPivot(SETTINGS_ROTATION_Y +
                SETTINGS_TEXT_OFFSET, 1, 0, 0, 0, 0, 0);
        mainScene.addSceneObject(resetText);

        position.set(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        addSpaceObject(new SettingsObject(cursorManager, getSettingAsset(), "settings", position,
                2.0f, -SETTINGS_ROTATION_X, SETTINGS_ROTATION_Y, 0.0f));
        GVRViewSceneObject settingsText = new GVRViewSceneObject(gvrContext, settingsTextView,
                gvrContext.createQuad(TEXT_QUAD_WIDTH, TEXT_QUAD_HEIGHT));
        settingsText.getTransform().setPosition(0.0f, SCENE_HEIGHT, SCENE_DEPTH);
        settingsText.getTransform().rotateByAxisWithPivot(-SETTINGS_ROTATION_X, 0, 1, 0, 0, 0, 0);
        settingsText.getTransform().rotateByAxisWithPivot(SETTINGS_ROTATION_Y +
                SETTINGS_TEXT_OFFSET, 1, 0, 0, 0, 0, 0);
        mainScene.addSceneObject(settingsText);

        cursors = new ArrayList<Cursor>();
        cursorManager.getEventReceiver().addListener(activationListener);

        // register all active cursors
        for(Cursor cursor : cursorManager.getActiveCursors()){
            activationListener.onActivated(cursor);
        }
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

    private void addGVRTextView(GVRApplication application, int index) {
        textViewList.add(getTextView(application, index));
    }

    private TextView getTextView(GVRApplication application, int index) {
        Resources resources = application.getActivity().getResources();
        TextView textView = new TextView(application.getActivity());
        textView.setLayoutParams(new ViewGroup.LayoutParams(TEXT_VIEW_WIDTH, getTextViewHeightFromIndex(index)));
        textView.setTextSize(STANDARD_TEXT_SIZE);
        textView.setTextColor(LIGHT_BLUE_COLOR);
        setTextViewProperties(textView, resources);
        return textView;
    }

    private void addCircleGVRTextViews(GVRApplication application, Resources resources) {
        for (int count = 0; count < NUM_TEXT_VIEWS; count++) {
            TextView textView = new TextView(application.getActivity());
            textView.setLayoutParams(new ViewGroup.LayoutParams(CIRCLE_TEXT_VIEW_WIDTH, CIRCLE_TEXT_VIEW_HEIGHT));
            textView.setTextSize(CIRCLE_TEXT_SIZE);
            textView.setTextColor(Color.BLACK);
            textView.setBackground(resources.getDrawable(R.drawable.circle));
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(count + 1));
            circleTextViewList.add(textView);
        }
    }

    private void setTextViewProperties(TextView textView, Resources resources) {
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

    private void setTextOnMainThread(final TextView textView, final String text) {
        gvrContext.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    private ICursorActivationListener activationListener = new ICursorActivationListener() {

        @Override
        public void onDeactivated(Cursor cursor) {
            Log.d(TAG, "Cursor DeActivated:" + cursor.getName());
            synchronized (cursors) {
                cursors.remove(cursor);
            }
        }

        @Override
        public void onActivated(Cursor newCursor) {
            synchronized (cursors) {
                cursors.add(newCursor);
            }

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
        GVRMesh quadMesh = new GVRMesh(gvrContext, "float3 a_position float2 a_texcoord");
        GVRTexture cubemapTexture = gvrContext.getAssetLoader()
                .loadCubemapTexture(new GVRAndroidResource(gvrContext, R.raw.earth));

        GVRMaterial cubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        cubemapMaterial.setMainTexture(cubemapTexture);
        quadMesh.createQuad(CUBE_WIDTH, CUBE_WIDTH);

        // surrounding cube
        GVRSceneObject frontFace = new GVRSceneObject(gvrContext, quadMesh, cubemapTexture);
        frontFace.getRenderData().setMaterial(cubemapMaterial);
        frontFace.setName("front");
        frontFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        scene.addSceneObject(frontFace);
        frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, quadMesh, cubemapTexture);
        backFace.getRenderData().setMaterial(cubemapMaterial);
        backFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, quadMesh, cubemapTexture);
        leftFace.getRenderData().setMaterial(cubemapMaterial);
        leftFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext, quadMesh, cubemapTexture);
        rightFace.getRenderData().setMaterial(cubemapMaterial);
        rightFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, quadMesh, cubemapTexture);
        topFace.getRenderData().setMaterial(cubemapMaterial);
        topFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext, quadMesh, cubemapTexture);
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
        //device.close();
        //device1.close();
        //device2.close();
    }

    private class SettingsObject extends SpaceObject {
        public SettingsObject(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY, float orientationX) {
            super(cursorMgr, asset, name, position, scale, rotationX, rotationY, orientationX,
                    0.0f, 0.0f);
        }

        @Override
        public void onTouchStart(Cursor c, GVRPicker.GVRPickedObject hit) {
            Log.d(TAG, "handleClickReleased: show settings menu");
            cursorManager.setSettingsMenuVisibility(c, true);
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

        public HandCursorButton(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(cursorMgr, asset, name, position, scale, rotationX, rotationY,
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
        public void onTouchEnd(Cursor c, GVRPicker.GVRPickedObject hit) {
            Cursor currentCursor = c;
            currentIoDevice = c.getIoDevice();
            currentIoDeviceUsed = false;
            handCursors.clear();
            setOffsetPositionFromCursor(rightCursorPosition,currentCursor);
            leftCursorPosition.set(rightCursorPosition);

            if (c.getCursorType() == CursorType.LASER) ;
            {
                currentCursor.deactivate();
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
                    cursor.activate();
                    if (cursor.getIoDevice() == currentIoDevice) {
                        currentIoDeviceUsed = true;
                    }
                    handCursors.add(cursor);
                } else if (cursor.getCursorType() == CursorType.LASER) {
                    cursor.deactivate();
                }
            }
        }
    }

    private class LaserCursorButton extends SpaceObject {
        public LaserCursorButton(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(cursorMgr, asset, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION, 0.0f, 0.0f);
        }

        @Override
        public void onTouchEnd(Cursor c, GVRPicker.GVRPickedObject hit) {
            Cursor laserCursor = cursorManager.findCursorByType(CursorType.LASER);

            if ((laserCursor == null) || c.getCursorType().equals(CursorType.LASER))
            {
                return;
            }
            cursorManager.replaceCursor(laserCursor, c);
        }
    }

    private class GearS2Button extends SpaceObject {
        Vector3f cursorPosition;
        public GearS2Button(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(cursorMgr, asset, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION, 0.0f, 0.0f);
            cursorPosition = new Vector3f();
        }

        @Override
        public void onTouchEnd(Cursor c, GVRPicker.GVRPickedObject hit)
        {
            Cursor currentCursor = c;
            Cursor targetCursor = cursorManager.findCursorByDevice(gearWearableDevice);

            if (targetCursor == null)
            {
                return;
            }
            setOffsetPositionFromCursor(cursorPosition, currentCursor);
            for (CursorTheme theme : cursorManager.getCursorThemes())
            {
                if (theme.getId().equals(CRYSTAL_THEME))
                {
                    targetCursor.setCursorTheme(theme);
                }
            }
            if (currentCursor == targetCursor)
            {
                return;
            }
            cursorManager.replaceCursor(targetCursor, currentCursor);
            setCursorPosition(cursorPosition, targetCursor);
        }
    }

    private class ResetButton extends SpaceObject {
        private static final String RESET_CURSOR_NAME = "Right Cursor";

        public ResetButton(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            super(cursorMgr, asset, name, position, scale, rotationX, rotationY,
                    BUTTON_XORIENTATION + 10.0f, 0.0f, 0.0f);
        }

        @Override
        public void onTouchEnd(Cursor c, GVRPicker.GVRPickedObject hit) {
            for (SpaceObject spaceObject : objects.values()) {
                spaceObject.reset();
            }

            Cursor resetCursor = cursorManager.findCursorByName(RESET_CURSOR_NAME);

            if (resetCursor == null)
            {
                return;
            }
            for (CursorTheme theme : cursorManager.getCursorThemes()) {
                if (theme.getId().equals(CRYSTAL_THEME)) {
                    resetCursor.setCursorTheme(theme);
                }
            }
            cursorManager.replaceCursor(resetCursor, c);
        }
    }

    class MovableObject extends SpaceObject {
        private GVRSceneObject sceneObject;

        public MovableObject(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY) {
            this(cursorMgr, asset, name, position, scale, rotationX, rotationY, 0.0f, 0.0f, 0.0f);
        }

        public MovableObject(CursorManager cursorMgr, GVRSceneObject asset, String name, Vector3f
                position, float scale, float rotationX, float rotationY, float orientationX,
                             float orientationY, float orientationZ) {
            super(cursorMgr, asset, name, position, scale, rotationX, rotationY, orientationX,
                    orientationY, orientationZ);
            initialize(cursorMgr);
        }

        private void initialize(CursorManager cursorMgr) {
            sceneObject = getSceneObject();
            cursorMgr.removeSelectableObject(sceneObject);
            sceneObject.detachComponent(SelectableBehavior.getComponentType());
            sceneObject.attachComponent(new MovableBehavior(cursorMgr, true));
        }
    }

    private GVRSceneObject getStarAsset() {
        return getAsset(STAR_MESHES, STAR_TEXTURES, true);
    }

    private GVRSceneObject getCubeAsset() {
        return getAsset(CUBE_MESHES, CUBE_TEXTURES, true);
    }

    private GVRSceneObject getAstronautAsset() {
        return getAsset(ASTRONAUT_MESHES, ASTRONAUT_TEXTURES, true);
    }

    private GVRSceneObject getRocketShipAsset() {
        return getAsset(ROCKET_SHIP_MESHES, ROCKET_SHIP_TEXTURES, true);
    }

    private GVRSceneObject getSettingAsset() {
        return getAsset(SETTING_MESHES, SETTING_TEXTURES, false);
    }

    private GVRSceneObject getCloud1Asset() {
        return getAsset(CLOUD_1_MESHES, CLOUD_TEXTURES, false);
    }

    private GVRSceneObject getCloud2Asset() {
        return getAsset(CLOUD_2_MESHES, CLOUD_TEXTURES, false);
    }

    private GVRSceneObject getCloud3Asset() {
        return getAsset(CLOUD_3_MESHES, CLOUD_TEXTURES, false);
    }

    private GVRSceneObject getButtonAsset() {
        return getAsset(BUTTON_MESHES, BUTTON_TEXTURES, false);
    }

    private GVRSceneObject getSwordAsset() {

        return getAsset(SWORD_MESHES, SWORD_TEXTURES, false);
    }

    private GVRSceneObject getAsset(String[] meshes, String[] textures, boolean useMesh) {
        GVRSceneObject root = new GVRSceneObject(gvrContext);
        GVRMeshCollider collider = new GVRMeshCollider(gvrContext, true);

        collider.setMesh(meshMap.get(meshes[0]));
        root.setName(meshes[0]);
        root.attachCollider(collider);
        for (int state = SelectableBehavior.ObjectState.DEFAULT.ordinal();
             state <= SelectableBehavior.ObjectState.CLICKED.ordinal();
             ++state)
        {
            GVRMesh mesh = meshMap.get(meshes[state]);
            GVRSceneObject child = new GVRSceneObject(gvrContext, mesh);

            child.getRenderData().setMaterial(materialMap.get(textures[state]));
            child.setName(meshes[state]);
            root.addChildObject(child);
        }
        return root;
    }

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.mipmap.ic_launcher);
        GVRTexture splashScreen = new GVRTexture(gvrContext);
        // return the correct splash screen bitmap
        splashScreen.setImage(new GVRBitmapImage(gvrContext, bitmap));
        return splashScreen;
    }
}
