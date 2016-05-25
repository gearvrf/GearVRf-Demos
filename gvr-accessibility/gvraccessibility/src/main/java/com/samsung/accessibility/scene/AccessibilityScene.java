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
package com.samsung.accessibility.scene;

import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessiblityObject;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.main.Main;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;
import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilitySceneShader;
import com.samsung.accessibility.util.AccessibilityTexture;

public class AccessibilityScene extends GVRScene {

    private GVRContext gvrContext;
    private GVRScene mainApplicationScene;
    private ShortcutMenu shortcutMenu;
    private AccessibilityTexture textures;

    public AccessibilityScene(GVRContext gvrContext, GVRScene mainApplicationScene, ShortcutMenu shortcutMenu) {
        super(gvrContext);
        this.gvrContext = gvrContext;
        textures = AccessibilityTexture.getInstance(gvrContext);
        this.mainApplicationScene = mainApplicationScene;
        this.shortcutMenu = shortcutMenu;

        GVRSceneObject skyBox = createSkybox();
        applyShaderOnSkyBox(skyBox);
        addSceneObject(skyBox);
        createItems();
    }

    private GVRSceneObject createSkybox() {
        GVRMesh mesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.environment_walls_mesh));
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.environment_walls_tex_diffuse));
        GVRSceneObject skybox = new GVRSceneObject(gvrContext, mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getTransform().setPositionY(-1.6f);
        skybox.getRenderData().setRenderingOrder(0);

        GVRMesh meshGround = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.environment_ground_mesh));
        GVRTexture textureGround = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.environment_ground_tex_diffuse));
        GVRSceneObject skyboxGround = new GVRSceneObject(gvrContext, meshGround, textureGround);
        skyboxGround.getRenderData().setRenderingOrder(0);

        GVRMesh meshFx = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.windows_fx_mesh));
        GVRTexture textureFx = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.windows_fx_tex_diffuse));
        GVRSceneObject skyboxFx = new GVRSceneObject(gvrContext, meshFx, textureFx);
        skyboxGround.getRenderData().setRenderingOrder(0);
        skybox.addChildObject(skyboxFx);
        skybox.addChildObject(skyboxGround);
        return skybox;
    }

    private void createItems() {
        float positionX = 0f;
        float positionY = -1f;
        float positionZ = -10f;
        float scale = 0.03f;
        GVRMesh mesh = getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.accessibility_item));
        final SceneItem invertedColors = new SceneItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.inverted_colors)));
        invertedColors.getTransform().setPosition(positionX, positionY, positionZ);
        invertedColors.getTransform().setScale(scale, scale, scale);
        invertedColors.attachEyePointeeHolder();
        invertedColors.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                invertedColors.animate();
                if (invertedColors.isActive) {
                    shortcutMenu.addShortcut(TypeItem.INVERTED_COLORS, textures.getInvertedColorsIcon());
                } else {
                    ShortcutMenuItem shortcut = shortcutMenu.removeShortcut(TypeItem.INVERTED_COLORS);
                    if (shortcut != null) {
                        shortcut.resetClick();
                        Main.manager.getInvertedColors().turnOff(Main.accessibilityScene.getMainApplicationScene());
                        Main.manager.getInvertedColors().turnOff(Main.accessibilityScene);
                     }
                }
            }
        });
        this.addSceneObject(invertedColors);

        final SceneItem zoom = new SceneItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.zoom)));
        zoom.getTransform().setPosition(positionX, positionY, positionZ);
        zoom.getTransform().setScale(scale, scale, scale);
        zoom.attachEyePointeeHolder();
        zoom.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                zoom.animate();
                if (zoom.isActive) {
                    shortcutMenu.addShortcut(TypeItem.ZOOM, textures.getZoomOut());
                    shortcutMenu.addShortcut(TypeItem.ZOOM, textures.getZoomIn());
                } else {
                    shortcutMenu.removeShortcut(TypeItem.ZOOM);
                }
            }
        });
        this.addSceneObject(zoom);

        final SceneItem talkBack = new SceneItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.talk_back)));
        talkBack.getTransform().setPosition(positionX, positionY, positionZ);
        talkBack.getTransform().setScale(scale, scale, scale);
        talkBack.attachEyePointeeHolder();
        talkBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                talkBack.animate();
                setActivityOrInactiveTalkBackObjects(talkBack.isActive);
                if (talkBack.isActive) {
                    shortcutMenu.addShortcut(TypeItem.TALK_BACK, textures.getTalkBackLess());
                    shortcutMenu.addShortcut(TypeItem.TALK_BACK, textures.getTalkBackMore());
                } else {
                    shortcutMenu.removeShortcut(TypeItem.TALK_BACK);
                }
            }
        });

        this.addSceneObject(talkBack);

        final SceneItem speech = new SceneItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.speech)));
        speech.getTransform().setPosition(positionX, positionY, positionZ);
        speech.getTransform().setScale(scale, scale, scale);
        speech.attachEyePointeeHolder();
        speech.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                speech.animate();
                if (speech.isActive) {
                    shortcutMenu.addShortcut(TypeItem.SPEECH, textures.getSpeechIcon());
                } else {
                    shortcutMenu.removeShortcut(TypeItem.SPEECH);
                }
            }
        });

        this.addSceneObject(speech);
        float angle = -20;
        invertedColors.getTransform().rotateByAxisWithPivot(-1 * angle, 0, 1, 0, 0, 0, 0);
        zoom.getTransform().rotateByAxisWithPivot(0 * angle, 0, 1, 0, 0, 0, 0);
        talkBack.getTransform().rotateByAxisWithPivot(1 * angle, 0, 1, 0, 0, 0, 0);
        speech.getTransform().rotateByAxisWithPivot(2 * angle, 0, 1, 0, 0, 0, 0);

    }

    private void applyShaderOnSkyBox(GVRSceneObject skyBox) {
        AccessibilitySceneShader shader = new AccessibilitySceneShader(gvrContext);
        applyShader(shader, skyBox);
        for (GVRSceneObject object : skyBox.getChildren()) {
            applyShader(shader, object);
        }
    }

    private void applyShader(AccessibilitySceneShader shader, GVRSceneObject object) {
        if (object != null && object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
            object.getRenderData().getMaterial().setShaderType(shader.getShaderId());
            object.getRenderData().getMaterial().setTexture(AccessibilitySceneShader.TEXTURE_KEY,
                    object.getRenderData().getMaterial().getMainTexture());
            object.getRenderData().getMaterial().setFloat(AccessibilitySceneShader.BLUR_INTENSITY, 1);
        }
    }

    public GVRScene getMainApplicationScene() {
        return mainApplicationScene;
    }

    public ShortcutMenu getShortcutMenu() {
        return shortcutMenu;
    }

    private void setActivityOrInactiveTalkBackObjects(boolean active) {
        for (GVRSceneObject object : mainApplicationScene.getWholeSceneObjects()) {
            if (object instanceof GVRAccessiblityObject && ((GVRAccessiblityObject) object).getTalkBack() != null) {
                ((GVRAccessiblityObject) object).getTalkBack().setActive(active);
            }
        }
    }

    public void show() {
        getGVRContext().setMainScene(this);
        mainApplicationScene.removeSceneObject(shortcutMenu);
        addSceneObject(shortcutMenu);
    }

}
