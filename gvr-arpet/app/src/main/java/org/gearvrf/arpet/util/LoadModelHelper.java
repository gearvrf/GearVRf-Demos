package org.gearvrf.arpet.util;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;

public class LoadModelHelper {
    public static String PET_MODEL_PATH = "objects/PET_ANIMATIONS_03.fbx";
    public static String BALL_MODEL_PATH = "objects/ball.fbx";

    public static GVRSceneObject loadModelSceneObject(GVRContext gvrContext, String modelPath) {
        GVRSceneObject modelSceneObject = null;
        try {
            modelSceneObject = gvrContext.getAssetLoader().loadModel(modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelSceneObject;
    }

    public static GVRSceneObject loadSceneObject(GVRContext gvrContext, String modelPath) {
        GVRSceneObject sceneObject = null;
        try {
            sceneObject = gvrContext.getAssetLoader().loadModel(modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sceneObject;
    }
}
