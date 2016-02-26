package com.samsung.accessibility.scene;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;

import android.util.Log;

import com.samsung.accessibility.R;
import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnClickListener;
import com.samsung.accessibility.focus.OnFocusListener;
import com.samsung.accessibility.interpolator.InterpolatorBackEaseIn;
import com.samsung.accessibility.interpolator.InterpolatorBackEaseOut;
import com.samsung.accessibility.interpolator.InterpolatorStrongEaseInOut;
import com.samsung.accessibility.util.Utils;

public class CursorSceneItem extends FocusableSceneObject {
    protected boolean isActive = false;
    private boolean isAnimating = false;
    private static final float duration = 0.35f;
    private GVRSceneObject selectMaskSceneObject;

    public CursorSceneItem(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
        selectMaskSceneObject = new GVRSceneObject(gvrContext, gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.edge_menu)),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.edge_menu)));
        selectMaskSceneObject.getTransform().setPosition(0, 23f, -.000001f);
        selectMaskSceneObject.getRenderData().setRenderingOrder(getRenderData().getRenderingOrder() + 1);
        selectMaskSceneObject.getTransform().rotateByAxis(180, 0, 1, 0);
        addChildObject(selectMaskSceneObject);

        final GVRSceneObject onFocusSceneObject = new GVRSceneObject(gvrContext, gvrContext.loadMesh(new GVRAndroidResource(gvrContext,
                R.raw.edge_box)), gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.edge_box)));
        onFocusSceneObject.getTransform().setPositionZ(-.1f);
        onFocusSceneObject.getRenderData().setRenderingOrder(getRenderData().getRenderingOrder() + 1);

        setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                removeChildObject(onFocusSceneObject);
            }

            @Override
            public void inFocus(FocusableSceneObject object) {
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                addChildObject(onFocusSceneObject);
            }
        });

        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick() {
                animate();
            }
        });
    }

    public void animate() {
        float distance = (float) Utils.distance(this, getGVRContext().getMainScene().getMainCameraRig());
        final float[] initialPosition = new float[3];
        initialPosition[0] = getTransform().getPositionX();
        initialPosition[1] = getTransform().getPositionY();
        initialPosition[2] = getTransform().getPositionZ();
        final float[] newPosition = Utils.calculatePointBetweenTwoObjects(this, getGVRContext().getMainScene().getMainCameraRig(),
                distance + 2);

        if (!isAnimating) {
            isAnimating = true;
            if (isActive) {
                float[] hit = getEyePointeeHolder().getHit();
                Log.d("AAA", "hit[0]" + hit[0] + ", hit[1]" + hit[1] + ", hit[2]" + hit[2]);

                new GVRRelativeMotionAnimation(this, duration, newPosition[0] - initialPosition[0], newPosition[1] - initialPosition[1],
                        newPosition[2] - initialPosition[2]).start(getGVRContext().getAnimationEngine())
                        .setInterpolator(InterpolatorBackEaseOut.getInstance()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation animation) {
                                new GVRRotationByAxisAnimation(CursorSceneItem.this, duration * 3, 180, 0, 1, 0)
                                        .start(getGVRContext().getAnimationEngine())
                                        .setInterpolator(InterpolatorStrongEaseInOut.getInstance()).setOnFinish(new GVROnFinish() {

                                            @Override
                                            public void finished(GVRAnimation animation) {
                                                new GVRRelativeMotionAnimation(CursorSceneItem.this, duration, initialPosition[0]
                                                        - newPosition[0],
                                                        initialPosition[1] - newPosition[1], initialPosition[2] - newPosition[2])
                                                        .start(getGVRContext().getAnimationEngine())
                                                        .setInterpolator(InterpolatorBackEaseIn.getInstance())
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
                        .setInterpolator(InterpolatorBackEaseOut.getInstance()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation animation) {
                                new GVRRotationByAxisAnimation(CursorSceneItem.this, duration * 3, -180, 0, 1, 0)
                                        .start(getGVRContext().getAnimationEngine())
                                        .setInterpolator(InterpolatorStrongEaseInOut.getInstance()).setOnFinish(new GVROnFinish() {

                                            @Override
                                            public void finished(GVRAnimation animation) {
                                                new GVRRelativeMotionAnimation(CursorSceneItem.this, duration, initialPosition[0]
                                                        - newPosition[0],
                                                        initialPosition[1] - newPosition[1], initialPosition[2] - newPosition[2])
                                                        .start(getGVRContext().getAnimationEngine())
                                                        .setInterpolator(InterpolatorBackEaseIn.getInstance())
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
