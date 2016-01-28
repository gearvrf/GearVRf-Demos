
package com.samsung.accessibility;

import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessibilityManager;

public class MainScript extends GVRScript {

    GVRSceneObject objectSeen;

    private static GVRContext mGVRContext;
    private GVRAccessibilityManager accessibilityManager;
    private GVRSceneObject object;
    private GVRGazeCursorSceneObject cursor;

    private GVRAccessibilityScene accessibilityScene;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        cursor = new GVRGazeCursorSceneObject(gvrContext);
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);

        GVRSceneObject skybox = createSkybox();
        skybox.getRenderData().setRenderingOrder(0);
        gvrContext.getMainScene().addSceneObject(skybox);

        object = new GVRSceneObject(mGVRContext, mGVRContext.createQuad(1, 1),
                mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.skybox_accessibility)));
        object.getTransform().setPosition(0, 0, -1);
        object.attachEyePointeeHolder();
        mGVRContext.getMainScene().addSceneObject(object);

        accessibilityManager = new GVRAccessibilityManager(mGVRContext,
                gvrContext.getMainScene());
        // accessibilityManager.getAccessiblityScene().setSkybox(skybox);
        accessibilityScene = new GVRAccessibilityScene(gvrContext,
                accessibilityManager);

    }

    public GVRSceneObject createSkybox() {

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
        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker
                .pickScene(mGVRContext.getMainScene());
        if (eyePointeeHolders.length == 0) {
            objectSeen = null;
        } else {
            for (GVREyePointeeHolder gvrEyePointeeHolder : eyePointeeHolders) {
                if (gvrEyePointeeHolder.getOwnerObject().equals(object)) {
                    objectSeen = gvrEyePointeeHolder.getOwnerObject();
                } else {
                    objectSeen = null;
                }
            }
        }

    }

    public void onSingleTap(MotionEvent e) {
        if (objectSeen != null) {
            mGVRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mGVRContext.setMainScene(accessibilityScene);
                    mGVRContext.getMainScene().getMainCameraRig().addChildObject(cursor);
                }
            });

        }
        if (mGVRContext.getMainScene().equals(accessibilityScene)) {
            accessibilityScene.interact();
        }
    }
}
