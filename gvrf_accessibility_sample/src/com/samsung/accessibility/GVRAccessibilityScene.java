
package com.samsung.accessibility;

import com.samsung.accessibility.focus.OnClickListener;

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

/**
 * {@link GVRAccessibilityScene} is responsible for encapsulating all
 * accessibility features interactions.<br/>
 * &nbsp; &nbsp;&nbsp; Add to scene in your project:
 * 
 * <pre>
 * GVRAccessibilityScene scene = new GVRAccessibilityScene(gvrContext);
 * gvrContextYouApplication.setMainScene(scene);
 * </pre>
 */
public class GVRAccessibilityScene extends GVRScene {

    private GVRSceneObject mRightEyeSkyBox;
    private GVRSceneObject mLeftEyeSkyBox;
    private GVRSceneObject mBothEyesSkyBox;
    private GVRContext mGvrContext;
    private GVRScene mainApplicationScene;
    private ShortcutMenu shortCurtMenu;

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
    public GVRAccessibilityScene(GVRContext gvrContext, GVRScene mainApplicationScene) {
        super(gvrContext);
        mGvrContext = gvrContext;
        this.mainApplicationScene = mainApplicationScene;
        shortCurtMenu = new ShortcutMenu(mGvrContext);
        createDefaultSkyBox();
        createItems();
        backToMainScene();
    }

    /**
     * With this constructor it is possible to customize sky box thought
     * {@link GVRSceneObject}.</br></br>
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
     * With this constructor it is possible to customize sky box thought
     * {@link GVRSceneObject}.</p>
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
    private GVRAccessibilityScene createDefaultSkyBox() {
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
        final GVRAccessibilityItem invertedColors = new GVRAccessibilityItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.inverted_colors)));
        invertedColors.getTransform().setPosition(positionX, positionY, positionZ);
        invertedColors.getTransform().setScale(scale, scale, scale);
        invertedColors.attachEyePointeeHolder();
        invertedColors.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                invertedColors.animate();

            }
        });
        this.addSceneObject(invertedColors);

        final GVRAccessibilityItem zoom = new GVRAccessibilityItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.zoom)));
        zoom.getTransform().setPosition(positionX, positionY, positionZ);
        zoom.getTransform().setScale(scale, scale, scale);
        zoom.attachEyePointeeHolder();
        zoom.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                zoom.animate();
            }
        });
        this.addSceneObject(zoom);

        final GVRAccessibilityItem talkBack = new GVRAccessibilityItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.talk_back)));
        talkBack.getTransform().setPosition(positionX, positionY, positionZ);
        talkBack.getTransform().setScale(scale, scale, scale);
        talkBack.attachEyePointeeHolder();
        talkBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                talkBack.animate();
            }
        });

        this.addSceneObject(talkBack);

        final GVRAccessibilityItem speech = new GVRAccessibilityItem(getGVRContext(), mesh, getGVRContext()
                .loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.speech)));
        speech.getTransform().setPosition(positionX, positionY, positionZ);
        speech.getTransform().setScale(scale, scale, scale);
        speech.attachEyePointeeHolder();
        speech.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                speech.animate();
            }
        });

        this.addSceneObject(speech);

        final GVRAccessibilityItem captions = new GVRAccessibilityItem(getGVRContext(), mesh, getGVRContext()
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

        GVRAccessibilityCursorItem cursor = new GVRAccessibilityCursorItem(getGVRContext(), mesh, getGVRContext()
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
     * Update accessibility items position to fit user's skybox and camera
     * position.
     * 
     * @param positionX
     * @param positionY
     * @param positionZ
     */
    public void setItemsRelativePosition(float positionX, float positionY, float positionZ) {
        for (GVRSceneObject object : getWholeSceneObjects()) {
            if (object instanceof GVRAccessibilityItem || object instanceof GVRAccessibilityCursorItem) {
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
        GVRAccessibilitySceneShader shader = new GVRAccessibilitySceneShader(mGvrContext);
        applyShader(shader, skyBox);
        for (GVRSceneObject object : skyBox.getChildren()) {
            applyShader(shader, object);
        }
    }

    private void applyShader(GVRAccessibilitySceneShader shader, GVRSceneObject object) {
        if (object != null && object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
            object.getRenderData().getMaterial().setShaderType(shader.getShaderId());
            object.getRenderData().getMaterial().setTexture(GVRAccessibilitySceneShader.TEXTURE_KEY,
                    object.getRenderData().getMaterial().getMainTexture());
            object.getRenderData().getMaterial().setFloat(GVRAccessibilitySceneShader.BLUR_INTENSITY, 1);
        }
    }

    private void backToMainScene() {
        ShortcutMenuItem mainItem = shortCurtMenu.getMenuItems().get(7);
        mainItem.focusAndUnFocus();
        mainItem.createIcon(mainItem.getBackIcon());
        mainItem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                for (GVRSceneObject object : getGVRContext().getMainScene().getWholeSceneObjects()) {
                    if (object.getRenderData() != null && object.getRenderData().getMaterial() != null) {
                        new GVROpacityAnimation(object, 1f, 0f).start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation arg0) {
                                mGvrContext.setMainScene(mainApplicationScene);
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
        addSceneObject(shortCurtMenu);
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
