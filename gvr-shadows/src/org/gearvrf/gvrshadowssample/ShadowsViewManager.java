package org.gearvrf.gvrshadowssample;

import java.io.IOException;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectionalLight;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
//import org.gearvrf.animation.GVRAnimation;
//import org.gearvrf.animation.GVRAnimationEngine;
//import org.gearvrf.animation.GVRRepeatMode;
//import org.gearvrf.scene_objects.GVRModelSceneObject;
//import org.gearvrf.GVRDirectionalLight.LightRenderMode;

import android.graphics.Color;
import android.util.Log;

public class ShadowsViewManager extends GVRScript {

    private GVRContext mGVRContext = null;

    int countTime = 0;

    private GVRDirectionalLight gvrDirectionalLight;

    private GVRSceneObject stormtrooper;

//    private GVRModelSceneObject dino;

//    private GVRAnimationEngine mAnimationEngine;
//    private GVRAnimation mAssimpAnimation;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;

         GVRScene scene = mGVRContext.getNextMainScene();
//        mAnimationEngine = gvrContext.getAnimationEngine();
//
//        GVRScene scene = gvrContext.getNextMainScene(new Runnable() {
//            @Override
//            public void run() {
//                mAssimpAnimation.start(mAnimationEngine);
//            }
//        });

        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);

        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 0.0f);

        gvrDirectionalLight = new GVRDirectionalLight(gvrContext);

        gvrDirectionalLight.setBias(0.001f);
        // 0.001f
        scene.setDirectionalLight(gvrDirectionalLight); // / Add Shadow Map
                                                        // light
                                                        // / // TODO method

        /*
         * Create the ground. A simple textured quad. In bullet it will be a
         * plane shape with 0 mass
         */
        GVRSceneObject groundScene = quadWithTexture(100.0f, 100.0f,
                "floor.jpg");
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

//        try {
//            dino = addDinoAnim(scene, .5f, 0, 1.0f, -2.0f);
//            mAssimpAnimation = dino.getAnimations().get(0);
//            mAssimpAnimation.setRepeatMode(GVRRepeatMode.REPEATED)
//                    .setRepeatCount(-1);
//
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
    }

    long lasttime = System.currentTimeMillis();;
    int countFrame;
    int framerate;

    @Override
    public void onStep() {

        Log.i("Framerate", "" + framerate);
        countFrame++;
        if (System.currentTimeMillis() - lasttime > 1000) {
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
            float lz = 0; // //
            gvrDirectionalLight.setLightPosition((float) (lightDistance * x)
                    + lx, ly, (float) (lightDistance * z) + lz);
            gvrDirectionalLight.setLightDirection(0, 0, 0);
            gvrDirectionalLight.setSpotangle(100);

            // //// Cartoon mode ////
            // gvrDirectionalLight.setShadowSmoothSize(0);
            // gvrDirectionalLight.setShadowGradientCenter(0);
            // gvrDirectionalLight.setLightAmbientOnShadow(.1f);
            // gvrDirectionalLight.setLightingShade(1f);
            // //////////////////////

            // gvrDirectionalLight.setBoardStratifiedSampling(1);
            // gvrDirectionalLight.setShadowSmoothDistance(3f);

            break;
        case 1:
            gvrDirectionalLight.setLightPosition(0, 7, 10);
            gvrDirectionalLight.setLightDirection(0, 0, 0);
            gvrDirectionalLight.setSpotangle((float) Math
                    .abs(countTime / 2f % 200 - 100) * 1.2f + 1);
            break;

        case 2:
            gvrDirectionalLight.setLightPosition(0, 7, 10);
            gvrDirectionalLight.setLightDirection(0, 0, 0);
            gvrDirectionalLight.setSpotangle(100);
            gvrDirectionalLight.setShadowSmoothSize((float) Math
                    .abs((countTime / 100f) % 3));
            break;
        }
    }

    private GVRSceneObject quadWithTexture(float width, float height,
            String texture) {
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                mGVRContext.createQuad(width, height));
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, futureMesh,
                    mGVRContext.loadFutureTexture(new GVRAndroidResource(
                            mGVRContext, texture)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private GVRSceneObject meshWithTexture(String mesh, String texture) {
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, new GVRAndroidResource(
                    mGVRContext, mesh), new GVRAndroidResource(mGVRContext,
                    texture));
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

    private void addSphere(GVRScene scene, float radius, float x, float y,
            float z) {
        GVRSceneObject sphereObject = meshWithTexture("sphere.obj",
                "sphere.jpg");
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
//
//    private GVRModelSceneObject addDinoAnim(GVRScene scene, float scale,
//            float x, float y, float z) throws IOException {
//        // GVRSceneObject object = meshWithTexture("TRex_NoGround.fbx",
//        // "t_rex_texture_diffuse.png");
//        GVRModelSceneObject object = mGVRContext.loadModel("TRex_NoGround.fbx");
//        object.getTransform().setPosition(x, y, z);
//        object.getTransform().setScale(scale, scale, scale);
//        object.getTransform().setRotationByAxis((float) -90, 0, 1, 0);
//        object.getTransform().setRotationByAxis((float) -45, 1, 0, 0);
//        object.getTransform().rotateByAxis(+90, 0, 1, 0);
//        scene.addSceneObject(object);
//        return object;
//    }
}
