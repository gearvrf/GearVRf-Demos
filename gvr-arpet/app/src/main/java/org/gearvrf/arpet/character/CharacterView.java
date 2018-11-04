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

package org.gearvrf.arpet.character;

import android.opengl.GLES30;
import android.support.annotation.NonNull;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRAvatar;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.gesture.OnScaleListener;
import org.gearvrf.arpet.gesture.ScalableObject;
import org.gearvrf.arpet.gesture.impl.ScaleGestureDetector;
import org.gearvrf.arpet.mode.ILoadEvents;
import org.gearvrf.arpet.mode.IPetView;
import org.gearvrf.arpet.service.share.SharedMixedReality;
import org.gearvrf.arpet.shaders.GVRTiledMaskShader;
import org.gearvrf.arpet.util.LoadModelHelper;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.IMixedReality;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterView extends GVRSceneObject implements
        IPetView,
        ScalableObject {

    private final String TAG = getClass().getSimpleName();

    private List<OnScaleListener> mOnScaleListeners = new ArrayList<>();

    private GVRSceneObject mBoundaryPlane = null;
    private float[] mPlaneCenterPose = new float[16];
    private GVRSceneObject mShadow;
    private GVRSceneObject mInfinityPlan;
    public final static String PET_COLLIDER = "corpo_GEO";  // From 3D model
    private final PetContext mPetContext;
    private GVRSceneObject m3DModel;
    private GVRAvatar mPetAvatar;
    private String mBoneMap;
    protected ILoadEvents mLoadListener = null;
    private GVRSceneObject mTapObject;

    CharacterView(@NonNull PetContext petContext) {
        super(petContext.getGVRContext());

        mPetContext = petContext;
        mTapObject = new GVRSceneObject(mPetContext.getGVRContext());
    }

    public GVRSceneObject getTapObject() {
         return mTapObject;
    }

    public void setTapPosition(float x, float y, float z) {
        mTapObject.getTransform().setPosition(x, y, z);
    }

    public GVRAnimator getAnimation(int i) {
        if (mPetAvatar != null && mPetAvatar.getAnimationCount() > i) {
            return mPetAvatar.getAnimation(i);
        }

        return null;
    }

    private void createShadow() {
        final GVRContext gvrContext = getGVRContext();
        GVRTexture tex = gvrContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.drag_shadow));
        GVRMaterial mat = new GVRMaterial(gvrContext);
        mat.setMainTexture(tex);
        mShadow = new GVRSceneObject(gvrContext, 0.3f, 0.6f);
        mShadow.getRenderData().setMaterial(mat);
        mShadow.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);
        mShadow.getTransform().setPosition(0f, 0.02f, -0.02f);
        mShadow.getRenderData().setAlphaBlend(true);
        mShadow.setName("shadow");
        mShadow.setEnable(false);
        addChildObject(mShadow);
    }

    private void createInfinityPlan() {
        final GVRContext gvrContext = getGVRContext();
        final float width = 2.0f;
        final float height = 2.0f;

        final float[] vertices = new float[]{
                0.0f, 0.0f, 0.0f,
                width * -0.25f, height * 0.5f, 0.0F,
                width * -0.5f, height * 0.25f, 0.0F,
                width * -0.5f, height * -0.25f, 0.0f,
                width * -0.25f, height * -0.5f, 0.0f,
                width * 0.25f, height * -0.5f, 0.0f,
                width * 0.5f, height * -0.25f, 0.0f,
                width * 0.5f, height * 0.25f, 0.0f,
                width * 0.25f, height * 0.5f, 0.0f,
                width * -0.25f, height * 0.5f, 0.0F
        };

        mInfinityPlan = new GVRSceneObject(gvrContext);
        final GVRTextureParameters texParams = new GVRTextureParameters(gvrContext);
        final GVRTexture tex = gvrContext.getAssetLoader().loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.infinity_plan));
        final GVRMaterial material = new GVRMaterial(gvrContext, new GVRShaderId(GVRTiledMaskShader.class));
        final GVRRenderData renderData = new GVRRenderData(gvrContext);
        final GVRMesh mesh = new GVRMesh(gvrContext, "float3 a_position");

        texParams.setWrapSType(GVRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT);
        texParams.setWrapTType(GVRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT);
        tex.updateTextureParameters(texParams);

        mesh.setVertices(vertices);
        renderData.setMesh(mesh);

        renderData.setAlphaBlend(true);
        material.setMainTexture(tex);
        renderData.setMaterial(material);
        renderData.setDrawMode(GLES30.GL_TRIANGLE_FAN);

        mInfinityPlan.attachComponent(renderData);
        mInfinityPlan.getTransform().setPosition(0f, 0.01f, -0.02f);
        mInfinityPlan.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);
        mInfinityPlan.setName("infinityPlan");
    }

    public boolean updatePose(float[] poseMatrix) {
        if (mPetContext.getMode() != SharedMixedReality.GUEST) {
            float[] planeModel = mBoundaryPlane.getTransform().getModelMatrix();
            Vector3f centerPlane = new Vector3f(planeModel[12], planeModel[13], planeModel[14]);
            poseMatrix[13] = planeModel[13];

            final boolean infinityPlane = false;
            if (!infinityPlane) {
                GVRPicker.GVRPickedObject pickedObject = GVRPicker.pickSceneObject(mBoundaryPlane,
                        0, 0, 0, poseMatrix[12], poseMatrix[13], poseMatrix[14]);
                float[] petModel = getTransform().getModelMatrix();
                if (pickedObject == null && centerPlane.distance(petModel[12], petModel[13], petModel[14])
                        < centerPlane.distance(poseMatrix[12], poseMatrix[13], poseMatrix[14])) {
                    return false;
                }
            }
        }

        getTransform().setModelMatrix(poseMatrix);

        return true;
    }

    public void setBoundaryPlane(GVRSceneObject boundary) {
        if (mBoundaryPlane != null) {
            mBoundaryPlane.removeChildObject(mTapObject);
            mPetContext.unregisterSharedObject(mBoundaryPlane);
        }

        boundary.addChildObject(mTapObject);
        mPetContext.registerSharedObject(boundary, ArPetObjectType.PLANE);

        mPlaneCenterPose = boundary.getTransform().getModelMatrix();
        mBoundaryPlane = boundary;
    }

    public GVRSceneObject getBoundaryPlane() {
         return mBoundaryPlane;
    }

    @Override
    public void scale(float factor) {
        getTransform().setScale(factor, factor, factor);
        notifyScale(factor);
    }

    public void rotate(float angle) {
        getTransform().rotateByAxis(angle, 0, 1, 0);
    }

    public void startDragging() {
        mShadow.setEnable(true);
        m3DModel.getTransform().setPositionY(0.4f);
    }

    public void stopDragging() {
        mShadow.setEnable(false);
        m3DModel.getTransform().setPositionY(0.2f);
    }

    public boolean isDragging() {
        return mShadow.isEnabled();
    }

    private synchronized void notifyScale(float factor) {
        for (OnScaleListener listener : mOnScaleListeners) {
            listener.onScale(factor);
        }
    }

    @Override
    public void addOnScaleListener(OnScaleListener listener) {
        if (!mOnScaleListeners.contains(listener)) {
            mOnScaleListeners.add(listener);
        }
    }

    @Override
    public void show(GVRScene mainScene) {
        mainScene.addSceneObject(this);
        //getAnchor().attachSceneObject(mInfinityPlan);
    }

    @Override
    public void hide(GVRScene mainScene) {
        mainScene.removeSceneObject(this);
        //getAnchor().detachSceneObject(mInfinityPlan);
    }

    @Override
    public void load(ILoadEvents listener) {
        final GVRContext gvrContext = getGVRContext();
        mLoadListener = listener;

        createShadow();

        // createInfinityPlan();

        mBoneMap = LoadModelHelper.readFile(gvrContext, LoadModelHelper.PET_BONES_MAP_PATH);
        mPetAvatar = new GVRAvatar(gvrContext, "PetModel");
        mPetAvatar.getEventReceiver().addListener(mAvatarListener);
        try
        {
            mPetAvatar.loadModel(new GVRAndroidResource(gvrContext, LoadModelHelper.PET_MODEL_PATH));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            if (mLoadListener != null) {
                mLoadListener.onFailure();
            }
        }
    }

    @Override
    public void unload() {

    }

    /**
     * Sets the initial scale according to the distance between the pet and camera
     */
    public void setInitialScale() {
        Vector3f vectorDistance = new Vector3f();
        float[] modelCam = getGVRContext().getMainScene().getMainCameraRig().getTransform().getModelMatrix();
        float[] modelCharacter = getTransform().getModelMatrix();

        vectorDistance.set(modelCam[12], modelCam[13], modelCam[14]);
        // Calculates the distance in centimeters
        float factor = 0.5f * vectorDistance.distance(modelCharacter[12], modelCharacter[13], modelCharacter[14]);
        float scale = Math.max(ScaleGestureDetector.MIN_FACTOR, Math.min(factor, ScaleGestureDetector.MAX_FACTOR));

        scale(scale);
    }

    public GVRSceneObject getGrabPivot() {
         int i = mPetAvatar.getSkeleton().getBoneIndex(LoadModelHelper.PET_GRAB_PIVOT);
         if (!(i < 0) && i < mPetAvatar.getSkeleton().getNumBones()) {
             return mPetAvatar.getSkeleton().getBone(i);
         }

         return null;
    }

    private void loadAnimations() {
        final GVRContext gvrContext = getGVRContext();
        int i = 0;
        try
        {
            for (i = 0; i < LoadModelHelper.PET_ANIMATIONS_PATH.length; i++) {
                GVRAndroidResource res = new GVRAndroidResource(gvrContext,
                        LoadModelHelper.PET_ANIMATIONS_PATH[i]);
                mPetAvatar.loadAnimation(res, mBoneMap);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, "Animation could not be loaded from "
                    + LoadModelHelper.PET_ANIMATIONS_PATH[i]);

            if (mLoadListener != null) {
                mLoadListener.onFailure();
            }
        }
    }

    private GVRAvatar.IAvatarEvents mAvatarListener = new GVRAvatar.IAvatarEvents() {
        int contAnim = 0;
        @Override
        public void onAvatarLoaded(GVRAvatar avatar, GVRSceneObject gvrSceneObject, String s, String s1) {
            final GVRContext gvrContext = getGVRContext();
            Log.d(TAG, "onAvatarLoaded %s => %s", s, s1);

            if (gvrSceneObject.getParent() == null)
            {
                gvrContext.runOnGlThread(new Runnable()
                {
                    public void run()
                    {
                        m3DModel = gvrSceneObject;

                        m3DModel.getTransform().setScale(0.003f, 0.003f, 0.003f);
                        m3DModel.getTransform().setPosition(0, 0.2f, 0);
                        // Get the pet's body from 3D model
                        GVRSceneObject body = m3DModel.getSceneObjectByName(PET_COLLIDER);
                        if (body != null) {
                            // Create a mesh collider and attach it to the body
                            body.attachCollider(new GVRMeshCollider(mPetContext.getGVRContext(), true));
                        }
                        CharacterView.this.addChildObject(m3DModel);
                    }
                });
            }

            loadAnimations();
        }

        @Override
        public void onModelLoaded(GVRAvatar avatar, GVRSceneObject gvrSceneObject, String s, String s1) {
            Log.d(TAG, "onModelLoaded %s => %s", s, s1);
        }

        @Override
        public void onAnimationLoaded(GVRAvatar avatar, GVRAnimator animation, String s, String s1) {
            Log.d(TAG, "onAnimationLoaded %s => %s", s, s1);
            contAnim++;

            animation.setRepeatMode(GVRRepeatMode.REPEATED);
            animation.setSpeed(1f);
            /*
            if (!mPetAvatar.isRunning())
            {
                mPetAvatar.startAll(GVRRepeatMode.REPEATED);

            }*/
            //mPetAvatar.start(animation.getName());

            if (contAnim == LoadModelHelper.PET_ANIMATIONS_PATH.length) {
                if (mLoadListener != null) {
                    mLoadListener.onSuccess();
                }
            }
        }

        @Override
        public void onAnimationStarted(GVRAvatar avatar, GVRAnimator gvrAnimator) {
            Log.d(TAG, "onAnimationStarted");
        }

        @Override
        public void onAnimationFinished(GVRAvatar avatar, GVRAnimator gvrAnimator, GVRAnimation gvrAnimation) {
            Log.d(TAG, "onAnimationFinished");
        }
    };
}
