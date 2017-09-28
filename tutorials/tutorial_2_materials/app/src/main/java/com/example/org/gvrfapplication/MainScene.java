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

        //Load texture
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.raw.crate_wood));

        /*******************
         * Create Sphere with flat material
         ********************/
        GVRMaterial flatMaterial;
        flatMaterial = new GVRMaterial(gvrContext);
        flatMaterial.setColor(1.0f, 1.0f, 1.0f);

        //Create Sphere with solid color
        mSphere = new GVRSphereSceneObject(gvrContext);
        mSphere.getTransform().setPosition(2, 0, -3);
        mSphere.getRenderData().setMaterial(flatMaterial);
        mSphere.getRenderData().setShaderTemplate(GVRPhongShader.class);
        gvrContext.getMainScene().addSceneObject(mSphere);

        /********************
         * Create Cube with textured material
         *********************/
        GVRMaterial textureMaterial;
        textureMaterial = new GVRMaterial(gvrContext);
        textureMaterial.setMainTexture(texture);
        textureMaterial.setTexture("diffuseTexture", texture);

        mCube = new GVRCubeSceneObject(gvrContext);
        mCube.getTransform().setPosition(-2, 0, -3);
        mCube.getRenderData().setMaterial(textureMaterial);
        mCube.getRenderData().setShaderTemplate(GVRPhongShader.class);
        gvrContext.getMainScene().addSceneObject(mCube);


        /**************************
         * Create Light
         **************************/
        GVRPointLight pointLight;
        pointLight = new GVRPointLight(gvrContext);
        pointLight.setDiffuseIntensity(0.8f, 0.5f, 0.5f, 1.0f);
        pointLight.setSpecularIntensity(0.5f, 0.5f, 0.5f, 1.0f);

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
