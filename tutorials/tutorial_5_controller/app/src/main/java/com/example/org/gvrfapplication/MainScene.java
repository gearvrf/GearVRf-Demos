package com.example.org.gvrfapplication;

import android.util.Log;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;

import java.util.ArrayList;

/**
 * The Main Scene of the App
 */
public class MainScene extends GVRMain {

    private GVRContext mContext;
    private GVRScene mMainScene;
    private static final float DEPTH = -1.5f;

    //Listener for add/removal of a controller
    private CursorControllerListener listener = new CursorControllerListener() {

        private GVRSceneObject cursor;
        private ArrayList<GVRCursorController> controllerList = new ArrayList<GVRCursorController>();

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {

            //Gaze Controller
            if (controller.getControllerType() == GVRControllerType.GAZE) {

                //Add controller cursor
                cursor = new GVRSceneObject(mContext,
                        mContext.createQuad(0.1f, 0.1f),
                        mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.cursor))
                );
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mMainScene.getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);

                //Set controller position
                controller.setPosition(0.0f, 0.0f, DEPTH);
                controller.setNearDepth(DEPTH);
                controller.setFarDepth(DEPTH);
            } else {
                // disable all other types
                controller.setEnable(false);
            }

            controllerList.add(controller);
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getControllerType() == GVRControllerType.GAZE) {
                if (cursor != null) {
                    mMainScene.getMainCameraRig().removeChildObject(cursor);
                }
                controller.setEnable(false);
            }
        }
    };

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {

        mContext = gvrContext;
        mMainScene = gvrContext.getMainScene();

        //Load texture
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

        //Create a rectangle with the texture we just loaded
        GVRSceneObject quad = new GVRSceneObject(gvrContext, 4, 2, texture);
        quad.getTransform().setPosition(0, 0, -3);

        //Add rectangle to the scene
        gvrContext.getMainScene().addSceneObject(quad);

        //Listen controller events
        GVRInputManager input = gvrContext.getInputManager();
        input.addCursorControllerListener(listener);

        Log.i("GUI", "Add Controller Listener");
        for (GVRCursorController cursor : input.getCursorControllers()) {
            listener.onCursorControllerAdded(cursor);
        }
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {
        //Add update logic here
    }
}

