package org.gearvrf.blurfilter;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRRenderTarget;
import org.gearvrf.GVRRenderTexture;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import org.gearvrf.utility.Log;


public class TestMain extends GVRMain
{
    private GVRContext mContext;
    private GVRSwitch mSwitch;
    private GVRRenderTexture mRenderTexture;

    public void onInit(GVRContext context)
    {
        mContext = context;
        GVRScene scene = context.getMainScene();
        GVRCameraSceneObject cameraObject = null;

        //
        // Create a camera scene object.
        // This step will fail if your camera cannot be accessed.
        //
        try
        {
            cameraObject = new GVRCameraSceneObject(context, 3.6f, 2.0f);
            cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
        }
        catch (GVRCameraSceneObject.GVRCameraAccessException e)
        {
            // Cannot open camera
            Log.e("Test", "Cannot open the camera",e);
            context.getActivity().finish();
        }
        //
        // Make a scene object to show the blurred camera view.
        // It is the same size as the camera scene object but will
        // used the blurred camera texture instead of the real one.
        // The texture in it's material will be substituted with
        // the blurred camera texture by "createBlurTexture"
        //
        final GVRMaterial cameraMtl = cameraObject.getRenderData().getMaterial();
        final GVRTexture cameraTex = cameraMtl.getMainTexture();
        final GVRMaterial blurryMtl = createDisplayScene(scene, cameraTex);
        //
        // Continuously render a scene which shows the blurred camera texture on a
        // full-screen quad.
        //
        context.runOnGlThread(new Runnable()
        {
            public void run()
            {
                createBlurTexture(cameraTex, blurryMtl);
            }
        });
    }

    //
    // Creates a blurred version of the camera texture.
    // We make a material which uses the texture from the camera
    // but employs a custom blur shader instead of the OESShader.
    // The quad which will be blurred is added to the scene
    // which renders to the blurred texture - not the main scene.
    //
    public void createBlurTexture(GVRTexture texture, final GVRMaterial material)
    {
        GVRScene blurScene = new GVRScene(mContext);
        GVRSceneObject blurryQuad = createBlurScene(blurScene, texture);
        blurScene.getMainCameraRig().addChildObject(blurryQuad);
        //
        // Set up a texture to render into and a GVRRenderTarget
        // to initiate rendering into the texture every frame.
        //
        mRenderTexture = new GVRRenderTexture(mContext, 1024, 1024);
        GVRRenderTarget renderTarget = new GVRRenderTarget(mRenderTexture, blurScene);
        blurScene.getMainCameraRig().getOwnerObject().attachComponent(renderTarget);
        material.setMainTexture(mRenderTexture);
        renderTarget.setEnable(true);
    }

    /*
     * Create a scene object which is a full screen quad that produces a blurry version
     * of the input texture when rendered. This quad has two render passes, each of which
     * each of which performs a gaussian blur in a single direction.
     */
    public GVRSceneObject createBlurScene(GVRScene scene, GVRTexture texture)
    {
        final GVRSceneObject blurryQuad = new GVRSceneObject(mContext, 2.0f, 2.0f, texture, GVRMaterial.GVRShaderType.OES.ID);
        GVRCamera camera = scene.getMainCameraRig().getCenterCamera();

        GVRMaterial horzBlurMtl = new GVRMaterial(mContext, new GVRShaderId(HorzBlurShader.class));
        horzBlurMtl.setFloat("u_resolution", 1024.0f);
        camera.addPostEffect(horzBlurMtl);

        GVRMaterial vertBlurMtl = new GVRMaterial(mContext, new GVRShaderId(VertBlurShader.class));
        vertBlurMtl.setFloat("u_resolution", 1024.0f);
        camera.addPostEffect(vertBlurMtl);

        blurryQuad.getTransform().setPositionZ(-0.1f);
        return blurryQuad;
    }

    //
    // Make two scene objects to show the normal and blurred camera views.
    // The blurred view uses the blurred camera texture instead of the real one.
    // The texture in it's material will be substituted with
    // the blurred camera texture by "createBlurTexture"
    //
    public GVRMaterial createDisplayScene(GVRScene scene, GVRTexture cameraTex)
    {
        GVRSceneObject normalCamera = new GVRSceneObject(mContext, 3.6f, 2.0f, cameraTex, GVRMaterial.GVRShaderType.OES.ID);
        GVRTexture tempTex = getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.checker));
        GVRSceneObject blurryCamera = new GVRSceneObject(mContext, 3.6f, 2.0f, tempTex);
        final GVRMaterial blurryMtl = blurryCamera.getRenderData().getMaterial();
        GVRSceneObject cameraRoot = new GVRSceneObject(mContext);

        blurryCamera.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
        normalCamera.getTransform().setPositionX(-2);
        blurryCamera.getTransform().setPositionX(2);
        cameraRoot.getTransform().setPosition(0.0f, 0.0f, -4.0f);
        cameraRoot.addChildObject(normalCamera);	// index 0 = normal camera
        cameraRoot.addChildObject(blurryCamera);	// index 1 = blurry camera
        scene.getMainCameraRig().addChildObject(cameraRoot);
        return blurryMtl;
    }
}