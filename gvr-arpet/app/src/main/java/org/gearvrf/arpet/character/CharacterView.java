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
import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
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
import org.gearvrf.arpet.AnchoredObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.arpet.gesture.OnScaleListener;
import org.gearvrf.arpet.gesture.ScalableObject;
import org.gearvrf.arpet.gesture.impl.ScaleGestureDetector;
import org.gearvrf.arpet.mode.ILoadEvents;
import org.gearvrf.arpet.mode.IPetView;
import org.gearvrf.arpet.shaders.GVRTiledMaskShader;
import org.gearvrf.arpet.util.LoadModelHelper;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.IMRCommon;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CharacterView extends AnchoredObject implements
        IPetView,
        ScalableObject {

    private final String TAG = getClass().getSimpleName();

    private final PetContext mPetContext;
    private List<OnScaleListener> mOnScaleListeners = new ArrayList<>();

    private GVRPlane mBoundaryPlane;
    private float[] mPlaneCenterPose = new float[16];
    private GVRSceneObject mShadow;
    private GVRSceneObject mInfinityPlan;
    public final static String PET_COLLIDER = "Pet collider";

    private GVRSceneObject m3DModel;
    private GVRAvatar mPetAvatar;
    private String mBoneMap;
    protected ILoadEvents mLoadListener = null;

    CharacterView(@NonNull PetContext petContext) {
        super(petContext.getGVRContext(), petContext.getMixedReality());

        mPetContext = petContext;
    }

    private void createDragCollider() {
        final boolean showCollider = false;
        GVRSceneObject cube;

        // To debug the  collision
        if (!showCollider) {
            cube = new GVRSceneObject(mPetContext.getGVRContext());
        }  else {
            GVRMaterial material = new GVRMaterial(mPetContext.getGVRContext(),
                    GVRMaterial.GVRShaderType.Color.ID);
            material.setColor(1, 0, 0);
            cube = new GVRCubeSceneObject(mPetContext.getGVRContext(),
                    true, material);
            cube.getRenderData().setDrawMode(GLES30.GL_LINE_LOOP);
        }

        GVRBoxCollider collider = new GVRBoxCollider(mPetContext.getGVRContext());
        collider.setHalfExtents(0.4f, 0.4f, 0.4f);
        cube.attachCollider(collider);

        cube.getTransform().setPosition(0, 0.2f, 0);
        cube.getTransform().setScale(0.2f, 0.5f, 0.5f);

        cube.setName(PET_COLLIDER);
        addChildObject(cube);
    }

    public GVRAnimator getAnimation(int i) {
        if (mPetAvatar != null && mPetAvatar.getAnimationCount() > i) {
            return mPetAvatar.getAnimation(i);
        }

        return null;
    }

    private void createShadow() {
        final GVRContext gvrContext = mPetContext.getGVRContext();
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
        final GVRContext gvrContext = mPetContext.getGVRContext();
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

    @Override
    public boolean updatePose(float[] poseMatrix) {
        // Update y position to plane's y pos
        mPlaneCenterPose = mBoundaryPlane.getTransform().getModelMatrix();
        poseMatrix[13] = mPlaneCenterPose[13];

        return super.updatePose(poseMatrix);
    }

    public void setBoundaryPlane(GVRPlane boundary) {
        mPlaneCenterPose = boundary.getTransform().getModelMatrix();
        mBoundaryPlane = boundary;
    }

    public GVRPlane getBoundaryPlane() {
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
        m3DModel.getTransform().setPositionY(0.2f);
    }

    public void stopDragging() {
        mShadow.setEnable(false);
        m3DModel.getTransform().setPositionY(0.0f);
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
        mainScene.addSceneObject(getAnchor());
        //getAnchor().attachSceneObject(mInfinityPlan);
    }

    @Override
    public void hide(GVRScene mainScene) {
        mainScene.removeSceneObject(getAnchor());
        //getAnchor().detachSceneObject(mInfinityPlan);
    }

    @Override
    public void load(ILoadEvents listener) {
        final GVRContext gvrContext = mPetContext.getGVRContext();
        mLoadListener = listener;

        createShadow();

        // createInfinityPlan();

        createDragCollider();

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
        final float MIN_DISTANCE = 100f;
        Vector3f vectorDistance = new Vector3f();
        float[] modelCam = mPetContext.getMainScene().getMainCameraRig().getTransform().getModelMatrix();
        float[] modelCharacter = getAnchor().getTransform().getModelMatrix();

        vectorDistance.set(modelCam[12], modelCam[13], modelCam[14]);
        // Calculates the distance in centimeters
        float distance = vectorDistance.distance(modelCharacter[12], modelCharacter[13], modelCharacter[14]);

        if (distance < MIN_DISTANCE) {
            scale(ScaleGestureDetector.MIN_FACTOR);
        }
    }

    public GVRSceneObject getGrabPivot() {
         int i = mPetAvatar.getSkeleton().getBoneIndex(LoadModelHelper.PET_GRAB_PIVOT);
         if (!(i < 0) && i < mPetAvatar.getSkeleton().getNumBones()) {
             return mPetAvatar.getSkeleton().getBone(i);
         }

         return null;
    }

    private void loadAnimations() {
        final GVRContext gvrContext = mPetContext.getGVRContext();
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
        public void onAvatarLoaded(GVRSceneObject gvrSceneObject, String s, String s1) {
            final GVRContext gvrContext = mPetContext.getGVRContext();
            Log.d(TAG, "onAvatarLoaded %s => %s", s, s1);

            if (gvrSceneObject.getParent() == null)
            {
                gvrContext.runOnGlThread(new Runnable()
                {
                    public void run()
                    {
                        m3DModel = gvrSceneObject;

                        m3DModel.getTransform().setScale(0.003f, 0.003f, 0.003f);
                        m3DModel.getTransform().setPosition(0, 0.4f, 0);
                        CharacterView.this.addChildObject(m3DModel);
                    }
                });
            }

            loadAnimations();
        }

        @Override
        public void onModelLoaded(GVRSceneObject gvrSceneObject, String s, String s1) {
            Log.d(TAG, "onModelLoaded %s => %s", s, s1);
        }

        @Override
        public void onAnimationLoaded(GVRAnimator animation, String s, String s1) {
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
        public void onAnimationStarted(GVRAnimator gvrAnimator) {
            Log.d(TAG, "onAnimationStarted");
        }

        @Override
        public void onAnimationFinished(GVRAnimator gvrAnimator, GVRAnimation gvrAnimation) {
            Log.d(TAG, "onAnimationFinished");
        }
    };
}
