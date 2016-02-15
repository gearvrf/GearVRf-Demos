package com.samsung.accessibility.main;

import java.util.Locale;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
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
        for (GVRSceneObject object : accessibilityScene.getWholeSceneObjects()) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                object.getRenderData().getMaterial().setOpacity(0);
            }
        }
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
        gvrContext.getMainScene().addSceneObject(createSkybox());

        createObjectTalkBack();
        createObject1TalkBack();
        createObject2TalkBack();
        gvrContext.getMainScene().addSceneObject(shortcutMenu);
    }

    private ShortcutMenu createShortCut() {
        ShortcutMenu shortcuteMenu = new ShortcutMenu(gvrContext);
        ShortcutMenuItem shortcuteItem = shortcuteMenu.getShortcutItems().get(0);
        shortcuteMenu.getTransform().setPositionY(-.5f);
        shortcuteItem.createIcon(AccessibilityTexture.getInstance(gvrContext).getAccessibilityIcon(), TypeItem.ACCESSIBILITY);
        return shortcuteMenu;
    }

    private void createObjectTalkBack() {

        FocusableSceneObject object = new FocusableSceneObject(gvrContext, gvrContext.createQuad(.5f, .5f),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.skybox_accessibility)));

        GVRAccessibilityTalkBack talkBack = new GVRAccessibilityTalkBack(Locale.US, gvrContext.getActivity(), "Object");
        object.getTransform().setPosition(-1, 0, -1);
        object.attachEyePointeeHolder();
        object.setTalkBack(talkBack);
        object.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {

            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                // TODO Auto-generated method stub

            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                object.getTalkBack().speak();
            }
        });
        gvrContext.getMainScene().addSceneObject(object);
    }

    private void createObject1TalkBack() {

        final FocusableSceneObject object1 = new FocusableSceneObject(gvrContext, gvrContext.createQuad(.5f, .5f),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.skybox_accessibility)));
        GVRAccessibilityTalkBack talkBack = new GVRAccessibilityTalkBack(Locale.US, gvrContext.getActivity(), "Object 2");
        object1.getTransform().setPosition(1, 0, -1);
        object1.attachEyePointeeHolder();
        object1.setTalkBack(talkBack);
        object1.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                // TODO Auto-generated method stub

            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                object1.getTalkBack().speak();
            }
        });
        gvrContext.getMainScene().addSceneObject(object1);
    }

    private void createObject2TalkBack() {
        final FocusableSceneObject object = new FocusableSceneObject(gvrContext, gvrContext.createQuad(.5f, .5f),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.skybox_accessibility)));

        GVRAccessibilityTalkBack talkBack = new GVRAccessibilityTalkBack(Locale.US, gvrContext.getActivity(), "Object 3");
        object.getTransform().setPosition(0, 0, -1);
        object.attachEyePointeeHolder();
        object.setTalkBack(talkBack);
        object.setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {

            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                // TODO Auto-generated method stub

            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                object.getTalkBack().speak();
            }
        });
        gvrContext.getMainScene().addSceneObject(object);
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
