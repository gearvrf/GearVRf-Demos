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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;
import java.io.InputStream;

public class LoadModelHelper {
    public static String PET_GRAB_PIVOT = "m_DnLip_JNT";
    public static String PET_MODEL_PATH = "objects/pet_model.dae";
    public static String BALL_MODEL_PATH = "objects/bone_model.obj";
    public static String PET_BONES_MAP_PATH = "objects/anim/pet_skeleton_map.txt";
    public static String[] PET_ANIMATIONS_PATH = {
            "objects/anim/pet_anim_idle.bvh",
            "objects/anim/pet_anim_walk.bvh",
            "objects/anim/pet_anim_run.bvh",
            "objects/anim/pet_bone_anim_walk.bvh",
            "objects/anim/pet_bone_anim_grab.bvh"
    };

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

    public static String readFile(GVRContext gvrContext, String filePath) {
        try
        {
            GVRAndroidResource res = new GVRAndroidResource(gvrContext, filePath);
            InputStream stream = res.getStream();
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            String s = new String(bytes);
            return s;
        }
        catch (IOException ex)
        {
            return null;
        }
    }
}
