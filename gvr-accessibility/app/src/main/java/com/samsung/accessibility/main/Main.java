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
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessibilityTalkBack;

import android.view.MotionEvent;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableController;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnFocusListener;
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

    @Override
    public void onInit(final GVRContext gvrContext) {
        GVRScene scene = gvrContext.getMainScene();
        this.gvrContext = gvrContext;
        AccessibilityTexture.getInstance(gvrContext);
        cursor = GazeCursorSceneObject.getInstance(gvrContext);
        manager = new AccessibilityManager(gvrContext);
        //gvrContext.getNextMainScene().setFrustumCulling(false);
        ShortcutMenu shortcutMenu = createShortcut();
        accessibilityScene = new AccessibilityScene(gvrContext, scene, shortcutMenu);

        createPedestalObject();
        createDinossaur();

        cursor.setName("cursor");
        scene.addSceneObject(shortcutMenu);
        scene.getMainCameraRig().addChildObject(cursor);
        scene.addSceneObject(createSkybox());

    }

    private ShortcutMenu createShortcut() {
        ShortcutMenu shortcutMenu = new ShortcutMenu(gvrContext);
        ShortcutMenuItem shortcuteItem = shortcutMenu.getShortcutItems().get(0);
        shortcuteItem.createIcon(AccessibilityTexture.getInstance(gvrContext).getAccessibilityIcon(), TypeItem.ACCESSIBILITY);        
        return shortcutMenu;
    }

    private void createPedestalObject() {
        GVRAssetLoader loader = gvrContext.getAssetLoader();
        GVRMesh baseMesh = loader.loadMesh(new GVRAndroidResource(gvrContext, R.raw.base));
        GVRMesh bookMesh = loader.loadMesh(new GVRAndroidResource(gvrContext, R.raw.book));
        GVRTexture bookTexture = loader.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.book));
        GVRTexture baseTexture = loader.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.base));

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
        GVRAssetLoader loader = gvrContext.getAssetLoader();
        GVRMesh mesh = loader.loadMesh(new GVRAndroidResource(gvrContext, R.raw.environment_walls_mesh));
        GVRTexture texture = loader.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.environment_walls_tex_diffuse));
        GVRSceneObject skybox = new GVRSceneObject(gvrContext, mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);

        skybox.getTransform().setPositionY(-1.6f);
        skybox.getRenderData().setRenderingOrder(0);

        GVRMesh meshGround = loader.loadMesh(new GVRAndroidResource(gvrContext, R.raw.environment_ground_mesh));
        GVRTexture textureGround = loader.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.environment_ground_tex_diffuse));
        GVRSceneObject skyboxGround = new GVRSceneObject(gvrContext, meshGround, textureGround);

        skyboxGround.getRenderData().setRenderingOrder(0);

        GVRMesh meshFx = loader.loadMesh(new GVRAndroidResource(gvrContext, R.raw.windows_fx_mesh));
        GVRTexture textureFx = loader.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.windows_fx_tex_diffuse));
        GVRSceneObject skyboxFx = new GVRSceneObject(gvrContext, meshFx, textureFx);
        skyboxGround.getRenderData().setRenderingOrder(0);

        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);

        return skybox;
    }

    private void createDinossaur() {
        GVRAssetLoader loader = gvrContext.getAssetLoader();
        EnumSet<GVRImportSettings> additionalSettings = EnumSet
                .of(GVRImportSettings.CALCULATE_SMOOTH_NORMALS);

        EnumSet<GVRImportSettings> settings = GVRImportSettings
                .getRecommendedSettingsWith(additionalSettings);

        GVRMesh baseMesh = loader.loadMesh(new GVRAndroidResource(gvrContext, R.raw.trex_mesh), settings);
        GVRTexture baseTexture = loader.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.trex_tex_diffuse));
        trex = new FocusableSceneObject(gvrContext, baseMesh, baseTexture);
        trex.getTransform().setPosition(0, -1.6f, -7f);
        trex.getTransform().rotateByAxis(-90, 1, 0, 0);
        trex.getTransform().rotateByAxis(90, 0, 1, 0);
        activeTalkBack();
        gvrContext.getMainScene().addSceneObject(trex);
    }

    @Override
    public void onStep() {
        FocusableController.process(gvrContext);
    }

    public void onSingleTap(MotionEvent e) {
        FocusableController.clickProcess(gvrContext);
    }

    private void activeTalkBack() {
        GVRAccessibilityTalkBack talkBackDinossaur = new GVRAccessibilityTalkBack(Locale.US, gvrContext.getContext(), "Dinossaur");
        trex.setTalkBack(talkBackDinossaur);
        trex.attachEyePointeeHolder();
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
        bookObject.attachCollider(new GVRMeshCollider(gvrContext, false));
        bookObject.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                // TODO Auto-generated method stub
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                // TODO Auto-generated method stub
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                bookObject.getTalkBack().speak();
            }
        });
    }
}
