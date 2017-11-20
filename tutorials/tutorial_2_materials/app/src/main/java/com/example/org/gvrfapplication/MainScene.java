package com.example.org.gvrfapplication;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

/**
 * The Main Scene of the App
 */
public class MainScene extends GVRMain {

    GVRCubeSceneObject mCube;
    GVRSphereSceneObject mSphere;


    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {

        //Create Sphere
        mSphere = new GVRSphereSceneObject(gvrContext);
        mSphere.getTransform().setPosition(1, 0, -3);
        gvrContext.getMainScene().addSceneObject(mSphere);

        //Create Cube
        mCube = new GVRCubeSceneObject(gvrContext);
        mCube.getTransform().setPosition(-1, 0, -3);
        gvrContext.getMainScene().addSceneObject(mCube);

        /*******************
         * Assign solid color to Sphere
         ********************/
        GVRMaterial flatMaterial;
        flatMaterial = new GVRMaterial(gvrContext);
        flatMaterial.setColor(1.0f, 1.0f, 1.0f);
        mSphere.getRenderData().setMaterial(flatMaterial);


        /********************
         * Assign textured material to Cube
         *********************/
        //Load texture
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.raw.crate_wood));

        GVRMaterial textureMaterial;
        textureMaterial = new GVRMaterial(gvrContext);
        textureMaterial.setMainTexture(texture);
        mCube.getRenderData().setMaterial(textureMaterial);


        /**************************
         * Create Light
         **************************/
        GVRPointLight pointLight;
        pointLight = new GVRPointLight(gvrContext);
        pointLight.setDiffuseIntensity(0.9f, 0.7f, 0.7f, 1.0f);

        GVRSceneObject lightNode = new GVRSceneObject(gvrContext);
        lightNode.getTransform().setPosition(0,0,0);
        lightNode.attachLight(pointLight);

        gvrContext.getMainScene().addSceneObject(lightNode);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        mCube.getTransform().rotateByAxis(1, 0, 1, 0);
    }
}
