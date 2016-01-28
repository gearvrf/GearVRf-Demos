
package com.samsung.accessibility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;

final class GVRAccessibilityItem extends GVRAccessibilityInteractiveObject {
    protected boolean isActive = false;
    private boolean isAnimating = false;
    private static final float duration = 0.35f;

    public GVRAccessibilityItem(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
    }

    @Override
    public void interact() {
        float distance = (float) GVRAccessibilityUtils.distance(this, getGVRContext().getMainScene().getMainCameraRig());
        final float[] initialPosition = new float[3];
        initialPosition[0] = getTransform().getPositionX();
        initialPosition[1] = getTransform().getPositionY();
        initialPosition[2] = getTransform().getPositionZ();
        final float[] newPosition = GVRAccessibilityUtils.calculatePointBetweenTwoObjects(this, getGVRContext().getMainScene().getMainCameraRig(),
                distance + 2);

        if (!isAnimating) {
            isAnimating = true;
            if (isActive) {
                new GVRRelativeMotionAnimation(this, duration, newPosition[0] - initialPosition[0], newPosition[1] - initialPosition[1],
                        newPosition[2] - initialPosition[2]).start(getGVRContext().getAnimationEngine())
                        .setInterpolator(GVRAccessibilityInterpolatorBackEaseOut.getInstance()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation animation) {
                                new GVRRotationByAxisAnimation(GVRAccessibilityItem.this, duration * 3, 180, 0, 1, 0)
                                        .start(getGVRContext().getAnimationEngine())
                                        .setInterpolator(GVRAccessibilityInterpolatorStrongEaseInOut.getInstance()).setOnFinish(new GVROnFinish() {

                                            @Override
                                            public void finished(GVRAnimation animation) {
                                                new GVRRelativeMotionAnimation(GVRAccessibilityItem.this, duration, initialPosition[0]
                                                        - newPosition[0],
                                                        initialPosition[1] - newPosition[1], initialPosition[2] - newPosition[2])
                                                        .start(getGVRContext().getAnimationEngine())
                                                        .setInterpolator(GVRAccessibilityInterpolatorBackEaseIn.getInstance())
                                                        .setOnFinish(new GVROnFinish() {

                                                            @Override
                                                            public void finished(GVRAnimation animation) {
                                                                isAnimating = false;

                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
            } else {
                new GVRRelativeMotionAnimation(this, duration, newPosition[0] - initialPosition[0], newPosition[1] - initialPosition[1],
                        newPosition[2] - initialPosition[2]).start(getGVRContext().getAnimationEngine())
                        .setInterpolator(GVRAccessibilityInterpolatorBackEaseOut.getInstance()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation animation) {
                                new GVRRotationByAxisAnimation(GVRAccessibilityItem.this, duration * 3, -180, 0, 1, 0)
                                        .start(getGVRContext().getAnimationEngine())
                                        .setInterpolator(GVRAccessibilityInterpolatorStrongEaseInOut.getInstance()).setOnFinish(new GVROnFinish() {

                                            @Override
                                            public void finished(GVRAnimation animation) {
                                                new GVRRelativeMotionAnimation(GVRAccessibilityItem.this, duration, initialPosition[0]
                                                        - newPosition[0],
                                                        initialPosition[1] - newPosition[1], initialPosition[2] - newPosition[2])
                                                        .start(getGVRContext().getAnimationEngine())
                                                        .setInterpolator(GVRAccessibilityInterpolatorBackEaseIn.getInstance())
                                                        .setOnFinish(new GVROnFinish() {

                                                            @Override
                                                            public void finished(GVRAnimation animation) {
                                                                isAnimating = false;

                                                            }
                                                        });
                                            }
                                        });
                            }
                        });
            }
            isActive = !isActive;
        }
    }

}
