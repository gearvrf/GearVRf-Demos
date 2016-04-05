package org.gearvrf.vuforiasample;

import java.io.IOException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderTexture;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;

import com.vuforia.GLTextureData;
import com.vuforia.GLTextureUnit;
import com.vuforia.ImageTarget;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;

import android.opengl.Matrix;
import android.util.Log;

public class VuforiaSampleScript extends GVRScript {

    private static final String TAG = "gvr-vuforia";

    private GVRContext gvrContext = null;
    private GVRSceneObject teapot = null;
    private GVRSceneObject passThroughObject = null;

    static final int VUFORIA_CAMERA_WIDTH = 1280;
    static final int VUFORIA_CAMERA_HEIGHT = 720;
    
    private volatile boolean init = false;

    private GVRScene mainScene;
    
    private float[] vuforiaMVMatrix;
    private float[] convertedMVMatrix;
    private float[] gvrMVMatrix;
    private float[] totalMVMatrix;

    private boolean teapotVisible = false;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        mainScene = gvrContext.getMainScene();
        mainScene.getMainCameraRig().setFarClippingDistance(20000);

        createTeaPotObject();

        vuforiaMVMatrix = new float[16];
        convertedMVMatrix = new float[16];
        gvrMVMatrix = new float[16];
        totalMVMatrix = new float[16];

        init = true;
    }

    @Override
    public void onStep() {
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    void onVuforiaInitialized() {
        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                createCameraPassThrough();
            }
        });
    }

    public boolean isInit() {
        return init;
    }

    private void createCameraPassThrough() {
        passThroughObject = new GVRSceneObject(gvrContext, 16.0f / 9.0f, 1.0f);

        passThroughObject.getTransform().setPosition(0.0f, 0.0f, -1000.0f);
        passThroughObject.getTransform().setScaleX(1000f);
        passThroughObject.getTransform().setScaleY(1000f);

        GVRTexture passThroughTexture;

        passThroughTexture = new GVRRenderTexture(gvrContext,
                VUFORIA_CAMERA_WIDTH, VUFORIA_CAMERA_HEIGHT);

        GVRRenderData renderData = passThroughObject.getRenderData();
        GVRMaterial material = new GVRMaterial(gvrContext);
        renderData.setMaterial(material);
        material.setMainTexture(passThroughTexture);
        material.setShaderType(GVRShaderType.Texture.ID);

        // the following texture coordinate values are determined empirically
        // and do not match what we expect them to be. but still they work :)
        float[] texCoords = { 0.0f, 0.0f, 0.0f, 0.70f, 0.62f, 0.0f, 0.62f, 0.7f };
        GVRMesh mesh = renderData.getMesh();
        mesh.setTexCoords(texCoords);
        renderData.setMesh(mesh);
        renderData.setDepthTest(false);

        mTextureUnit = new GLTextureUnit(0);
        GLTextureData textureData = new GLTextureData(passThroughTexture.getId());
        final boolean result = Renderer.getInstance().setVideoBackgroundTexture(textureData);
        if (!result) {
            Log.e(TAG, "Vuforia's setVideoBackgroundTexture failed");
            gvrContext.getActivity().finish();
            return;
        }

        mainScene.getMainCameraRig().addChildObject(passThroughObject);

        gvrContext.registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float frameTime) {
                Renderer.getInstance().begin();
                Renderer.getInstance().updateVideoBackgroundTexture(mTextureUnit);
                Renderer.getInstance().end();
            }
        });
    }

    private void createTeaPotObject() {
        try {
            teapot = new GVRSceneObject(gvrContext,
                    gvrContext.loadMesh(new GVRAndroidResource(gvrContext
                            .getContext(), "teapot.obj")),
                    gvrContext.loadTexture(new GVRAndroidResource(gvrContext
                            .getContext(), "teapot_tex1.jpg")));
            teapot.getRenderData().setDepthTest(false);
            teapot.getRenderData().setRenderingOrder(GVRRenderingOrder.OVERLAY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        teapot.getTransform().setPosition(0f, 0f, -0.5f);
    }

    private float[] convertMatrix = { 1f, 0f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 0f,
            -1f, 0f, 0f, 0f, 0f, 1f };

    private void showTeapot() {
        if (teapotVisible == false) {
            mainScene.addSceneObject(teapot);
            teapotVisible = true;
        }
    }

    private void hideTeapot() {
        if (teapotVisible) {
            mainScene.removeSceneObject(teapot);
            teapotVisible = false;
        }
    }

    public void updateObjectPose(State state) {
        // did we find any trackables this frame?
        int numDetectedMarkers = state.getNumTrackableResults();

        if (numDetectedMarkers == 0) {
            hideTeapot();
            return;
        }

        for (int tIdx = 0; tIdx < numDetectedMarkers; tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            if (trackable.getId() == 1 || trackable.getId() == 2) {
                Matrix44F modelViewMatrix_Vuforia = Tool
                        .convertPose2GLMatrix(result.getPose());
                vuforiaMVMatrix = modelViewMatrix_Vuforia.getData();

                Matrix.multiplyMM(convertedMVMatrix, 0, convertMatrix, 0,
                        vuforiaMVMatrix, 0);

                float scaleFactor = ((ImageTarget) trackable).getSize()
                        .getData()[0];
                Matrix.rotateM(convertedMVMatrix, 0, 90, 1, 0, 0);
                Matrix.scaleM(convertedMVMatrix, 0, scaleFactor, scaleFactor,
                        scaleFactor);

                gvrMVMatrix = gvrContext.getMainScene().getMainCameraRig()
                        .getHeadTransform().getModelMatrix();

                Matrix.multiplyMM(totalMVMatrix, 0, gvrMVMatrix, 0,
                        convertedMVMatrix, 0);
                teapot.getTransform().setModelMatrix(totalMVMatrix);

                showTeapot();
                
                break;
            } else {
                hideTeapot();
            }
        }
    }

    @SuppressWarnings("unused")
    private void showMatrix(String name, float[] matrix) {
        Log.d(TAG, name);
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[0],
                matrix[4], matrix[8], matrix[12]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[1],
                matrix[5], matrix[9], matrix[13]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[2],
                matrix[6], matrix[10], matrix[14]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[3],
                matrix[7], matrix[11], matrix[15]));
        Log.d(TAG, "\n");
    }

    private GLTextureUnit mTextureUnit;
}
