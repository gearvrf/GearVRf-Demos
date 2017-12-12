/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.main;

import java.util.EnumSet;
import java.util.Locale;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.IActivityEvents;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.accessibility.GVRAccessibilityTalkBack;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRGazeCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.GearCursorController;
import org.gearvrf.utility.Log;

import android.view.MotionEvent;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableController;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnFocusListener;
import com.samsung.accessibility.focus.VRTouchPadGestureDetector;
import com.samsung.accessibility.gaze.GazeCursorSceneObject;
import com.samsung.accessibility.scene.AccessibilityScene;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;
import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilityManager;
import com.samsung.accessibility.util.AccessibilityTexture;

public class Main extends GVRMain {

    private GVRContext gvrContext;

    private GazeCursorSceneObject cursor;

    private FocusableSceneObject trex;

    private FocusableSceneObject bookObject;
    public static AccessibilityScene accessibilityScene;
    public static AccessibilityManager manager;

    private ITouchEvents mTouchHandler = new TouchHandler();
    private GVRCursorController mController = null;
    private GVRSceneObject pickedObject = null;

    /*
     * Handles initializing the selected controller:
     * - add listener for touch events coming from the controller
     * - attach the scene object to represent the cursor
     * - set cursor properties
     * If we are using the Gaze controller, it does not generate touch events directly.
     * We need to listen for them from GVRActivity to process them with a gesture detector.
     */
    private GVRInputManager.ICursorControllerSelectListener controllerSelector = new GVRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
        {
            mController = newController;
            newController.addPickEventListener(mTouchHandler);
            newController.setCursor(cursor);
            newController.setCursorDepth(10.0f);
            newController.sendEventsToActivity(true);
            newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
        }
    };

    /*
     * Keeps track of the current picked object.
     * The default for GVRPicker is to pick only the closest object.
     * If that is changed, the logic below is incorrect because
     * it is possible for multiple objects to be picked.
     */
    public class TouchHandler extends GVREventListeners.TouchEvents
    {
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject hit)
        {
            pickedObject = hit.getHitObject();
            Log.d("PICK:", "onEnter %s %s", sceneObj.getClass().getSimpleName(), sceneObj.getName());
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject hit)
        {
            if (sceneObj == pickedObject)
            {
                pickedObject = null;
            }
        }
    }

    @Override
    public void onInit(final GVRContext gvrContext) {

        this.gvrContext = gvrContext;
        AccessibilityTexture.getInstance(gvrContext);
        cursor = GazeCursorSceneObject.getInstance(gvrContext);
        manager = new AccessibilityManager(gvrContext);

        GVRScene scene = gvrContext.getMainScene();
        final ShortcutMenu shortcutMenu = createShortcut();

        accessibilityScene = new AccessibilityScene(gvrContext, gvrContext.getMainScene(), shortcutMenu);
        createPedestalObject();
        createDinossaur();

        scene.addSceneObject(shortcutMenu);
        scene.getMainCameraRig().addChildObject(cursor);
        scene.addSceneObject(createSkybox());
        gvrContext.getInputManager().selectController(controllerSelector);
    }

    private ShortcutMenu createShortcut() {
        ShortcutMenu shortcutMenu = new ShortcutMenu(gvrContext);
        ShortcutMenuItem shortcuteItem = shortcutMenu.getShortcutItems().get(0);
        shortcuteItem.createIcon(AccessibilityTexture.getInstance(gvrContext).getAccessibilityIcon(), TypeItem.ACCESSIBILITY);        
        return shortcutMenu;
    }

    private void createPedestalObject() {
        GVRMesh baseMesh = gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.base));
        GVRMesh bookMesh = gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.book));
        GVRTexture bookTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.book));
        GVRTexture baseTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.base));

        FocusableSceneObject baseObject = new FocusableSceneObject(gvrContext, baseMesh, baseTexture);
        bookObject = new FocusableSceneObject(gvrContext, bookMesh, bookTexture);
        FocusableSceneObject pedestalObject = new FocusableSceneObject(gvrContext);

        baseObject.getTransform().setScale(0.005f, 0.005f, 0.005f);
        bookObject.getTransform().setScale(0.005f, 0.005f, 0.005f);

        baseObject.getTransform().setPosition(0, -1.6f, -2);
        bookObject.getTransform().setPosition(0, -1.6f, -2);

        pedestalObject.addChildObject(baseObject);
        pedestalObject.addChildObject(bookObject);

        gvrContext.getMainScene().addSceneObject(pedestalObject);
    }

    private GVRSceneObject createSkybox() {
        GVRMesh mesh = gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.environment_walls_mesh));
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.environment_walls_tex_diffuse));
        GVRSceneObject skybox = new GVRSceneObject(gvrContext, mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getTransform().setPositionY(-1.6f);
        skybox.getRenderData().setRenderingOrder(0);

        GVRMesh meshGround = gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.environment_ground_mesh));
        GVRTexture textureGround = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.environment_ground_tex_diffuse));
        GVRSceneObject skyboxGround = new GVRSceneObject(gvrContext, meshGround, textureGround);
        skyboxGround.getRenderData().setRenderingOrder(0);

        GVRMesh meshFx = gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.windows_fx_mesh));
        GVRTexture textureFx = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.windows_fx_tex_diffuse));
        GVRSceneObject skyboxFx = new GVRSceneObject(gvrContext, meshFx, textureFx);
        skyboxGround.getRenderData().setRenderingOrder(0);
        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);
        return skybox;
    }

    private void createDinossaur() {

        EnumSet<GVRImportSettings> additionalSettings = EnumSet
                .of(GVRImportSettings.CALCULATE_SMOOTH_NORMALS);

        EnumSet<GVRImportSettings> settings = GVRImportSettings
                .getRecommendedSettingsWith(additionalSettings);

        GVRMesh baseMesh = gvrContext.getAssetLoader().loadMesh(new GVRAndroidResource(gvrContext, R.raw.trex_mesh), settings);
        GVRTexture baseTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.trex_tex_diffuse));
        trex = new FocusableSceneObject(gvrContext, baseMesh, baseTexture);
        trex.getTransform().setPosition(0, -1.6f, -7f);
        trex.getTransform().rotateByAxis(-90, 1, 0, 0);
        trex.getTransform().rotateByAxis(90, 0, 1, 0);
        activeTalkBack();
        gvrContext.getMainScene().addSceneObject(trex);
    }

    public void setScene(GVRScene scene)
    {
        mController.setScene(scene);
        gvrContext.setMainScene(scene);
    }

    @Override
    public void onStep() {
    }


    public void onSingleTap(MotionEvent e) {
        FocusableController.clickProcess(pickedObject);
    }

    private void activeTalkBack() {
        GVRAccessibilityTalkBack talkBackDinossaur = new GVRAccessibilityTalkBack(Locale.US, gvrContext.getContext(), "Dinossaur");
        trex.setTalkBack(talkBackDinossaur);
        trex.attachComponent(new GVRSphereCollider(gvrContext));
        trex.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                trex.getTalkBack().speak();
            }
        });

        GVRAccessibilityTalkBack talkBackBook = new GVRAccessibilityTalkBack(Locale.US, gvrContext.getContext(), "Book");
        bookObject.setTalkBack(talkBackBook);
        bookObject.attachComponent(new GVRMeshCollider(gvrContext, true));
        bookObject.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                bookObject.getTalkBack().speak();
            }
        });
    }
}
