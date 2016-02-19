package com.samsung.accessibility.main;

import java.util.EnumSet;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;

import android.view.MotionEvent;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableController;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.gaze.GazeCursorSceneObject;
import com.samsung.accessibility.scene.AccessibilityScene;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;
import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilityManager;
import com.samsung.accessibility.util.AccessibilityTexture;

public class MainScript extends GVRScript {

    private GVRContext gvrContext;

    private GazeCursorSceneObject cursor;
    public static AccessibilityScene accessibilityScene;
    public static AccessibilityManager manager;

    @Override
    public void onInit(final GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        AccessibilityTexture.getInstance(gvrContext);
        cursor = GazeCursorSceneObject.getInstance(gvrContext);
        manager = new AccessibilityManager(gvrContext);
        ShortcutMenu shortcutMenu = createShortCut();
        accessibilityScene = new AccessibilityScene(gvrContext, gvrContext.getMainScene(), shortcutMenu);

        createPedestalObject();
        createDinossaur();

        gvrContext.getMainScene().addSceneObject(shortcutMenu);
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
        gvrContext.getMainScene().addSceneObject(createSkybox());

    }

    private ShortcutMenu createShortCut() {
        ShortcutMenu shortcuteMenu = new ShortcutMenu(gvrContext);
        ShortcutMenuItem shortcuteItem = shortcuteMenu.getShortcutItems().get(0);
        shortcuteMenu.getTransform().setPositionY(-.5f);
        shortcuteItem.createIcon(AccessibilityTexture.getInstance(gvrContext).getAccessibilityIcon(), TypeItem.ACCESSIBILITY);
        return shortcuteMenu;
    }

    private void createPedestalObject() {
        GVRMesh baseMesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.base));
        GVRMesh bookMesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.book));
        GVRTexture bookTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.book));
        GVRTexture baseTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.base));

        FocusableSceneObject baseObject = new FocusableSceneObject(gvrContext, baseMesh, baseTexture);
        FocusableSceneObject bookObject = new FocusableSceneObject(gvrContext, bookMesh, bookTexture);

        baseObject.getTransform().setScale(0.005f, 0.005f, 0.005f);
        bookObject.getTransform().setScale(0.005f, 0.005f, 0.005f);

        baseObject.getTransform().setPosition(0, -1.6f, -2);
        bookObject.getTransform().setPosition(0, -1.6f, -2);

        gvrContext.getMainScene().addSceneObject(bookObject);
        gvrContext.getMainScene().addSceneObject(baseObject);
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

    private void createDinossaur() {

        EnumSet<GVRImportSettings> additionalSettings = EnumSet
                .of(GVRImportSettings.CALCULATE_SMOOTH_NORMALS);

        EnumSet<GVRImportSettings> settings = GVRImportSettings
                .getRecommendedSettingsWith(additionalSettings);

        GVRMesh baseMesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.trex_mesh), settings);
        GVRTexture baseTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.trex_tex_diffuse));
        FocusableSceneObject apatosaurus = new FocusableSceneObject(gvrContext, baseMesh, baseTexture);
        apatosaurus.getTransform().setPosition(0, -1.6f, -7f);
        apatosaurus.getTransform().rotateByAxis(-90, 1, 0, 0);
        apatosaurus.getTransform().rotateByAxis(90, 0, 1, 0);
        gvrContext.getMainScene().addSceneObject(apatosaurus);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        FocusableController.process(gvrContext);
    }

    public void onSingleTap(MotionEvent e) {
        FocusableController.clickProcess(gvrContext);
    }
}
