package com.samsung.accessibility.scene;

import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.gaze.GazeCursorSceneObject;
import com.samsung.accessibility.shortcut.ShortcutMenu;
import com.samsung.accessibility.shortcut.ShortcutMenuItem;

/**
 * {@link AccessibilityScene} is responsible for encapsulating all accessibility features interactions.<br/>
 * &nbsp; &nbsp;&nbsp; Add to scene in your project:
 * 
 * <pre>
 * GVRAccessibilityScene scene = new GVRAccessibilityScene(gvrContext);
 * gvrContextYouApplication.setMainScene(scene);
 * </pre>
 */
public class AccessibilityScene extends GVRScene {

    private GVRSceneObject mRightEyeSkyBox;
    private GVRSceneObject mLeftEyeSkyBox;
    private GVRSceneObject mBothEyesSkyBox;
    private GVRContext mGvrContext;
    private GVRScene mainApplicationScene;
    private ShortcutMenu shortcutMenu;

    /**
     * This constructor creates default scene</p>
     * 
     * <pre>
     * GVRAccessibilityScene scene = new GVRAccessibilityScene(gvrContext);
     * gvrContextYouApplication.setMainScene(scene);
     * </pre>
     * 
     * @param gvrContext
     */
    public AccessibilityScene(GVRContext gvrContext, GVRScene mainApplicationScene) {
        super(gvrContext);
        mGvrContext = gvrContext;
        this.mainApplicationScene = mainApplicationScene;
        shortcutMenu = new ShortcutMenu(mGvrContext);
        createDefaultSkyBox();
        createItems();
        backToMainScene();
    }

    /**
     * With this constructor it is possible to customize sky box thought {@link GVRSceneObject}.</br></br>
     * 
     * <pre>
     * GVRSceneObject leftScreen = new GVRSceneObject(gvrContext);
     * GVRSceneObject rightScreen = new GVRSceneObject(gvrContext);
     * 
     * GVRAccessibilityScene scene = new GVRAccessibilityScene(gvrContext,rightScreen, leftScreen);
     * gvrContextYourApplication.setMainScene(scene)
     * </pre>
     * 
     * }
     * 
     * @param gvrContext
     * @param rightEye
     * @param leftEye
     */
    public void setSkyBox(GVRSceneObject rightEyeSkyBox, GVRSceneObject leftEyeSkyBox) {
        removePreviousSkybox();
        mRightEyeSkyBox = rightEyeSkyBox;
        mLeftEyeSkyBox = leftEyeSkyBox;

        mRightEyeSkyBox.getRenderData().setRenderingOrder(0);
        mRightEyeSkyBox.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

        mLeftEyeSkyBox.getRenderData().setRenderingOrder(0);
        mLeftEyeSkyBox.getRenderData().setRenderMask(GVRRenderMaskBit.Left);

        applyShaderOnSkyBox(mRightEyeSkyBox);
        applyShaderOnSkyBox(mLeftEyeSkyBox);

        addSceneObject(mRightEyeSkyBox);
        addSceneObject(mLeftEyeSkyBox);
    }

    /**
     * With this constructor it is possible to customize sky box thought {@link GVRSceneObject}.</p>
     * 
     * <pre>
     * GVRSceneObject skyBox = new GVRSceneObject(gvrContext);
     * GVRAccessibilityScene scene = new GVRAccessibilityScene(gvrContext,skyBox);
     * gvrContextYourApplication.setMainScene(scene)
     * </pre>
     * 
     * @param gvrContext
     * @param bothEyeSkyBox
     */
    public void setSkybox(GVRSceneObject bothEyesSkyBox) {
        removePreviousSkybox();
        mBothEyesSkyBox = bothEyesSkyBox;

        mBothEyesSkyBox.getRenderData().setRenderingOrder(0);
        applyShaderOnSkyBox(mBothEyesSkyBox);
        addSceneObject(mBothEyesSkyBox);
    }

    private void removePreviousSkybox() {
        if (mBothEyesSkyBox != null) {
            removeSceneObject(mBothEyesSkyBox);
            mBothEyesSkyBox = null;
        }
        if (mRightEyeSkyBox != null) {
            removeSceneObject(mRightEyeSkyBox);
            mRightEyeSkyBox = null;
        }
        if (mLeftEyeSkyBox != null) {
            removeSceneObject(mLeftEyeSkyBox);
            mLeftEyeSkyBox = null;
        }
    }

    /**
     * Create default sky box for accessibility scene.
     * 
     * @return
     */
    private AccessibilityScene createDefaultSkyBox() {
        GVRMesh defaultMesh = getGVRContext().loadMesh(new GVRAndroidResource(getGVRContext(), R.raw.skybox_esphere_acessibility));
        GVRTexture defaultTexture = getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.skybox_accessibility));
        mBothEyesSkyBox = new GVRSceneObject(getGVRContext(), defaultMesh, defaultTexture);
        mBothEyesSkyBox.getTransform().setScale(1, 1, 1);
        mBothEyesSkyBox.getRenderData().setRenderingOrder(0);
        addSceneObject(mBothEyesSkyBox);
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
                for (int i = 0; i < shortcutItems.size(); i++) {

                    if (shortcutItems.get(i).isEmpty()) {
                        shortcutItems.get(i).createIcon(shortcutItems.get(i).getInvertedColorsIcon());
                        break;
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
                for (int i = 0; i < shortcutItems.size(); i++) {

                    if (shortcutItems.get(i).isEmpty()) {
                        shortcutItems.get(i).createIcon(shortcutItems.get(i).getZoomIn());
                        shortcutItems.get(i + 2).createIcon(shortcutItems.get(i).getZoomOut());
                        break;
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
                List<ShortcutMenuItem> shortcutItems = shortcutMenu.getShortcutItems();
                for (int i = 0; i < shortcutItems.size(); i++) {

                    if (shortcutItems.get(i).isEmpty()) {
                        shortcutItems.get(i).createIcon(shortcutItems.get(i).getTalkBackLess());
                        shortcutItems.get(i + 2).createIcon(shortcutItems.get(i).getTalkBackMore());
                        break;
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
                for (int i = 0; i < shortcutItems.size(); i++) {

                    if (shortcutItems.get(i).isEmpty()) {
                        shortcutItems.get(i).createIcon(shortcutItems.get(i).getSpeechIcon());
                        break;
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

    /**
     * Update accessibility items position to fit user's skybox and camera position.
     * 
     * @param positionX
     * @param positionY
     * @param positionZ
     */
    public void setItemsRelativePosition(float positionX, float positionY, float positionZ) {
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object instanceof SceneItem || object instanceof CursorSceneItem) {
                object.getTransform().setPosition(object.getTransform().getPositionX() + positionX, object.getTransform().getPositionY() + positionY,
                        object.getTransform().getPositionZ() + positionZ);
            }
        }
    }

    /**
     * Apply blur effect on SkyBox
     * 
     * @param skyBox
     */
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

    private void backToMainScene() {
        ShortcutMenuItem shortcutItem = shortcutMenu.getShortcutItems().get(0);
        shortcutItem.focusAndUnFocus();
        shortcutItem.createIcon(shortcutItem.getBackIcon());
        shortcutItem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                for (GVRSceneObject object : getGVRContext().getMainScene().getWholeSceneObjects()) {
                    if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                        new GVROpacityAnimation(object, 1f, 0f).start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation arg0) {
                                mGvrContext.setMainScene(mainApplicationScene);
                                mGvrContext.getMainScene().getMainCameraRig().addChildObject(GazeCursorSceneObject.getInstance(mGvrContext));
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
        addSceneObject(shortcutMenu);
    }

    public void show() {
        getGVRContext().setMainScene(this);
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                new GVROpacityAnimation(object, 1f, 1f).start(getGVRContext().getAnimationEngine());
            }
        }
    }

}
