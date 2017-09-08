package com.example.org.gvrfapplication;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.scene_objects.GVRModelSceneObject;

import java.util.concurrent.Future;

/**
 * The Main Scene of the App
 */
public class MainScene extends GVRMain {

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        GVRMesh dinoMesh = gvrContext.getAssetLoader().loadMesh(
                new GVRAndroidResource(gvrContext, R.raw.trex_mesh)
        );

        GVRTexture dinoTexture = gvrContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(gvrContext, R.raw.trex_tex_diffuse)
        );

        GVRSceneObject dinoObj = new GVRSceneObject(gvrContext, dinoMesh, dinoTexture);

        dinoObj.getTransform().setPosition(5,0,-10);
        dinoObj.getTransform().rotateByAxis(-90, 1f, 0f, 0f);
        gvrContext.getMainScene().addSceneObject(dinoObj);

        //Load animated model
        GVRModelSceneObject character = gvrContext.getAssetLoader().loadModel("astro_boy.dae");
        character.getTransform().setRotationByAxis(45.0f, 0.0f, 1.0f, 0.0f);
        character.getTransform().setScale(3, 3, 3);
        character.getTransform().setPosition(0.0f, -0.4f, -0.5f);
        gvrContext.getMainScene().addSceneObject(character);

        final GVRAnimator animator = (GVRAnimator)character.getComponent(GVRAnimator.getComponentType());
        animator.setRepeatCount(-1);
        animator.setRepeatMode(GVRRepeatMode.REPEATED);
        animator.start();
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
