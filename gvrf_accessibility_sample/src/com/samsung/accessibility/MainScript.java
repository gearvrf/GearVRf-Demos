
package com.samsung.accessibility;

import android.view.MotionEvent;

import com.samsung.accessibility.focus.FocusableController;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnClickListener;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;

public class MainScript extends GVRScript {

    private static GVRContext mGVRContext;
    private FocusableSceneObject object;
    public static GVRGazeCursorSceneObject cursor;

    @Override
    public void onInit(final GVRContext gvrContext) {
        mGVRContext = gvrContext;
        cursor = new GVRGazeCursorSceneObject(gvrContext);
        final GVRAccessibilityScene accessibilityScene = new GVRAccessibilityScene(gvrContext, gvrContext.getMainScene());
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
        GVRSceneObject skybox = createSkybox();
        skybox.getRenderData().setRenderingOrder(0);
        gvrContext.getMainScene().addSceneObject(skybox);

        object = new FocusableSceneObject(mGVRContext, mGVRContext.createQuad(1, 1),
                mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                        R.drawable.skybox_accessibility)));
        object.getTransform().setPosition(0, 0, -1);
        object.attachEyePointeeHolder();
        mGVRContext.getMainScene().addSceneObject(object);
        object.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                mGVRContext.getMainScene().getMainCameraRig().removeChildObject(cursor);
                accessibilityScene.getMainCameraRig().addChildObject(cursor);
                mGVRContext.setMainScene(accessibilityScene);
            }
        });

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
        FocusableController.process(mGVRContext);
    }

    public void onSingleTap(MotionEvent e) {
        FocusableController.clickProcess(mGVRContext);
    }
}
