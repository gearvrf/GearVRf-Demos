package com.example.org.gvrfapplication;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;

/**
 * The Main Scene of the App
 */
public class MainScene extends GVRMain {

    private GVRVideoPlayerObject mPlayerObj = null;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {

        mPlayerObj = new GVRVideoPlayerObject(gvrContext);
        mPlayerObj.loadVideo("videos_s_3.mp4");
        mPlayerObj.setLooping(true);
        mPlayerObj.play();

        gvrContext.getMainScene().addSceneObject(mPlayerObj);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        //Add update logic here
    }

    public void onResume() {
        if(mPlayerObj != null)
            mPlayerObj.onResume();
    }
}
