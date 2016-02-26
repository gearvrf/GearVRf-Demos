/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.samsung.accessibility.util;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRSceneObject;

public class Utils {

    public static double distance(GVRSceneObject object, GVRCameraRig gvrCameraRig) {
        return Math.sqrt(Math.pow(object.getTransform().getPositionX()
                - gvrCameraRig.getTransform().getPositionX(), 2)
                +
                Math.pow(object.getTransform().getPositionY()
                        - gvrCameraRig.getTransform().getPositionY(), 2)
                +
                Math.pow(object.getTransform().getPositionZ()
                        - gvrCameraRig.getTransform().getPositionZ(), 2));

    }

    public static float[] calculatePointBetweenTwoObjects(GVRSceneObject object,
            GVRCameraRig gvrCameraRig, float desiredDistance) {
        float[] point = new float[3];
        float ratio = desiredDistance / (float) distance(object, gvrCameraRig);
        point[0] = (1 - ratio) * gvrCameraRig.getTransform().getPositionX() + (ratio)
                * object.getTransform().getPositionX();
        point[1] = (1 - ratio) * gvrCameraRig.getTransform().getPositionY() + (ratio)
                * object.getTransform().getPositionY();
        point[2] = (1 - ratio) * gvrCameraRig.getTransform().getPositionZ() + (ratio)
                * object.getTransform().getPositionZ();

        return point;
    }
}
