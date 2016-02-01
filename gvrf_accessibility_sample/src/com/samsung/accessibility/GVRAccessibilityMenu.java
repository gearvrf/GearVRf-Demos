
package com.samsung.accessibility;

import com.samsung.accessibility.focus.OnClickListener;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessibilityManager;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

public class GVRAccessibilityMenu extends GVRSceneObject {

    private GVRContext mGvrContext;
    private float angle = 45;
    private GVRScene scene;
    private GVRAccessibilityManager manager;
    private SceneType sceneType;
    private static final int LOST_FOCUS_COLOR = 6186095;
    private static final int CLICKED_COLOR = 12631476;

    public GVRAccessibilityMenu(GVRContext gvrContext, GVRScene scene, SceneType sceneType) {
        super(gvrContext);
        this.getTransform().rotateByAxis(20, 0, 1, 0);
        mGvrContext = gvrContext;
        int multiplier = 2;
        this.sceneType = sceneType;

        this.scene = scene;
        manager = new GVRAccessibilityManager(getGVRContext());
        createBackButton(multiplier++ * angle);
        createGazeButton(multiplier++ * angle);
        createZoomButton(multiplier++ * angle);
        createCaptionsButton(multiplier++ * angle);
        createDefaultSpace(multiplier++ * angle);
        createTalkBackButton(multiplier++ * angle);
        createSpeechButton(multiplier++ * angle);
        createInvertedColorsButton(multiplier++ * angle);
    }

    private void createBackButton(float degree) {
        GVRTexture icon = null;
        if (isAccessibilityScene())
            icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_accessibility));
        else
            icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_back));

        final GVRAccessibilityMenuItem mainButton = new GVRAccessibilityMenuItem(mGvrContext, icon);
        mainButton.getTransform().setPosition(0, -1f, 0);
        mainButton.getTransform().rotateByAxis(degree, 0, 1, 0);
        mainButton.attachEyePointeeHolder();
        mainButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                item.getRenderData().getMaterial().setColor(12631476);
                for (GVRSceneObject object : getGVRContext().getMainScene().getWholeSceneObjects()) {
                    if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                        new GVROpacityAnimation(object, 1f, 0f).start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation arg0) {
                                mGvrContext.setMainScene(scene);
                                mGvrContext.getMainScene().getMainCameraRig().addChildObject(MainScript.cursor);
                                for (GVRSceneObject object : mainApplicationScene.getWholeSceneObjects()) {
                                    if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                                        new GVROpacityAnimation(object, 1f, 1f).start(getGVRContext().getAnimationEngine());
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        addChildObject(item);
    }

    private void createGazeButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_cursor));
        final GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.attachEyePointeeHolder();
        item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                clickEffectMenu(item);

            }
        });
        addChildObject(item);
    }

    private void createZoomButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_zoom));
        final GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.attachEyePointeeHolder();
        item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                clickEffectMenu(item);

            }
        });
        addChildObject(item);
    }

    private void createCaptionsButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_captions));
        final GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.attachEyePointeeHolder();
        item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                clickEffectMenu(item);

            }
        });
        addChildObject(item);
    }

    private void createInvertedColorsButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_inverted));
        final GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.attachEyePointeeHolder();
        item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                clickEffectMenu(item);
                manager.getInvertedColors().switchState(scene);
            }
        });
        addChildObject(item);
    }

    private void createTalkBackButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_talk_back));
        final GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.attachEyePointeeHolder();
        item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                clickEffectMenu(item);

            }
        });
        addChildObject(item);
    }

    private void createSpeechButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_speech));
        final GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.attachEyePointeeHolder();
        item.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                clickEffectMenu(item);

            }
        });
        addChildObject(item);
    }

    private void createDefaultSpace(float degree) {
        GVRMesh slotMesh = mGvrContext.loadMesh(new GVRAndroidResource(mGvrContext, R.raw.circle_menu));
        GVRTexture spacerTexture = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.circle_normal_alpha));
        GVRSceneObject item = new GVRSceneObject(mGvrContext, slotMesh, spacerTexture);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.getRenderData().getMaterial().setColor(6186095);
        addChildObject(item);
    }

    private void clickEffectMenu(GVRAccessibilityMenuItem item) {
        if (!item.isClicked()) {
            item.setClicked(true);
            item.getRenderData().getMaterial().setColor(CLICKED_COLOR);
        } else {
            item.setClicked(false);
            item.getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);
        }
    }

    private boolean isAccessibilityScene() {

        if (sceneType == SceneType.ACCESSIBILITY_SCENE)
            return true;
        return false;
    }

    public enum SceneType {
        MAIN_SCENE_APPLICATION, ACCESSIBILITY_SCENE;
    }
}
