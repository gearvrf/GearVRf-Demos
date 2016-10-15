package org.gearvrf.gvrsimlephysics.util;

/**
 * Created by d.alipio@samsung.com on 11/4/16.
 */

public class MathUtils {

    public static int calculateForce(float velocityX) {
        int maxDuration = 2;
        int velocity = Math.round(Math.abs(velocityX) * maxDuration / 1000f);
        return velocity * 100;
    }

    public static float[] calculateRotation(float rotationPitch, float rotationYaw) {
        return new float[]{
                (float) -Math.sin(rotationYaw * Math.PI / 180),
                (float) Math.sin(rotationPitch * Math.PI / 180),
                (float) -(Math.cos(rotationPitch * Math.PI / 180) * Math.cos(rotationYaw * Math.PI / 180))
        };
    }
}
