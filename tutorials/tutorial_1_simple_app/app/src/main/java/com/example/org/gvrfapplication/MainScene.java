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

    GVRSceneObject mCube;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {

        //Create a cube
        mCube = new GVRCubeSceneObject(gvrContext);

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
