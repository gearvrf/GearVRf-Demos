
package com.samsung.accessibility;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRSceneObject;

final class GVRAccessibilityUtils {

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
