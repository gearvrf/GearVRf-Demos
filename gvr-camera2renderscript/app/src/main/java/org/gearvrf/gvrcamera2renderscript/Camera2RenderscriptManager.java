package org.gearvrf.gvrcamera2renderscript;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.renderscript.RenderScript;
import android.util.Size;
import android.view.Surface;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

import java.util.Arrays;
import java.util.List;

public class Camera2RenderscriptManager extends GVRMain {
    private GVRActivity mActivity;
    private RenderScript mRS;
    private SurfaceTexture mSurfaceTexture;
    private SurfaceTexture mEffectTexture;
    private Camera2Helper mCameraHelper;
    private RenderscriptProcessor mProcessor;
    private Surface mSurfaceInterim;

    public Camera2RenderscriptManager(GVRActivity activity) {
        mActivity = activity;
        mRS = RenderScript.create(mActivity);
    }

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        GVRScene mainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                try {
                    mCameraHelper = new Camera2Helper(mActivity, 0);
                    Size previewSize = mCameraHelper.setPreferredSize(1920, 1080);

                    mSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                    Surface surface = new Surface(mSurfaceTexture);

                    mEffectTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                    Surface effectSurface = new Surface(mEffectTexture);

                    mProcessor = new RenderscriptProcessor(mRS, previewSize.getWidth(), previewSize.getHeight());
                    mProcessor.setOutputSurface(effectSurface);
                    mSurfaceInterim = mProcessor.getInputSurface();

                    Surface[] surface_array = {surface, mSurfaceInterim};
                    List<Surface> surfaces = Arrays.asList(surface_array);
                    mCameraHelper.startCapture(surfaces);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        GVRExternalTexture passThroughTexture = new GVRExternalTexture(gvrContext);
        mSurfaceTexture = new SurfaceTexture(passThroughTexture.getId());

        GVRSceneObject passThroughObject = new GVRSceneObject(gvrContext, gvrContext.createQuad(3.0f, 1.5f), passThroughTexture);
        passThroughObject.getRenderData().getMaterial().setShaderType(GVRShaderType.OES.ID);
        passThroughObject.getTransform().setPositionY(-0.7f);
        passThroughObject.getTransform().setPositionZ(-3.0f);
        mainScene.getMainCameraRig().addChildObject(passThroughObject);

        GVRExternalTexture effectTexture = new GVRExternalTexture(gvrContext);
        mEffectTexture = new SurfaceTexture(effectTexture.getId());

        GVRSceneObject effectObject = new GVRSceneObject(gvrContext, gvrContext.createQuad(3.0f, 1.5f), effectTexture);
        effectObject.getRenderData().getMaterial().setShaderType(GVRShaderType.OES.ID);
        effectObject.getTransform().setPositionY(0.8f);
        effectObject.getTransform().setPositionZ(-3.0f);

        mainScene.getMainCameraRig().addChildObject(effectObject);
        gvrContext.registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float v) {
                mSurfaceTexture.updateTexImage();
                mEffectTexture.updateTexImage();
            }
        });
    }

    public void onPause() {
        if (mCameraHelper != null) {
            mCameraHelper.closeCamera();
        }
    }
}
