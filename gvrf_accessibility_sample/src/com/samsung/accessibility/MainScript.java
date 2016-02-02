package com.samsung.accessibility;

import java.util.Locale;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessibilityTalkBack;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

import android.util.Log;
import android.view.MotionEvent;

import com.samsung.accessibility.focus.FocusableController;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.focus.OnFocusListener;

public class MainScript extends GVRScript {

    private static GVRContext mGVRContext;
    private FocusableSceneObject object;
    public static GVRGazeCursorSceneObject cursor;
    private GVRAccessibilityScene accessibilityScene;

    @Override
    public void onInit(final GVRContext gvrContext) {
        mGVRContext = gvrContext;
        cursor = new GVRGazeCursorSceneObject(gvrContext);
        accessibilityScene = new GVRAccessibilityScene(gvrContext, gvrContext.getMainScene());
        for (GVRSceneObject object : accessibilityScene.getWholeSceneObjects()) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                object.getRenderData().getMaterial().setOpacity(0);
            }
        }
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
        GVRSceneObject skybox = createSkybox();
        skybox.getRenderData().setRenderingOrder(0);
        gvrContext.getMainScene().addSceneObject(skybox);

        createShortCut();
        createObjectTalkBack();
        createObject1TalkBack();
        createObject2TalkBack();
    }

    private void createShortCut() {
        ShortcutMenu menu = new ShortcutMenu(mGVRContext);
        ShortcutMenuItem mainItem = menu.getMenuItems().get(7);
        mainItem.focusAndUnFocus();
        mainItem.createIcon(mainItem.getAccessibilityIcon());
        mainItem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                mGVRContext.setMainScene(accessibilityScene);
            }
        });
        mGVRContext.getMainScene().addSceneObject(menu);

    }

    private void createObjectTalkBack() {

        FocusableSceneObject object = new FocusableSceneObject(mGVRContext, mGVRContext.createQuad(.5f, .5f),
                mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.skybox_accessibility)));
        object.getTransform().setPosition(-1, 0, -1);
        object.attachEyePointeeHolder();
        object.setTalkBack(new GVRAccessibilityTalkBack(Locale.US, mGVRContext.getActivity(), "Object"));
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
                Log.e("test", "gained focus");

            }
        });
        mGVRContext.getMainScene().addSceneObject(object);
    }

    private void createObject1TalkBack() {

        FocusableSceneObject object = new FocusableSceneObject(mGVRContext, mGVRContext.createQuad(.5f, .5f),
                mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.skybox_accessibility)));
        object.getTransform().setPosition(1, 0, -1);
        object.attachEyePointeeHolder();
        object.setTalkBack(new GVRAccessibilityTalkBack(Locale.US, mGVRContext.getActivity(), "First Object"));
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
                Log.e("test", "gained focus");

            }
        });
        mGVRContext.getMainScene().addSceneObject(object);
    }

    private void createObject2TalkBack() {
        final FocusableSceneObject object = new FocusableSceneObject(mGVRContext, mGVRContext.createQuad(.5f, .5f),
                mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.skybox_accessibility)));
        object.getTransform().setPosition(0, 0, -1);
        object.attachEyePointeeHolder();
        object.setTalkBack(new GVRAccessibilityTalkBack(Locale.US, mGVRContext.getActivity(), "Second Object"));
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
                Log.e("test", "gained focus");
            }
        });
        mGVRContext.getMainScene().addSceneObject(object);
    }

    private GVRSceneObject createSkybox() {

        GVRMesh mesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext,
                R.raw.skybox_esphere_acessibility));
        GVRTexture texture = mGVRContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.skybox_accessibility));
        GVRSceneObject skybox = new GVRSceneObject(mGVRContext, mesh, texture);
        skybox.getTransform().rotateByAxisWithPivot(-90, 1, 0, 0, 0, 0, 0);
        skybox.getTransform().setPositionY(-1.6f);
        skybox.getRenderData().setRenderingOrder(0);

        // applyShaderOnSkyBox(skybox);

        return skybox;
    }

    // private void applyShaderOnSkyBox(GVRSceneObject skyBox) {
    // GVRAccessibilitySceneShader shader = new
    // GVRAccessibilitySceneShader(mGVRContext);
    // skyBox.getRenderData().getMaterial().setShaderType(shader.getShaderId());
    // skyBox.getRenderData().getMaterial().setTexture(GVRAccessibilitySceneShader.TEXTURE_KEY,
    // skyBox.getRenderData().getMaterial().getMainTexture());
    // skyBox.getRenderData().getMaterial().setFloat(GVRAccessibilitySceneShader.BLUR_INTENSITY,
    // 1);
    // }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        FocusableController.process(mGVRContext);
    }

    public void onSingleTap(MotionEvent e) {
        FocusableController.clickProcess(mGVRContext);
    }
}
