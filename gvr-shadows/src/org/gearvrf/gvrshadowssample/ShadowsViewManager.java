package org.gearvrf.gvrshadowssample;

import java.io.IOException;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
//import org.gearvrf.GVRDirectionalLight;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRLight.GVRLightType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

import android.graphics.Color;
import android.util.Log;

public class ShadowsViewManager extends GVRScript {

    private GVRContext mGVRContext = null;
    private GVRLight mLight = null;
    private GVRSceneObject stormtrooper;
    private int countTime = 0;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();

        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 0.0f);

        mLight = new GVRLight(mGVRContext, GVRLightType.DIRECTIONAL);

        scene.setDirectionalLight(mLight);

        /*
         * Create the ground. A simple textured quad. In bullet it will be a
         * plane shape with 0 mass
         */
        GVRSceneObject groundScene = quadWithTexture(100.0f, 100.0f, "floor.jpg");
        groundScene.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(groundScene);

        addSphere(scene, 1.0f, 0, 1.0f, 0.0f);
        //
        // addSphere(scene, 1.0f, -2, 1.0f, -2.0f);
        // addSphere(scene, 1.0f, -2, 1.0f, 2.0f);
        // addSphere(scene, 1.0f, 2, 1.0f, -2.0f);
        // addSphere(scene, 1.0f, 2, 1.0f, 2.0f);
        //
        addSphere(scene, 1.0f, -2, 2.0f, -2.0f);
        // addSphere(scene, 1.0f, -2, 2.0f, 2.0f);
        // addSphere(scene, 1.0f, 2, 2.0f, -2.0f);
        // addSphere(scene, 1.0f, 2, 2.0f, 2.0f);
        //
        addSphere(scene, 1.0f, -4, 2.0f, -4.0f);
        // addSphere(scene, 1.0f, -6, 2.0f, -6.0f);
        // addSphere(scene, 1.0f, -8, 2.0f, -8.0f);
        //
        addCube(scene, 2, 6f, 2, -6.0f);

        stormtrooper = addStormtrooper(scene, 0, 0.0f, -2.0f);
    }

    private long lasttime = System.currentTimeMillis();;
    private int countFrame;
    private int framerate;

    @Override
    public void onStep() {
        Log.i("Framerate: ", "" + framerate);

        countFrame++;

        if ((System.currentTimeMillis() - lasttime) > 1000) {
            lasttime = System.currentTimeMillis();
            framerate = countFrame;
            countFrame = 0;
        }

        countTime++;

        // gvrDirectionalLight.setShadowSmoothSize(1);
        // gvrDirectionalLight.setShadowGradientCenter(2);

        switch ((countTime / 3000) % 3) {
        case 0:
            double angle = countTime / 100.0;
            float x = (float) Math.sin(angle);
            float z = (float) Math.cos(angle);
            float lightDistance = 10.0f * 1;
            float lx = 0;
            float ly = 7;
            float lz = 0;
            mLight.setLightDirection(0, 0, 0);
            mLight.setSpotangle(100);
            mLight.setPosition((float) (lightDistance * x) + lx,
                    ly, (float) (lightDistance * z) + lz);

            // gvrDirectionalLight.setBoardStratifiedSampling(1);
            // gvrDirectionalLight.setShadowSmoothDistance(3f);
            break;
        case 1:
            // TODO: export parameters
            mLight.setLightDirection(0, 0, 0);
            mLight.setSpotangle(
                    (float) Math.abs(countTime / 2f % 200 - 100) * 1.2f + 1);
            mLight.setPosition(0, 7, 10);

            break;

        case 2:
            // TODO: export parameters
            mLight.setLightDirection(0, 0, 0);
            mLight.setSpotangle(100);
            mLight.setShadowSmoothSize(
                    (float) Math.abs((countTime / 100f) % 3));
            mLight.setPosition(0, 7, 10);

            break;
        }
    }

    private GVRSceneObject quadWithTexture(float width, float height, String texture) {

        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                mGVRContext.createQuad(width, height));

        GVRSceneObject object = null;

        try {
            object = new GVRSceneObject(mGVRContext, futureMesh,
                    mGVRContext.loadFutureTexture(
                            new GVRAndroidResource(mGVRContext, texture)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }

    private GVRSceneObject meshWithTexture(String mesh, String texture) {
        GVRSceneObject object = null;

        try {
            object = new GVRSceneObject(mGVRContext,
                    new GVRAndroidResource(mGVRContext, mesh),
                    new GVRAndroidResource(mGVRContext, texture));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return object;
    }

    private void addCube(GVRScene scene, float size, float x, float y, float z) {
        GVRSceneObject cubeObject = meshWithTexture("cube.obj", "cube.jpg");
        cubeObject.getTransform().setPosition(x, y, z);
        cubeObject.getTransform().setScale(size, size, size);

        scene.addSceneObject(cubeObject);
    }

    private void addSphere(GVRScene scene, float radius, float x, float y, float z) {
        GVRSceneObject sphereObject = meshWithTexture("sphere.obj", "sphere.jpg");
        sphereObject.getTransform().setPosition(x, y, z);

        scene.addSceneObject(sphereObject);
    }

    private GVRSceneObject addStormtrooper(GVRScene scene, float x, float y, float z) {
        GVRSceneObject object = meshWithTexture("storm.obj", "Stormtrooper_D.jpg");
        object.getTransform().setPosition(x, y, z);
        object.getTransform().setScale(1.5f, 1.5f, 1.5f);
        object.getTransform().setRotationByAxis((float) -90, 0, 1, 0);

        scene.addSceneObject(object);

        return object;
    }
}
