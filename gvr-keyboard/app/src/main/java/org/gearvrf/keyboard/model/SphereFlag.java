/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.keyboard.model;

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.interpolator.FloatEffectInterpolator;
import org.gearvrf.keyboard.interpolator.InterpolatorExpoEaseInOut;
import org.gearvrf.keyboard.interpolator.InterpolatorExpoEaseOut;
import org.gearvrf.keyboard.shader.GVRShaderAnimation;
import org.gearvrf.keyboard.shader.SphereShader;
import org.gearvrf.keyboard.util.Constants;
import org.gearvrf.keyboard.util.SceneObjectNames;
import org.gearvrf.keyboard.util.Util;
import org.gearvrf.utility.Log;

public class SphereFlag extends GVRSceneObject {

    private final float CURSOR_POSITION_OFFSET_Y = 2f;
    private String mCountryName;
    private int mTexture;
    private int mResultTexture;
    private String mQuestion;
    private String mAnswer;
    private Vector3D positionVector;
    public boolean isSpottingSphere = false;
    public boolean isUnsnappingSphere = false;
    public boolean isUnspottingSphere = false;
    public boolean isFloatingSphere = false;
    public int answerState = SphereStaticList.MOVEABLE;
    public GVRAnimation snapAnimation;
    private GVRAnimation spotAnimation;
    private GVRAnimation scaleParentAnimation;
    private GVRAnimation scaleThisAnimation;
    private GVRAnimation floatingAnimation;
    private GVRAnimation followCursorAnimation;

    private boolean moveTogetherDashboard = false;
    private GVRContext gvrContext;

    public SphereFlag(GVRContext gvrContext, TypedArray sphere) {
        super(gvrContext);
        setName(SceneObjectNames.SPHERE_FLAG);

        this.gvrContext = gvrContext;

        initSphere(sphere);

        GVRMaterial material = getMaterial();
        
        GVRRenderData renderData = getRenderData(material);
        renderData.setShaderTemplate(SphereShader.class);
        attachRenderData(renderData);
        
        updateMaterial();
    }

    private void initSphere(TypedArray sphere) {
        Resources res = gvrContext.getContext().getResources();

        mCountryName = res.getString(sphere.getResourceId(0, -1));
        mTexture = sphere.getResourceId(1, -1);
        mQuestion = res.getString(sphere.getResourceId(2, -1));
        mAnswer = res.getString(sphere.getResourceId(3, -1));
        mResultTexture = R.drawable.check;

        float posX = Util.applyRatioAt(sphere.getFloat(4, -1));
        float posY = Util.applyRatioAt(sphere.getFloat(5, -1));
        float posZ = Util.applyRatioAt(sphere.getFloat(6, -1));
        positionVector = new Vector3D(posX, posY, posZ);
    }

    public void updateMaterial() {

        float[] mat = this.getTransform().getModelMatrix();

        float[] light = new float[4];
        light[0] = 0;
        light[1] = 6;
        light[2] = 6;
        light[3] = 1.0f;

        float lX = mat[0] * light[0] + mat[1] * light[1] + mat[2] * light[2] + mat[3] * light[3];
        float lY = mat[4] * light[0] + mat[5] * light[1] + mat[6] * light[2] + mat[7] * light[3];
        float lZ = mat[8] * light[0] + mat[9] * light[1] + mat[10] * light[2] + mat[11] * light[3];

        float x = 0;
        float y = 0;
        float z = 0;

       
        this.getRenderData().getMaterial().setVec3(SphereShader.LIGHT_KEY,
                lX - this.getTransform().getPositionX(),
                lY - this.getTransform().getPositionY(),
                lZ - this.getTransform().getPositionZ());
        this.getRenderData().getMaterial().setVec3(SphereShader.EYE_KEY, x, y, z);

    }

    private GVRMaterial getMaterial() {
        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.BeingGenerated.ID);
        material.setTexture(SphereShader.TEXTURE_KEY,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, mTexture)));
        material.setFloat("blur", 0);
        material.setFloat(SphereShader.ANIM_TEXTURE, 0.0f);
        material.setTexture(SphereShader.SECUNDARY_TEXTURE_KEY,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, mResultTexture)));
        material.setVec3(SphereShader.TRANSITION_COLOR, 1, 1, 1);
        material.setVec3(SphereShader.EYE_KEY, 0, 0, 0);

        // Light config
        GVRTexture hdriTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                R.drawable.hdri_reflex));
        material.setTexture(SphereShader.HDRI_TEXTURE_KEY, hdriTexture);

        return material;
    }

    private GVRRenderData getRenderData(GVRMaterial material) {
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMesh(gvrContext.getAssetLoader().loadFutureMesh(new GVRAndroidResource(gvrContext,
                R.raw.sphere_uv_flag)));
        renderData.setMaterial(material);
        renderData.setRenderingOrder(100);
        renderData.setAlphaBlend(true);
        return renderData;
    }

    public void animateFloating() {
        if (!isFloatingSphere) {
            isFloatingSphere = true;
            float intensity = 1;
            float randomValue = 0.7f + ((float) Math.random() * 1.0f);

            floatingAnimation = new GVRRelativeMotionAnimation
                    (getParent(), intensity * 3 * randomValue, 0, getParent().getTransform()
                            .getPositionY() - intensity * 2 * randomValue, 0);

            floatingAnimation.setInterpolator(new FloatEffectInterpolator());
            floatingAnimation.setRepeatMode(GVRRepeatMode.PINGPONG);
            floatingAnimation.setRepeatCount(-1);
            floatingAnimation.start(gvrContext.getAnimationEngine());
        }
    }

    public void stopFloatingSphere() {
        gvrContext.getAnimationEngine().stop(floatingAnimation);
        isFloatingSphere = false;
    }

    public void spotSphere() {
        if (!isSpottingSphere) {
            isSpottingSphere = true;

            stopAnimationsToSpot();

            spotAnimation = createSpotAnimation();
            spotAnimation.start(gvrContext.getAnimationEngine()).setOnFinish(new GVROnFinish() {
                @Override
                public void finished(GVRAnimation arg0) {
                    isSpottingSphere = false;
                }
            });
        }
    }

    private void stopAnimationsToSpot() {
        if (spotAnimation != null) {
            gvrContext.getAnimationEngine().stop(spotAnimation);
            gvrContext.getAnimationEngine().stop(scaleParentAnimation);
            gvrContext.getAnimationEngine().stop(scaleThisAnimation);
            isUnspottingSphere = false;
        }
    }

    private GVRAnimation createSpotAnimation() {
        GVRCameraRig cameraObject = gvrContext.getMainScene().getMainCameraRig();
        float distance = (float) Math.max(
                0.7 * Util.distance(getInitialPositionVector(), cameraObject.getTransform()),
                Constants.MINIMUM_DISTANCE_FROM_CAMERA);
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                getParent(),
                distance);
        float scaleFactor = Util.getHitAreaScaleFactor(distance);

        scaleParentAnimation = new GVRScaleAnimation(getParent(), 1.2f, scaleFactor)
                .start(gvrContext
                        .getAnimationEngine());
        scaleThisAnimation = new GVRScaleAnimation(this, 1.2f, 1 / scaleFactor).start(gvrContext
                .getAnimationEngine());

        return new GVRRelativeMotionAnimation(getParent(), 1.2f, newPosition[0]
                - getParent().getTransform().getPositionX(),
                newPosition[1] - getParent().getTransform().getPositionY(),
                newPosition[2] - getParent().getTransform().getPositionZ())
                .setInterpolator(new InterpolatorExpoEaseOut());
    }

    public void unspotSphere() {
        if (!isUnspottingSphere) {
            isUnspottingSphere = true;
            GVRCameraRig cameraObject = gvrContext.getMainScene().getMainCameraRig();
            float scaleFactor = Util.getHitAreaScaleFactor((float) Util.distance(
                    getInitialPositionVector(), cameraObject.getTransform()));

            stopAnimationsToUnspot();

            spotAnimation = createUnspotAnimation(scaleFactor);
            spotAnimation.start(gvrContext.getAnimationEngine()).setOnFinish(new GVROnFinish() {
                @Override
                public void finished(GVRAnimation arg0) {
                    isUnspottingSphere = false;
                }
            });
        }
    }

    private GVRAnimation createUnspotAnimation(float scaleFactor) {
        scaleParentAnimation = new GVRScaleAnimation(getParent(), 1.2f, scaleFactor)
                .start(gvrContext
                        .getAnimationEngine());
        scaleThisAnimation = new GVRScaleAnimation(this, 1.2f, 1 / scaleFactor).start(gvrContext
                .getAnimationEngine());

        return new GVRRelativeMotionAnimation(getParent(), 1.2f, (float) getInitialPositionVector()
                .getX() - getParent().getTransform().getPositionX(),
                (float) getInitialPositionVector().getY()
                        - getParent().getTransform().getPositionY(),
                (float) getInitialPositionVector().getZ()
                        - getParent().getTransform().getPositionZ())
                .setInterpolator(new InterpolatorExpoEaseOut());
    }

    private void stopAnimationsToUnspot() {
        if (spotAnimation != null) {
            gvrContext.getAnimationEngine().stop(spotAnimation);
            gvrContext.getAnimationEngine().stop(scaleParentAnimation);
            gvrContext.getAnimationEngine().stop(scaleThisAnimation);
            isSpottingSphere = false;
        }
    }

    public void snapSphere(float[] hit) {
        if (isUnsnappingSphere) {
            gvrContext.getAnimationEngine().stop(snapAnimation);
            isUnsnappingSphere = false;
        }

        if(hit != null)
        snapAnimation = new GVRRelativeMotionAnimation(this, 1.2f, hit[0]
                - getTransform().getPositionX(),
                hit[1] - getTransform().getPositionY(), 0f).start(gvrContext.getAnimationEngine());

    }

    public void unsnapSphere(float duration) {
        if (!isUnsnappingSphere) {
            if (snapAnimation != null) {
                gvrContext.getAnimationEngine().stop(snapAnimation);
            }
            isUnsnappingSphere = true;
            snapAnimation = new GVRRelativeMotionAnimation(this, duration, -getTransform()
                    .getPositionX(),
                    -getTransform().getPositionY(), 0f).start(gvrContext.getAnimationEngine())
                    .setInterpolator(new InterpolatorExpoEaseInOut())
                    .setOnFinish(new GVROnFinish() {
                        @Override
                        public void finished(GVRAnimation arg0) {
                            isUnsnappingSphere = false;
                            if (answerState == SphereStaticList.MOVEABLE) {
                                animateFloating();
                            }
                        }
                    });
        }
    }

    public void giveAnswer(String answer) {
        moveTogetherDashboard = false;
        checkAnswer(answer);
        changeTexture();
        resetSphereAfterTime(3f);
    }

    private void checkAnswer(final String answer) {
        getGVRContext().runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (getAnswer().equalsIgnoreCase(answer)) {

                    AudioClip.getInstance(getGVRContext().getContext()).playSound(
                            AudioClip.getSucessSoundID(), 1.0f, 1.0f);
                    getRenderData().getMaterial().setVec3(SphereShader.TRANSITION_COLOR, 0.2f,
                            0.675f, 0.443f);
                    getRenderData().getMaterial().setTexture(
                            SphereShader.SECUNDARY_TEXTURE_KEY,
                            getGVRContext().getAssetLoader().loadTexture(
                                    new GVRAndroidResource(getGVRContext(), R.drawable.check)));
                } else {

                    AudioClip.getInstance(getGVRContext().getContext()).playSound(
                            AudioClip.getWrongSoundID(), 1.0f, 1.0f);
                    getRenderData().getMaterial().setVec3(SphereShader.TRANSITION_COLOR, 1, 0, 0);
                    getRenderData().getMaterial().setTexture(
                            SphereShader.SECUNDARY_TEXTURE_KEY,
                            getGVRContext().getAssetLoader().loadTexture(
                                    new GVRAndroidResource(getGVRContext(), R.drawable.error)));
                }
            }
        });
    }

    public void moveToCursor() {
        if (followCursorAnimation != null) {
            getGVRContext().getAnimationEngine().stop(followCursorAnimation);
        }
        GVRCameraRig cameraObject = getGVRContext().getMainScene().getMainCameraRig();

        float desiredDistance = (float) Math.max(
                0.7 * Util.distance(getParent(), cameraObject.getTransform()),
                Constants.MINIMUM_DISTANCE_FROM_CAMERA);
        float[] lookAt = getGVRContext().getMainScene().getMainCameraRig().getLookAt();
        Vector3D lookAtVector = new Vector3D(lookAt[0], lookAt[1], lookAt[2]);

        final float desiredX = (float) lookAtVector.getX() * desiredDistance;
        final float desiredY = (float) lookAtVector.getY() * desiredDistance
                + CURSOR_POSITION_OFFSET_Y;
        final float desiredZ = (float) lookAtVector.getZ() * desiredDistance;

        float x = desiredX - getParent().getTransform().getPositionX();
        float y = desiredY - getParent().getTransform().getPositionY();
        float z = desiredZ - getParent().getTransform().getPositionZ();

        followCursorAnimation = new GVRRelativeMotionAnimation(getParent(), 0.8f, x, y, z)
                .setInterpolator(new InterpolatorExpoEaseOut()).start(
                        getGVRContext().getAnimationEngine());
    }

    private void changeTexture() {
        new GVRShaderAnimation(this, SphereShader.ANIM_TEXTURE, 0.6f, 1).setInterpolator(
                new InterpolatorExpoEaseOut()).start(getGVRContext().getAnimationEngine());
    }

    private void resetSphereAfterTime(float delay) {
        getGVRContext().getPeriodicEngine().runAfter(new Runnable() {
            @Override
            public void run() {
                answerState = SphereStaticList.RESTORING;
                restoreSpherePosition(1.2f);
                restoreTexture();
            }
        }, delay);
    }

    public void restoreSpherePosition(float duration) {
        if (followCursorAnimation != null) {
            getGVRContext().getAnimationEngine().stop(followCursorAnimation);
        }

        float x = (float) getInitialPositionVector().getX()
                - getParent().getTransform().getPositionX();
        float y = (float) getInitialPositionVector().getY()
                - getParent().getTransform().getPositionY();
        float z = (float) getInitialPositionVector().getZ()
                - getParent().getTransform().getPositionZ();

        followCursorAnimation = new GVRRelativeMotionAnimation(getParent(), duration, x, y, z)
                .setInterpolator(new InterpolatorExpoEaseInOut())
                .start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {
                    @Override
                    public void finished(GVRAnimation arg0) {
                        answerState = SphereStaticList.MOVEABLE;
                        animateFloating();
                    }
                });
    }

    public void restoreTexture() {
        new GVRShaderAnimation(this, SphereShader.ANIM_TEXTURE, 0.8f, 0).setInterpolator(
                new InterpolatorExpoEaseInOut()).start(getGVRContext().getAnimationEngine());
    }

    public void tapSphere() {
        final SphereFlag sphereFlag = this;

        if (spotAnimation != null) {
            getGVRContext().getAnimationEngine().stop(spotAnimation);
            getGVRContext().getAnimationEngine().stop(scaleParentAnimation);
            getGVRContext().getAnimationEngine().stop(scaleThisAnimation);
        }
        getGVRContext().getPeriodicEngine().runAfter(new Runnable() {

            @Override
            public void run() {
                float duration = 0.71f;
                unsnapSphere(duration);
                GVRCameraRig cameraObject = getGVRContext().getMainScene().getMainCameraRig()
                ;
                float distance = Constants.SPHERE_SELECTION_DISTANCE;
                float[] newPosition = Util.calculatePointBetweenTwoObjects(
                        cameraObject.getTransform(),
                        getInitialPositionVector(), distance);
                float scaleFactor = Util.getHitAreaScaleFactor(distance);

                scaleParentAnimation = new GVRScaleAnimation(getParent(), duration, scaleFactor).start(getGVRContext()
                        .getAnimationEngine());
                scaleThisAnimation = new GVRScaleAnimation(sphereFlag, duration, 1 / scaleFactor).start(getGVRContext()
                        .getAnimationEngine());

                new GVRRelativeMotionAnimation(getParent(), duration, newPosition[0]
                        - getParent().getTransform().getPositionX(),
                        newPosition[1] - getParent().getTransform().getPositionY(),
                        newPosition[2] - getParent().getTransform().getPositionZ())
                        .setInterpolator(new InterpolatorExpoEaseInOut())
                        .start(getGVRContext().getAnimationEngine()).setOnFinish(new GVROnFinish() {

                            @Override
                            public void finished(GVRAnimation arg0) {
                                SphereFlag.this.moveTogetherDashboard = true;
                            }
                        });
            }
        }, 0.5f);
    }

    public void unselectSphere() {
        answerState = SphereStaticList.RESTORING;
        stopFloatingSphere();
        final SphereFlag sphereFlag = this;
        float duration = 1.5f;
        new GVRShaderAnimation(sphereFlag, SphereShader.BLUR_INTENSITY, duration, 1)
                .start(getGVRContext().getAnimationEngine());

        GVRCameraRig cameraObject = getGVRContext().getMainScene().getMainCameraRig();
        float distance = (float) (Constants.NEAREST_NON_SELECTED_SPHERE - Constants.NEAREST_SPHERE + Util
                .distance(getInitialPositionVector(),
                        cameraObject.getTransform()));
        float[] newPosition = Util.calculatePointBetweenTwoObjects(cameraObject.getTransform(),
                getParent(),
                distance);
        float scaleFactor = Util.getHitAreaScaleFactor(distance);

        scaleParentAnimation = new GVRScaleAnimation(getParent(), duration, scaleFactor)
                .start(getGVRContext()
                        .getAnimationEngine());
        scaleThisAnimation = new GVRScaleAnimation(sphereFlag, duration, 1 / scaleFactor)
                .start(getGVRContext()
                        .getAnimationEngine());

        new GVRRelativeMotionAnimation(getParent(), duration, newPosition[0]
                - getParent().getTransform().getPositionX(),
                newPosition[1] - getParent().getTransform().getPositionY(),
                newPosition[2] - getParent().getTransform().getPositionZ())
                .setInterpolator(new InterpolatorExpoEaseInOut()).start(
                        getGVRContext().getAnimationEngine());
    }

    public String getQuestion() {
        return mQuestion;
    }

    public String getAnswer() {
        return mAnswer;
    }

    public Vector3D getInitialPositionVector() {
        return positionVector;
    }

    public boolean canMoveTogetherDashboard() {
        return moveTogetherDashboard;
    }

}
