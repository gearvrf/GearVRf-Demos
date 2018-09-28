/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
