package com.samsung.accessibility.shortcut;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

import android.content.Context;
import android.media.AudioManager;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.focus.OnFocusListener;
import com.samsung.accessibility.gaze.GazeCursorSceneObject;
import com.samsung.accessibility.main.MainScript;
import com.samsung.accessibility.scene.AccessibilityScene;
import com.samsung.accessibility.util.AccessibilityTexture;

public class ShortcutMenuItem extends FocusableSceneObject {

    private GVRContext gvrContext;
    private static final int IN_FOCUS_COLOR = 8570046;
    private static final int LOST_FOCUS_COLOR = 6186095;
    private static final int CLICKED_COLOR = 12631476;
    private boolean clicked;
    private GVRSceneObject icon;
    private TypeItem typeItem;
    private AccessibilityTexture textures;

    public ShortcutMenuItem(GVRContext gvrContext) {
        super(gvrContext);
        this.gvrContext = gvrContext;
        createRenderData();
        attachEyePointeeHolder();
        getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);
        focusAndUnFocus();
        clickEvent();
    }

    private void createRenderData() {
        GVRMesh mesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.circle_menu));
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.circle_normal));
        textures = AccessibilityTexture.getInstance(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        attachRenderData(renderData);
        getRenderData().getMaterial().setMainTexture(texture);
    }

    private void focusAndUnFocus() {
        setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                if (clicked)
                    object.getRenderData().getMaterial().setColor(CLICKED_COLOR);
                else
                    object.getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);

            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                if (clicked)
                    object.getRenderData().getMaterial().setColor(CLICKED_COLOR);
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                if (typeItem != TypeItem.EMPTY)
                    object.getRenderData().getMaterial().setColor(IN_FOCUS_COLOR);
            }
        });
    }

    public void createIcon(GVRTexture iconMenu, TypeItem typeItem) {

        if (icon != null)
            removeIcon();

        icon = new GVRSceneObject(gvrContext, gvrContext.createQuad(.60f, .20f), iconMenu);
        icon.getTransform().setPosition(-0f, 0.02f, -0.7f);
        icon.getTransform().rotateByAxis(-90, 1, 0, 0);
        icon.getTransform().rotateByAxisWithPivot(245, 0, 1, 0, 0, 0, 0);
        icon.getRenderData().setRenderingOrder(GVRRenderingOrder.OVERLAY);
        getRenderData().getMaterial().setMainTexture(textures.getSpaceTexture());
        this.typeItem = typeItem;
        addChildObject(icon);
    }

    private void clickEvent() {
        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                final GVRSceneObject wholeSceneObjects[] = gvrContext.getMainScene().getWholeSceneObjects();
                switch (typeItem) {
                case TALK_BACK:

                    talkBack();
                    break;

                case BACK:

                    back(wholeSceneObjects);
                    break;

                case ZOOM:
                    zoom();
                    break;

                case INVERTED_COLORS:
                    clickEffectMenu();
                    invertedColors();

                    break;

                case SPEECH:
                    clickEffectMenu();

                    break;

                case ACCESSIBILITY:
                    accessibility(wholeSceneObjects);
                    break;
                default:
                    break;

                }

            }
        });
    }

    private void talkBack() {
        AudioManager audioManager = (AudioManager) gvrContext.getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (icon.getRenderData().getMaterial().getMainTexture().equals(textures.getTalkBackLess())) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);

        } else {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void back(final GVRSceneObject[] wholeSceneObjects) {
        final AccessibilityScene accessibilityScene = MainScript.accessibilityScene;
        for (final GVRSceneObject object : wholeSceneObjects) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                new GVROpacityAnimation(object, 1f, 0f).start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {

                        if (object.equals(wholeSceneObjects[wholeSceneObjects.length - 1])) {
                            gvrContext.setMainScene(accessibilityScene.getMainApplicationScene());
                            createIcon(textures.getAccessibilityIcon(), TypeItem.ACCESSIBILITY);
                            accessibilityScene.removeSceneObject(accessibilityScene.getShortcutMenu());
                            accessibilityScene.getMainApplicationScene().addSceneObject(accessibilityScene.getShortcutMenu());
                            gvrContext.getMainScene().getMainCameraRig()
                                    .addChildObject(GazeCursorSceneObject.getInstance(gvrContext));
                            for (GVRSceneObject object : accessibilityScene.getMainApplicationScene().getWholeSceneObjects()) {
                                if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                                    new GVROpacityAnimation(object, 1f, 1f).start(getGVRContext().getAnimationEngine());
                                }
                            }
                        }

                    }
                });
            }
        }
    }

    private void accessibility(final GVRSceneObject[] wholeSceneObjects) {
        for (final GVRSceneObject object : wholeSceneObjects) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                new GVROpacityAnimation(object, 1f, 0f).start(gvrContext.getAnimationEngine()).setOnFinish(new GVROnFinish() {

                    @Override
                    public void finished(GVRAnimation arg0) {
                        if (object.equals(wholeSceneObjects[wholeSceneObjects.length - 1])) {
                            gvrContext.getMainScene().getMainCameraRig()
                                    .removeChildObject(GazeCursorSceneObject.getInstance(gvrContext));
                            MainScript.accessibilityScene.getMainCameraRig()
                                    .addChildObject(GazeCursorSceneObject.getInstance(gvrContext));
                            createIcon(textures.getBackIcon(), TypeItem.BACK);
                            MainScript.accessibilityScene.show();
                        }
                    }
                });
            }
        }
    }

    private void invertedColors() {
        if (MainScript.manager.getInvertedColors().isInverted()) {
            MainScript.manager.getInvertedColors().turnOff(MainScript.accessibilityScene.getMainApplicationScene(),
                    MainScript.accessibilityScene);
        } else {
            MainScript.manager.getInvertedColors().turnOn(MainScript.accessibilityScene.getMainApplicationScene(),
                    MainScript.accessibilityScene);
        }
    }

    private void zoom() {
        if (icon.getRenderData().getMaterial().getMainTexture().equals(textures.getZoomIn())) {
            MainScript.manager.getZoom().zoomIn(MainScript.accessibilityScene.getMainApplicationScene(),
                    MainScript.accessibilityScene);
        } else {
            MainScript.manager.getZoom().zoomOut(MainScript.accessibilityScene.getMainApplicationScene(),
                    MainScript.accessibilityScene);
        }
    }

    private void clickEffectMenu() {
        if (!isClicked()) {
            setClicked(true);
            getRenderData().getMaterial().setColor(CLICKED_COLOR);
        } else {
            resetClick();
        }
    }

    public void resetClick() {
        setClicked(false);
        getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);
    }

    public void removeIcon() {
        typeItem = TypeItem.EMPTY;
        icon.getRenderData().getMaterial().setMainTexture(AccessibilityTexture.getInstance(gvrContext).getEmptyIcon());
    }

    public GVRSceneObject getIcon() {
        return icon;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public TypeItem getTypeItem() {
        return typeItem;
    }

    public void setTypeItem(TypeItem typeItem) {
        this.typeItem = typeItem;
    }

    public enum TypeItem {
        SPEECH, INVERTED_COLORS, TALK_BACK, ZOOM, EMPTY, BACK, ACCESSIBILITY
    }

}
