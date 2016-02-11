package com.samsung.accessibility.scene;

import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessiblityObject;
import org.gearvrf.animation.GVROpacityAnimation;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.main.MainScript;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;
import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilityTexture;

public class AccessibilityScene extends GVRScene {

    private GVRSceneObject skybox;
    private GVRContext mGvrContext;
    private GVRScene mainApplicationScene;
    private ShortcutMenu shortcutMenu;
    private AccessibilityTexture textures;

    public AccessibilityScene(GVRContext gvrContext, GVRScene mainApplicationScene, ShortcutMenu shortcutMenu) {
        super(gvrContext);
        mGvrContext = gvrContext;
        textures = AccessibilityTexture.getInstance(gvrContext);
        this.mainApplicationScene = mainApplicationScene;
        this.shortcutMenu = shortcutMenu;

        createDefaultSkyBox();
        createItems();
    }

    private AccessibilityScene createDefaultSkyBox() {
        GVRMesh defaultMesh = getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.skybox_esphere_acessibility));
        GVRTexture defaultTexture = getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.skybox_accessibility));
        skybox = new GVRSceneObject(getGVRContext(), defaultMesh, defaultTexture);
        skybox.getTransform().setScale(1, 1, 1);
        skybox.getRenderData().setRenderingOrder(0);
        addSceneObject(skybox);
        applyShaderOnSkyBox(skybox);
        return this;
    }

    private void createItems() {
        float positionX = 0f;
        float positionY = -2f;
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
                List<ShortcutMenuItem> shortcutItems = shortcutMenu.getShortcutItems();

                if (invertedColors.isActive) {
                    for (int i = 0; i < shortcutItems.size(); i++) {

                        if (shortcutItems.get(i).getTypeItem() == TypeItem.EMPTY) {
                            shortcutItems.get(i).createIcon(textures.getInvertedColorsIcon(), TypeItem.INVERTED_COLORS);
                            break;
                        }

                    }
                } else {
                    for (int i = 0; i < shortcutItems.size(); i++) {
                        if (shortcutItems.get(i).getTypeItem() == TypeItem.INVERTED_COLORS) {
                            shortcutItems.get(i).resetClick();
                            shortcutMenu.removeShortcut(i);
                            MainScript.manager.getInvertedColors().turnOff(MainScript.accessibilityScene.getMainApplicationScene());
                            MainScript.manager.getInvertedColors().turnOff(MainScript.accessibilityScene);
                        }
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

                List<ShortcutMenuItem> shortcutItems = shortcutMenu.getShortcutItems();
                if (zoom.isActive) {
                    for (int i = 0; i < shortcutItems.size(); i++) {

                        if (shortcutItems.get(i).getTypeItem() == TypeItem.EMPTY) {
                            shortcutItems.get(i).createIcon(textures.getZoomIn(), TypeItem.ZOOM);
                            shortcutItems.get(i + 2).createIcon(textures.getZoomOut(), TypeItem.ZOOM);
                            break;
                        }

                    }
                } else {

                    for (int i = 0; i < shortcutItems.size(); i++) {
                        if (shortcutItems.get(i).getTypeItem() == TypeItem.ZOOM) {
                            shortcutMenu.removeShortcut(i, i + 2);
                            break;
                        }
                    }

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

                List<ShortcutMenuItem> shortcutItems = shortcutMenu.getShortcutItems();
                if (talkBack.isActive) {
                    for (int i = 0; i < shortcutItems.size(); i++) {

                        if (shortcutItems.get(i).getTypeItem() == TypeItem.EMPTY) {
                            shortcutItems.get(i).createIcon(textures.getTalkBackLess(), TypeItem.TALK_BACK);
                            shortcutItems.get(i + 2).createIcon(textures.getTalkBackMore(), TypeItem.TALK_BACK);
                            break;
                        }

                    }
                } else {

                    for (int i = 0; i < shortcutItems.size(); i++) {
                        if (shortcutItems.get(i).getTypeItem() == TypeItem.TALK_BACK) {
                            shortcutMenu.removeShortcut(i, i + 2);
                            break;
                        }
                    }

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

                List<ShortcutMenuItem> shortcutItems = shortcutMenu.getShortcutItems();

                if (speech.isActive) {

                    for (int i = 0; i < shortcutItems.size(); i++) {

                        if (shortcutItems.get(i).getTypeItem() == TypeItem.EMPTY) {
                            shortcutItems.get(i).createIcon(textures.getSpeechIcon(), TypeItem.SPEECH);
                            break;
                        }

                    }

                } else {

                    for (int i = 0; i < shortcutItems.size(); i++) {
                        if (shortcutItems.get(i).getTypeItem() == TypeItem.SPEECH)
                            shortcutMenu.removeShortcut(i);
                    }

                }
            }
        });

        this.addSceneObject(speech);

        final SceneItem captions = new SceneItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.captions)));
        captions.getTransform().setPosition(positionX, positionY, positionZ);
        captions.getTransform().setScale(scale, scale, scale);
        captions.attachEyePointeeHolder();
        captions.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                captions.animate();
            }
        });

        this.addSceneObject(captions);

        CursorSceneItem cursor = new CursorSceneItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.cursor)));
        cursor.getTransform().setPosition(positionX, positionY, positionZ);
        cursor.getTransform().setScale(scale, scale, scale);
        cursor.attachEyePointeeHolder();
        this.addSceneObject(cursor);

        float angle = -20;

        invertedColors.getTransform().rotateByAxisWithPivot(-3 * angle, 0, 1, 0, 0, 0, 0);
        zoom.getTransform().rotateByAxisWithPivot(-2 * angle, 0, 1, 0, 0, 0, 0);
        talkBack.getTransform().rotateByAxisWithPivot(-1 * angle, 0, 1, 0, 0, 0, 0);
        speech.getTransform().rotateByAxisWithPivot(0 * angle, 0, 1, 0, 0, 0, 0);
        captions.getTransform().rotateByAxisWithPivot(1 * angle, 0, 1, 0, 0, 0, 0);
        cursor.getTransform().rotateByAxisWithPivot(2 * angle, 0, 1, 0, 0, 0, 0);
    }

    public void setItemsRelativePosition(float positionX, float positionY, float positionZ) {
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object instanceof SceneItem || object instanceof CursorSceneItem) {
                object.getTransform().setPosition(object.getTransform().getPositionX() + positionX, object.getTransform().getPositionY() + positionY,
                        object.getTransform().getPositionZ() + positionZ);
            }
        }
    }

    private void applyShaderOnSkyBox(GVRSceneObject skyBox) {
        AccessibilitySceneShader shader = new AccessibilitySceneShader(mGvrContext);
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
            if (object instanceof FocusableSceneObject) {
                ((GVRAccessiblityObject) object).getTalkBack().setActive(active);
            }
        }
    }

    public void show() {
        getGVRContext().setMainScene(this);
        mainApplicationScene.removeSceneObject(shortcutMenu);
        addSceneObject(shortcutMenu);
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                new GVROpacityAnimation(object, 1f, 1f).start(getGVRContext().getAnimationEngine());

            }
        }
    }

}
