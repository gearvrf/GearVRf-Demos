package com.example.org.gvrfapplication;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

/**
 * The Main Scene of the App
 */
public class MainScene extends GVRMain {

    GVRCubeSceneObject mCube;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {

        //Load texture
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

        //Create a rectangle with the texture we just loaded
        GVRSceneObject quad = new GVRSceneObject(gvrContext, 4, 2, texture);
        quad.getTransform().setPosition(0, 0, -3);

        //Add rectangle to the scene
        gvrContext.getMainScene().addSceneObject(quad);

        //Create a cube
        mCube = new GVRCubeSceneObject(gvrContext);

        //Set shader for the cube
        mCube.getRenderData().setShaderTemplate(GVRPhongShader.class);

        //Set position of the cube at (0, -2, -3)
        mCube.getTransform().setPosition(0, -2, -3);

        //Add cube to the scene
        gvrContext.getMainScene().addSceneObject(mCube);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        //Rotate the cube along the Y axis
        mCube.getTransform().rotateByAxis(1, 0, 1, 0);
    }
}
