package com.example.org.gvrfapplication;

import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class MainActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMain(new Main(), "gvr.xml");
    }

    private final class Main extends GVRMain {
        @Override
        public void onInit(GVRContext gvrContext) throws Throwable {
            GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

            GVRSceneObject quad = new GVRSceneObject(gvrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -3);

            gvrContext.getMainScene().addSceneObject(quad);
        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.NONE;
        }

        @Override
        public void onStep() {
        }
    }
}
