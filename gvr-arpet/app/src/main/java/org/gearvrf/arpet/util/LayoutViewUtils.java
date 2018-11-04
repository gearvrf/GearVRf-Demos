package org.gearvrf.arpet.util;

import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

public class LayoutViewUtils {
    // Base dimensions of our layouts
    public static final float DISPLAY_WIDTH_DP = 640;
    public static final float DISPLAY_HEIGHT_DP = 360;

    public static void setWorldPosition(GVRScene scene, GVRSceneObject view, float x, float y,
                                        float width, float height) {
        final GVRPerspectiveCamera cam = scene.getMainCameraRig().getCenterCamera();
        final float aspect = cam.getAspectRatio();
        final float near = cam.getNearClippingDistance();
        final double fov = Math.toRadians(cam.getFovY());
        final float h = (float)(near * Math.tan(fov * 0.5f));
        final float w = aspect * h;

        final float s = (width * height) / (DISPLAY_WIDTH_DP * DISPLAY_HEIGHT_DP);

        x = (Math.min(DISPLAY_WIDTH_DP, x + (width / 2.0f)) / DISPLAY_WIDTH_DP - 0.5f) * w * 2;
        y = (0.5f - Math.min(DISPLAY_HEIGHT_DP, y + (height / 2.0f)) / DISPLAY_HEIGHT_DP) * h * 2;

        view.getTransform().setPosition(x / near, y / near, -near / near);
        view.getTransform().setScale(s / near, s / near, 1);
    }
}
