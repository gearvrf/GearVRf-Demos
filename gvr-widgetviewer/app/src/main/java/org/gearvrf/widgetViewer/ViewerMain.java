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

package org.gearvrf.widgetViewer;

import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;
import org.gearvrf.widgetplugin.GVRWidgetPlugin;
import org.gearvrf.widgetplugin.GVRWidgetSceneObject;
import org.gearvrf.widgetplugin.GVRWidgetSceneObjectMeshInfo;

import java.io.IOException;

public class ViewerMain extends GVRMain {
    private static final int THUMBNAIL_RENDER_ORDER = GVRRenderData.GVRRenderingOrder.TRANSPARENT + 50;
    private static final String TAG = "ViewerMain";

    private GVRWidgetPlugin mPlugin = null;
    private GVRContext mGVRContext = null;
    private GVRScene mScene = null;

    private GVRMaterial mMetalMaterial = null;
    private GVRMaterial mGlassMaterial = null;
    private GVRMaterial mDiffuseMaterial = null;
    private GVRMaterial mReflectionMaterial = null;
    private GVRMaterial mPhongMaterial = null;

    private GVRMaterial mCarBodyMaterial = null;
    private GVRMaterial mCarGlassMaterial = null;
    private GVRMaterial mCarTireMaterial = null;
    private GVRMaterial mCarWheelMaterial = null;
    private GVRMaterial mCarGrillMaterial = null;
    private GVRMaterial mCarBackMaterial = null;
    private GVRMaterial mCarLightMaterial = null;
    private GVRMaterial mCarInsideMaterial = null;

    private GVRMaterial mRobotBodyMaterial = null;
    private GVRMaterial mRobotHeadMaterial = null;
    private GVRMaterial mRobotMetalMaterial = null;
    private GVRMaterial mRobotRubberMaterial = null;

    private GVRMaterial mLeafBodyMaterial = null;
    private GVRMaterial mLeafBoxMaterial = null;
    private final float EYE_TO_OBJECT = 2.4f;
    private final int THUMBNAIL_NUM = 5;
    private GVRSceneObject[] ThumbnailObject = new GVRSceneObject[THUMBNAIL_NUM];
    private GVRSceneObject[] ThumbnailRotation = new GVRSceneObject[THUMBNAIL_NUM];
    private GVRRenderData[] ThumbnailGlasses = new GVRRenderData[THUMBNAIL_NUM];
    private GVRSceneObject[] Thumbnails = new GVRSceneObject[THUMBNAIL_NUM];
    private float[][] ThumbnailTargetPosition = new float[THUMBNAIL_NUM][3];
    private int[] ThumbnailTargetIndex = new int[THUMBNAIL_NUM];
    private float[][] ThumbnailCurrentPosition = new float[THUMBNAIL_NUM][3];
    private int[] ThumbnailOrder = new int[THUMBNAIL_NUM];
    public int ThumbnailSelected = 2;
    public boolean mUseThumbnails = false;
    private GVRSceneObject mWidgetButtonObject;
    private GVRSceneObject mWdgetButtonObject2;

    private boolean mSelectionActive = false;
    public boolean mButtonPointed = false;
    public boolean mObjectPointed = true;

    private PickHandler mPickHandler = new PickHandler();

    public GVRSceneObject mObjectPos;
    public GVRSceneObject mObjectRot;
    public float mRotateX = 0.0f;
    public float mRotateY = 0.0f;
    public float mRotateZ = 0.0f;

    public float mLastRotateX = 0.0f;
    public float mLastRotateY = 0.0f;
    public float mLastRotateZ = 0.0f;

    public boolean mResetRotate = false;
    GVRTexture mBlueTex;
    GVRTexture mBlackTex;
    GVRTexture mGreenTex;
    GVRTexture mSilverTex;
    GVRTexture mDefaultColorTex;
    public int mTexColor = 1;
    public boolean mLookInside = false;
    public float mZoomLevel = -2.0f;

    ViewerMain(GVRWidgetPlugin plugin) {
        mPlugin = plugin;
    }
    GVRTexture mWidgetTexture = null;
    GVRMaterial mWidgetMaterial;
    GVRMaterial mWidgetMaterial2;

    public class PickHandler extends GVREventListeners.TouchEvents
    {
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            if ((mWidgetButtonObject == sceneObj) ||
                (mWdgetButtonObject2 == sceneObj))
            {
                mButtonPointed = false;
            }
            mObjectPointed = true;
            mSelectionActive = false;
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            mButtonPointed = false;
            if ((mWidgetButtonObject == sceneObj) ||
                (mWdgetButtonObject2 == sceneObj))
            {
                mObjectPointed = false;
                mButtonPointed = true;
            }
            mSelectionActive = true;
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            if (pickInfo.isTouched())
            {
                updateState();
            }
        }

        public void onMotionOutside(GVRPicker picker, MotionEvent event)
        {
            updateState();
        }
    }

    private GVRInputManager.ICursorControllerSelectListener mControllerSelector = new GVRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
        {
            if (oldController != null)
            {
                oldController.removePickEventListener(mPlugin.getTouchHandler());
                oldController.removePickEventListener(mPickHandler);
            }
            newController.addPickEventListener(mPickHandler);
            newController.addPickEventListener(mPlugin.getTouchHandler());
        }
    };

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        final GVRAssetLoader assetLoader = mGVRContext.getAssetLoader();
        mScene = mGVRContext.getMainScene();
        mScene.setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        gvrContext.getInputManager().selectController(mControllerSelector);

        try
        {
            makeMaterials();
            mScene.addSceneObject(makeObjects(gvrContext));
            if (mUseThumbnails)
            {
                makeThumbnails();
            }
            makeWidgetButtons();

            GVRTexture m360 = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "env.jpg"));
            GVRMesh sphere = assetLoader.loadMesh(new GVRAndroidResource(mGVRContext, "sphere.obj"));

            GVRSceneObject env_object = new GVRSceneObject(mGVRContext, sphere, m360);
            env_object.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            mScene.addSceneObject(env_object);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Assets were not loaded. Stopping application!");
            gvrContext.getActivity().finish();
        }
    }

    public void selectObject()
    {
        GVRSwitch selector = (GVRSwitch) mObjectRot.getComponent(GVRSwitch.getComponentType());
        selector.setSwitchIndex(ThumbnailSelected);
    }

    private void updateState()
    {
        if (mUseThumbnails)
        {
            moveThumbnails();
        }
        if (mRotateY - mLastRotateY != 0.0f)
        {
            mObjectRot.getTransform().setRotationByAxis(mRotateY, 0.0f, 1.0f, 0.0f);
            Log.d("NOLA", "updateState rotateY = %f", mRotateY);
            mLastRotateZ = mRotateZ;
            mLastRotateX = mRotateX;
            mLastRotateY = mRotateY;
        }
        else if (mResetRotate)
        {
            mLastRotateZ = mRotateZ = 0.0f;
            mLastRotateX = mRotateX = 0.0f;
            mLastRotateY = mRotateY = 0.0f;
            mResetRotate = false;
            mObjectRot.getTransform().setRotation(1.0f, 0.0f, 0.0f, 0.0f);
        }
        if ((ThumbnailSelected == 3) && mLookInside)
        {
            mObjectPos.getTransform().setPositionZ(0);
        }
        else
        {
            mObjectPos.getTransform().setPositionZ(-EYE_TO_OBJECT + mZoomLevel);
        }
        updateLighting();
        if (ThumbnailSelected == 3 || ThumbnailSelected == 1)
        {
            switch (mTexColor)
            {
                case 1:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, mDefaultColorTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY, mDefaultColorTex);
                    break;
                case 2:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, mBlackTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY, mBlackTex);
                    break;
                case 3:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, mBlueTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY, mBlueTex);
                    break;
                case 4:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, mGreenTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY, mGreenTex);
                    break;
                case 5:
                    mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, mSilverTex);
                    mPhongMaterial.setTexture(PhongShader3.TEXTURE_KEY, mSilverTex);
                    break;
            }
        }
    }

    public void onButtonDown() {
        mGVRContext.getMainScene().getMainCameraRig().resetYaw();
    }

    public void onSingleTap(MotionEvent e)
    {
        if (ThumbnailSelected == 3 && mLookInside)
            mLookInside = false;
    }

    private void moveThumbnails()
    {
        boolean MoveActive = false;
        if (Math.abs(ThumbnailTargetPosition[ThumbnailTargetIndex[0]][0]
                     - ThumbnailCurrentPosition[0][0]) < 0.2f)
            MoveActive = true;

        if ((mSelectionActive && MoveActive)) {
            if (ThumbnailCurrentPosition[ThumbnailSelected][0] < -0.5f) {
                for (int i = 0; i < THUMBNAIL_NUM; i++) {
                    ThumbnailTargetIndex[i]++;
                    if (ThumbnailTargetIndex[i] >= THUMBNAIL_NUM)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                                  - THUMBNAIL_NUM;
                    if (ThumbnailTargetIndex[i] < 0)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                                  + THUMBNAIL_NUM;
                }
            } else if (ThumbnailCurrentPosition[ThumbnailSelected][0] > 0.5f) {
                for (int i = 0; i < THUMBNAIL_NUM; i++) {
                    ThumbnailTargetIndex[i]--;
                    if (ThumbnailTargetIndex[i] >= THUMBNAIL_NUM)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                                  - THUMBNAIL_NUM;
                    if (ThumbnailTargetIndex[i] < 0)
                        ThumbnailTargetIndex[i] = ThumbnailTargetIndex[i]
                                                  + THUMBNAIL_NUM;
                }
            }
        }

        for (int i = 0; i < THUMBNAIL_NUM; i++) {
            float speed = 0.08f;
            for (int j = 0; j < 3; j++)
                ThumbnailCurrentPosition[i][j] += speed
                                                  * (ThumbnailTargetPosition[ThumbnailTargetIndex[i]][j] - ThumbnailCurrentPosition[i][j]);
            ThumbnailObject[i].getTransform().setPosition(
                    ThumbnailCurrentPosition[i][0],
                    ThumbnailCurrentPosition[i][1],
                    ThumbnailCurrentPosition[i][2]);
        }
    }

    private void makeMaterials() throws IOException
    {
        GVRAssetLoader assetLoader = mGVRContext.getAssetLoader();
        mBlueTex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "blue.png"));
        mBlackTex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "black.png"));
        mGreenTex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "green.png"));
        mSilverTex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "silver.png"));

        GVRTexture env_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "env.jpg"));
        mReflectionMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(ReflectionShader.class));
        mReflectionMaterial.setVec4(ReflectionShader.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mReflectionMaterial.setFloat(ReflectionShader.RADIUS_KEY, 10.0f);
        mReflectionMaterial.setTexture(ReflectionShader.TEXTURE_KEY, env_tex);

        // watch
        mMetalMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(MetalOnlyShader.class));
        mMetalMaterial.setVec4(MetalOnlyShader.COLOR_KEY, 1.7f, 1.4f, 1.0f, 1.0f);
        mMetalMaterial.setFloat(MetalOnlyShader.RADIUS_KEY, 10.0f);
        mMetalMaterial.setTexture(MetalOnlyShader.TEXTURE_KEY, env_tex);

        mGlassMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(GlassShader.class));
        mGlassMaterial.setVec4(GlassShader.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mGlassMaterial.setFloat(GlassShader.RADIUS_KEY, 10.0f);
        mGlassMaterial.setTexture(GlassShader.TEXTURE_KEY, env_tex);

        GVRTexture board_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "watch/board.jpg"));
        mDiffuseMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(DiffuseShader.class));
        mDiffuseMaterial.setVec4(DiffuseShader.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mDiffuseMaterial.setTexture(DiffuseShader.TEXTURE_KEY, board_tex);

        // jar
        mPhongMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader.class));
        mPhongMaterial.setVec4(PhongShader.COLOR_KEY, 1.2f, 1.2f, 1.3f, 1.0f);
        mPhongMaterial.setFloat(PhongShader.RADIUS_KEY, 10.0f);
        mPhongMaterial.setTexture(PhongShader.TEXTURE_KEY, env_tex);

        // car
        GVRTexture car_body_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "car/body.jpg"));
        mDefaultColorTex = car_body_tex;
        mCarBodyMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader3.class));
        mCarBodyMaterial.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
        mCarBodyMaterial.setTexture(PhongShader3.ENV_KEY, env_tex);
        mCarBodyMaterial.setTexture(PhongShader3.TEXTURE_KEY, car_body_tex);

        mCarWheelMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(MetalShader2.class));
        mCarWheelMaterial.setVec4(MetalShader2.COLOR_KEY, 1.2f, 1.2f, 1.2f, 1.0f);
        mCarWheelMaterial.setFloat(MetalShader2.RADIUS_KEY, 10.0f);
        mCarWheelMaterial.setTexture(MetalShader2.TEXTURE_KEY, env_tex);

        mCarGlassMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(GlassShader2.class));
        mCarGlassMaterial.setVec4(GlassShader2.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mCarGlassMaterial.setFloat(GlassShader2.RADIUS_KEY, 10.0f);
        mCarGlassMaterial.setTexture(GlassShader2.TEXTURE_KEY, env_tex);

        GVRTexture default_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "car/default.png"));
        mCarTireMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(DiffuseShader2.class));
        mCarTireMaterial.setVec4(DiffuseShader2.COLOR_KEY, 0.1f, 0.1f, 0.1f, 1.0f);
        mCarTireMaterial.setTexture(DiffuseShader2.TEXTURE_KEY, default_tex);

        GVRTexture back_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "car/back.jpg"));
        mCarBackMaterial = new GVRMaterial(mGVRContext,  new GVRShaderId(DiffuseShader2.class));
        mCarBackMaterial.setVec4(DiffuseShader2.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mCarBackMaterial.setTexture(DiffuseShader2.TEXTURE_KEY, back_tex);

        GVRTexture grill_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "car/grill.jpg"));
        mCarGrillMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(DiffuseShader2.class));
        mCarGrillMaterial.setVec4(DiffuseShader2.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mCarGrillMaterial.setTexture(DiffuseShader2.TEXTURE_KEY, grill_tex);

        mCarLightMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(GlassShader2.class));
        mCarLightMaterial.setVec4(GlassShader2.COLOR_KEY, 2.5f, 2.5f, 2.5f, 1.0f);
        mCarLightMaterial.setFloat(GlassShader2.RADIUS_KEY, 10.0f);
        mCarLightMaterial.setTexture(GlassShader2.TEXTURE_KEY, env_tex);

        mCarInsideMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader2.class));
        mCarInsideMaterial.setVec4(PhongShader2.COLOR_KEY, 0.0f, 0.0f, 0.0f, 1.0f);
        mCarInsideMaterial.setFloat(PhongShader2.RADIUS_KEY, 10.0f);
        mCarInsideMaterial.setTexture(PhongShader2.TEXTURE_KEY, env_tex);

        // robot
        GVRTexture robot_head_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "robot/head.jpg"));
        mRobotHeadMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader3.class));
        mRobotHeadMaterial.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
        mRobotHeadMaterial.setTexture(PhongShader3.ENV_KEY, env_tex);
        mRobotHeadMaterial.setTexture(PhongShader3.TEXTURE_KEY, robot_head_tex);

        mRobotMetalMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(MetalShader2.class));
        mRobotMetalMaterial.setVec4(MetalShader2.COLOR_KEY, 1.5f, 1.5f, 1.5f, 1.0f);
        mRobotMetalMaterial.setFloat(MetalShader2.RADIUS_KEY, 10.0f);
        mRobotMetalMaterial.setTexture(MetalShader2.TEXTURE_KEY, env_tex);

        mRobotBodyMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader2.class));
        mRobotBodyMaterial.setVec4(PhongShader2.COLOR_KEY, 1.0f, 1.0f, 1.0f, 1.0f);
        mRobotBodyMaterial.setFloat(PhongShader2.RADIUS_KEY, 10.0f);
        mRobotBodyMaterial.setTexture(PhongShader2.TEXTURE_KEY, env_tex);

        mRobotRubberMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(DiffuseShader2.class));
        mRobotRubberMaterial.setVec4(DiffuseShader2.COLOR_KEY, 0.3f, 0.3f, 0.3f, 1.0f);
        mRobotRubberMaterial.setTexture(DiffuseShader2.TEXTURE_KEY, default_tex);

        GVRTexture leaf_box_tex;

        leaf_box_tex = assetLoader.loadTexture(new GVRAndroidResource(mGVRContext, "leaf/box.jpg"));

        mLeafBoxMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader3.class));
        mLeafBoxMaterial.setFloat(PhongShader3.RADIUS_KEY, 10.0f);
        mLeafBoxMaterial.setTexture(PhongShader3.ENV_KEY, env_tex);
        mLeafBoxMaterial.setTexture(PhongShader3.TEXTURE_KEY, leaf_box_tex);

        mLeafBodyMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(MetalShader2.class));
        mLeafBodyMaterial.setVec4(MetalShader2.COLOR_KEY, 2.5f, 2.5f, 2.5f, 1.0f);
        mLeafBodyMaterial.setFloat(MetalShader2.RADIUS_KEY, 10.0f);
        mLeafBodyMaterial.setTexture(MetalShader2.TEXTURE_KEY, env_tex);
    }

    /*
     * This demo was created before the asset loader could import OBJ materials
     * from an MTL file. The proper way to do this now would be to include an MTL
     * file in th4e OBJ which described the materials instead of programmatically
     * creating them.
     */
    private GVRSceneObject makeObjects(GVRContext ctx) throws IOException
    {
        GVRSceneObject.BoundingVolume bv;
        GVRAssetLoader loader = ctx.getAssetLoader();
        mObjectPos = new GVRSceneObject(ctx);
        mObjectPos.getTransform().setPositionZ(-EYE_TO_OBJECT);
        mObjectRot = new GVRSceneObject(ctx);
        GVRSwitch selector = new GVRSwitch(ctx);
        mObjectRot.attachComponent(selector);
        mObjectPos.addChildObject(mObjectRot);


        // leaf
        GVRSceneObject leafRoot = new GVRSceneObject(ctx);
        GVRSceneObject obj20 = new GVRSceneObject(ctx);
        GVRRenderData renderData20 = new GVRRenderData(ctx);
        GVRMesh mesh20 = loader.loadMesh(new GVRAndroidResource(ctx, "leaf/leaf.obj"));
        renderData20.setMesh(mesh20);
        renderData20.setMaterial(mLeafBodyMaterial);
        obj20.attachRenderData(renderData20);
        leafRoot.addChildObject(obj20);

        GVRSceneObject obj21 = new GVRSceneObject(ctx);
        GVRRenderData renderData21 = new GVRRenderData(ctx);
        GVRMesh mesh21 = loader.loadMesh(new GVRAndroidResource(ctx, "leaf/box.obj"));
        renderData21.setMesh(mesh21);
        renderData21.setMaterial(mLeafBoxMaterial);
        obj21.attachRenderData(renderData21);
        leafRoot.addChildObject(obj21);
        bv = leafRoot.getBoundingVolume();
        leafRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        mObjectRot.addChildObject(leafRoot);

        // --------------bike
        GVRSceneObject bikeRoot = new GVRSceneObject(ctx);
        GVRSceneObject obj5 = new GVRSceneObject(ctx);
        GVRRenderData renderData5 = new GVRRenderData(ctx);
        GVRMesh mesh5 = loader.loadMesh(new GVRAndroidResource(ctx, "bike/bike.obj"));
        renderData5.setMesh(mesh5);
        renderData5.setMaterial(mPhongMaterial);
        obj5.attachRenderData(renderData5);
        bikeRoot.addChildObject(obj5);
        bv = bikeRoot.getBoundingVolume();
        //bikeRoot.getTransform().setPosition(0.7f - bv.center.x, -1.0f - bv.center.y, -2.0f - bv.center.z);

        bikeRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        mObjectRot.addChildObject(bikeRoot);

        // --------------watch
        GVRSceneObject watchRoot = new GVRSceneObject(ctx);
        GVRSceneObject obj1 = new GVRSceneObject(ctx);
        GVRRenderData renderData1 = new GVRRenderData(ctx);
        GVRMesh mesh1 = loader.loadMesh(new GVRAndroidResource(ctx, "watch/frame.obj"));
        renderData1.setMesh(mesh1);
        renderData1.setMaterial(mMetalMaterial);
        obj1.attachRenderData(renderData1);
        watchRoot.addChildObject(obj1);

        GVRSceneObject obj2 = new GVRSceneObject(ctx);
        GVRRenderData renderData2 = new GVRRenderData(ctx);
        GVRMesh mesh2 = loader.loadMesh(new GVRAndroidResource(ctx, "watch/board.obj"));
        renderData2.setMesh(mesh2);
        renderData2.setMaterial(mDiffuseMaterial);
        obj2.attachRenderData(renderData2);
        watchRoot.addChildObject(obj2);

        GVRSceneObject obj3 = new GVRSceneObject(ctx);
        GVRRenderData renderData3 = new GVRRenderData(ctx);
        GVRMesh mesh3 = loader.loadMesh(new GVRAndroidResource(ctx, "watch/glass.obj"));
        renderData3.setMesh(mesh3);
        renderData3.setMaterial(mGlassMaterial);
        obj3.attachRenderData(renderData3);
        obj3.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        watchRoot.addChildObject(obj3);
        bv = watchRoot.getBoundingVolume();
        watchRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        mObjectRot.addChildObject(watchRoot);

        // --------------car
        GVRSceneObject carRoot = new GVRSceneObject(ctx);
        GVRSceneObject obj6 = new GVRSceneObject(ctx);
        GVRRenderData renderData6 = new GVRRenderData(ctx);
        GVRMesh mesh6 = loader.loadMesh(new GVRAndroidResource(ctx, "car/body.obj"));
        renderData6.setMesh(mesh6);
        renderData6.setMaterial(mCarBodyMaterial);
        obj6.attachRenderData(renderData6);
        obj6.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
        carRoot.addChildObject(obj6);

        GVRSceneObject obj9 = new GVRSceneObject(ctx);
        GVRRenderData renderData9 = new GVRRenderData(ctx);
        GVRMesh mesh9 = loader.loadMesh(new GVRAndroidResource(ctx, "car/tire.obj"));
        renderData9.setMesh(mesh9);
        renderData9.setMaterial(mCarTireMaterial);
        obj9.attachRenderData(renderData9);
        carRoot.addChildObject(obj9);

        GVRSceneObject obj10 = new GVRSceneObject(ctx);
        GVRRenderData renderData10 = new GVRRenderData(ctx);
        GVRMesh mesh10 = loader.loadMesh(new GVRAndroidResource(ctx, "car/glass.obj"));
        renderData10.setMesh(mesh10);
        renderData10.setMaterial(mCarGlassMaterial);
        obj10.attachRenderData(renderData10);
        obj10.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
        obj10.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        carRoot.addChildObject(obj10);

        GVRSceneObject obj11 = new GVRSceneObject(ctx);
        GVRRenderData renderData11 = new GVRRenderData(ctx);
        GVRMesh mesh11 = loader.loadMesh(new GVRAndroidResource(ctx, "car/wheel.obj"));
        renderData11.setMesh(mesh11);
        renderData11.setMaterial(mCarWheelMaterial);
        obj11.attachRenderData(renderData11);
        carRoot.addChildObject(obj11);

        GVRSceneObject obj12 = new GVRSceneObject(ctx);
        GVRRenderData renderData12 = new GVRRenderData(ctx);
        GVRMesh mesh12 = loader.loadMesh(new GVRAndroidResource(ctx, "car/back.obj"));
        renderData12.setMesh(mesh12);
        renderData12.setMaterial(mCarBackMaterial);
        obj12.attachRenderData(renderData12);
        carRoot.addChildObject(obj12);

        GVRSceneObject obj13 = new GVRSceneObject(mGVRContext);
        GVRRenderData renderData13 = new GVRRenderData(mGVRContext);
        GVRMesh mesh13 = loader.loadMesh(new GVRAndroidResource(ctx, "car/grill.obj"));
        renderData13.setMesh(mesh13);
        renderData13.setMaterial(mCarGrillMaterial);
        obj13.attachRenderData(renderData13);
        obj10.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        carRoot.addChildObject(obj13);

        GVRSceneObject obj14 = new GVRSceneObject(mGVRContext);
        GVRRenderData renderData14 = new GVRRenderData(mGVRContext);
        GVRMesh mesh14 = loader.loadMesh(new GVRAndroidResource(ctx, "car/glass2.obj"));
        renderData14.setMesh(mesh14);
        renderData14.setMaterial(mCarLightMaterial);
        obj14.attachRenderData(renderData14);
        obj14.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT + 1);
        carRoot.addChildObject(obj14);

        GVRSceneObject obj19 = new GVRSceneObject(ctx);
        GVRRenderData renderData19 = new GVRRenderData(ctx);
        GVRMesh mesh19 = loader.loadMesh(new GVRAndroidResource(ctx, "car/inside.obj"));
        renderData19.setMesh(mesh19);
        renderData19.setMaterial(mCarInsideMaterial);
        obj19.attachRenderData(renderData19);
        carRoot.addChildObject(obj19);
        bv = carRoot.getBoundingVolume();
        carRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        carRoot.getTransform().setScale(0.5f, 0.5f, 0.5f);
        mObjectRot.addChildObject(carRoot);

        GVRSceneObject robotRoot = new GVRSceneObject(ctx);
        GVRSceneObject obj15 = new GVRSceneObject(ctx);
        GVRRenderData renderData15 = new GVRRenderData(ctx);
        GVRMesh mesh15 = loader.loadMesh(new GVRAndroidResource(ctx, "robot/body.obj"));
        renderData15.setMesh(mesh15);
        renderData15.setMaterial(mRobotBodyMaterial);
        obj15.attachRenderData(renderData15);
        robotRoot.addChildObject(obj15);

        GVRSceneObject obj16 = new GVRSceneObject(ctx);
        GVRRenderData renderData16 = new GVRRenderData(ctx);
        GVRMesh mesh16 = loader.loadMesh(new GVRAndroidResource(ctx, "robot/head.obj"));
        renderData16.setMesh(mesh16);
        renderData16.setMaterial(mRobotHeadMaterial);
        obj16.attachRenderData(renderData16);
        robotRoot.addChildObject(obj16);

        GVRSceneObject obj17 = new GVRSceneObject(ctx);
        GVRRenderData renderData17 = new GVRRenderData(ctx);
        GVRMesh mesh17 = loader.loadMesh(new GVRAndroidResource(ctx, "robot/metal.obj"));
        renderData17.setMesh(mesh17);
        renderData17.setMaterial(mRobotMetalMaterial);
        obj17.attachRenderData(renderData17);
        obj17.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        robotRoot.addChildObject(obj17);

        GVRSceneObject obj18 = new GVRSceneObject(ctx);
        GVRRenderData renderData18 = new GVRRenderData(ctx);
        GVRMesh mesh18 = loader.loadMesh(new GVRAndroidResource(ctx, "robot/rubber.obj"));
        renderData18.setMesh(mesh18);
        renderData18.setMaterial(mRobotRubberMaterial);
        obj18.attachRenderData(renderData18);
        robotRoot.addChildObject(obj18);
        bv = robotRoot.getBoundingVolume();
        robotRoot.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        mObjectRot.addChildObject(robotRoot);

        return mObjectPos;
    }


    /*
     * Updates the lighting information in all the materials.
     * This demo was written before GearVRF supported multiple lights.
     * The better way to do this now is to attach light sources to
     * scene objects and let the GVRPhongShader automatically
     * apply lighting to your objects.
     */
    private void updateLighting()
    {

        float[] light = new float[4];
        light[0] = 6.0f;
        light[1] = 10.0f;
        light[2] = 10.0f;
        light[3] = 1.0f;

        float[] eye = new float[4];
        eye[0] = 0.0f;
        eye[1] = 0.0f;
        eye[2] = 3.0f * EYE_TO_OBJECT;
        eye[3] = 1.0f;

        float[] matO = mObjectRot.getTransform().getModelMatrix();

        // ---------------------------- watch, jar

        float x = matO[0] * light[0] + matO[1] * light[1] + matO[2] * light[2] + matO[3] * light[3];
        float y = matO[4] * light[0] + matO[5] * light[1] + matO[6] * light[2] + matO[7] * light[3];
        float z = matO[8] * light[0] + matO[9] * light[1] + matO[10] * light[2] + matO[11] * light[3];
        float mag = (float) Math.sqrt(x * x + y * y + z * z);

        mMetalMaterial.setVec3(MetalOnlyShader.LIGHT_KEY, x / mag, y / mag, z / mag);
        mDiffuseMaterial.setVec3(DiffuseShader.LIGHT_KEY, x / mag, y / mag, z / mag);
        mGlassMaterial.setVec3(GlassShader.LIGHT_KEY, x / mag, y / mag, z / mag);
        mPhongMaterial.setVec3(PhongShader.LIGHT_KEY, x / mag, y / mag, z / mag);

        x = matO[0] * eye[0] + matO[1] * eye[1] + matO[2] * eye[2] + matO[3] * eye[3];
        y = matO[4] * eye[0] + matO[5] * eye[1] + matO[6] * eye[2] + matO[7] * eye[3];
        z = matO[8] * eye[0] + matO[9] * eye[1] + matO[10] * eye[2] + matO[11] * eye[3];
        mag = (float) Math.sqrt(x * x + y * y + z * z);

        mMetalMaterial.setVec3(MetalOnlyShader.EYE_KEY, x / mag, y / mag, z / mag);
        mDiffuseMaterial.setVec3(DiffuseShader.EYE_KEY, x / mag, y / mag, z / mag);
        mGlassMaterial.setVec3(GlassShader.EYE_KEY, x / mag, y / mag, z / mag);
        mPhongMaterial.setVec3(PhongShader.EYE_KEY, x / mag, y / mag, z / mag);

        // ---------------------------- robot

        mRobotHeadMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mRobotHeadMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mRobotHeadMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mRobotHeadMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mRobotHeadMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mRobotHeadMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mRobotMetalMaterial.setVec4(MetalShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mRobotMetalMaterial.setVec4(MetalShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mRobotMetalMaterial.setVec4(MetalShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mRobotMetalMaterial.setVec4(MetalShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mRobotMetalMaterial.setVec3(MetalShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mRobotMetalMaterial.setVec3(MetalShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mRobotBodyMaterial.setVec4(PhongShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mRobotBodyMaterial.setVec4(PhongShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mRobotBodyMaterial.setVec4(PhongShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mRobotBodyMaterial.setVec4(PhongShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mRobotBodyMaterial.setVec3(PhongShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mRobotBodyMaterial.setVec3(PhongShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mRobotRubberMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mRobotRubberMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mRobotRubberMaterial.setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        // ---------------------------- leaf

        mLeafBodyMaterial.setVec4(MetalShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mLeafBodyMaterial.setVec4(MetalShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mLeafBodyMaterial.setVec4(MetalShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mLeafBodyMaterial.setVec4(MetalShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mLeafBodyMaterial.setVec3(MetalShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mLeafBodyMaterial.setVec3(MetalShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mLeafBoxMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mLeafBoxMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mLeafBoxMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mLeafBoxMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mLeafBoxMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mLeafBoxMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        // ---------------------------- car
        eye[0] = 4.0f;
        eye[1] = 0.0f;
        eye[2] = 3.0f * EYE_TO_OBJECT;
        eye[3] = 1.0f;

        mCarBodyMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarBodyMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarBodyMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarBodyMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarBodyMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mCarBodyMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarTireMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarTireMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarTireMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarTireMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarTireMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarTireMaterial.setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarGlassMaterial.setVec4(GlassShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarGlassMaterial.setVec4(GlassShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarGlassMaterial.setVec4(GlassShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarGlassMaterial.setVec4(GlassShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarGlassMaterial.setVec3(GlassShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarGlassMaterial.setVec3(GlassShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarWheelMaterial.setVec4(MetalShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarWheelMaterial.setVec4(MetalShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarWheelMaterial.setVec4(MetalShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarWheelMaterial.setVec4(MetalShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarWheelMaterial.setVec3(MetalShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarWheelMaterial.setVec3(MetalShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarBackMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarBackMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarBackMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarBackMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarBackMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarBackMaterial.setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarGrillMaterial.setVec4(DiffuseShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarGrillMaterial.setVec4(DiffuseShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarGrillMaterial.setVec4(DiffuseShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarGrillMaterial.setVec4(DiffuseShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarGrillMaterial.setVec3(DiffuseShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarGrillMaterial.setVec3(DiffuseShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarLightMaterial.setVec4(GlassShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarLightMaterial.setVec4(GlassShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarLightMaterial.setVec4(GlassShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarLightMaterial.setVec4(GlassShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarLightMaterial.setVec3(GlassShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarLightMaterial.setVec3(GlassShader2.EYE_KEY, eye[0], eye[1], eye[2]);

        mCarInsideMaterial.setVec4(PhongShader2.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mCarInsideMaterial.setVec4(PhongShader2.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mCarInsideMaterial.setVec4(PhongShader2.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mCarInsideMaterial.setVec4(PhongShader2.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mCarInsideMaterial.setVec3(PhongShader2.LIGHT_KEY, light[0], light[1], light[2]);
        mCarInsideMaterial.setVec3(PhongShader2.EYE_KEY, eye[0], eye[1], eye[2]);
        // ---------------------------- thumbnail glasses

        eye[0] = 0.0f;
        eye[1] = 0.0f;
        eye[2] = EYE_TO_OBJECT;
        eye[3] = 1.0f;

        if (mUseThumbnails)
        {
            float[] matT = ThumbnailRotation[0].getTransform().getModelMatrix();
            mReflectionMaterial.setVec4(ReflectionShader.MAT1_KEY, matT[0], matT[4], matT[8],
                                        matT[12]);
            mReflectionMaterial.setVec4(ReflectionShader.MAT2_KEY, matT[1], matT[5], matT[9],
                                        matT[13]);
            mReflectionMaterial.setVec4(ReflectionShader.MAT3_KEY, matT[2], matT[6], matT[10],
                                        matT[14]);
            mReflectionMaterial.setVec4(ReflectionShader.MAT4_KEY, matT[3], matT[7], matT[11],
                                        matT[15]);
            mReflectionMaterial.setVec3(ReflectionShader.LIGHT_KEY, light[0], light[1], light[2]);
            mReflectionMaterial.setVec3(ReflectionShader.EYE_KEY, eye[0], eye[1], eye[2]);
        }
    }

    private void makeWidgetButtons() throws IOException
    {
        GVRMesh widgetbutton2_mesh = mGVRContext.getAssetLoader().loadMesh(new GVRAndroidResource(mGVRContext, "button2.obj"));

        mWidgetTexture = new GVRSharedTexture(mGVRContext, mPlugin.getTextureId());

        GVRWidgetSceneObjectMeshInfo info = new GVRWidgetSceneObjectMeshInfo(
                -2.5f, 1.0f, -1.5f, -1.0f, new int[]{0, 0}, new int[]{1280, 1440});

        GVRWidgetSceneObjectMeshInfo info2 =new GVRWidgetSceneObjectMeshInfo(
                1.5f,1.0f,2.5f,-1.0f,new int[] { 1281, 0 },new int[] { 2560, 1440 });

        mWidgetButtonObject = new GVRWidgetSceneObject(mGVRContext,
                                                       mPlugin.getTextureId(), info, mPlugin.getWidth(),
                                                       mPlugin.getHeight());
        mWdgetButtonObject2 = new GVRWidgetSceneObject(mGVRContext,
                                                       mPlugin.getTextureId(), info2, mPlugin.getWidth(),
                                                       mPlugin.getHeight());
        GVRRenderData ldata = new GVRRenderData(mGVRContext);
        GVRRenderData ldata2 = new GVRRenderData(mGVRContext);

        mWidgetMaterial2 = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader3.class));

        ldata2.setMesh(widgetbutton2_mesh);
        ldata2.setMaterial(mWidgetMaterial2);
        float[] light = new float[4];
        light[0] = 6.0f;
        light[1] = 10.0f;
        light[2] = 10.0f;
        light[3] = 1.0f;

        float[] eye = new float[4];
        eye[0] = 0.0f;
        eye[1] = 0.0f;
        eye[2] = 3.0f * EYE_TO_OBJECT;
        eye[3] = 1.0f;

        float[] matO = mObjectRot.getTransform().getModelMatrix();

        mWidgetMaterial = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader3.class));//new GVRMaterial(gvrContext, GVRShaderType.UnlitFBO.ID);
        ldata.setMaterial(mWidgetMaterial);
        mWidgetMaterial.setMainTexture(mWidgetTexture);
        mWidgetMaterial.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mWidgetMaterial.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mWidgetMaterial.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mWidgetMaterial.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mWidgetMaterial.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mWidgetMaterial.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        //mWidgetMaterial2 = new GVRMaterial(gvrContext);
        mWidgetMaterial2 = new GVRMaterial(mGVRContext, new GVRShaderId(PhongShader3.class));
        mWidgetMaterial2.setMainTexture(mWidgetTexture);
        mWidgetMaterial2.setVec4(PhongShader3.MAT1_KEY, matO[0], matO[4], matO[8], matO[12]);
        mWidgetMaterial2.setVec4(PhongShader3.MAT2_KEY, matO[1], matO[5], matO[9], matO[13]);
        mWidgetMaterial2.setVec4(PhongShader3.MAT3_KEY, matO[2], matO[6], matO[10], matO[14]);
        mWidgetMaterial2.setVec4(PhongShader3.MAT4_KEY, matO[3], matO[7], matO[11], matO[15]);
        mWidgetMaterial2.setVec3(PhongShader3.LIGHT_KEY, light[0], light[1], light[2]);
        mWidgetMaterial2.setVec3(PhongShader3.EYE_KEY, eye[0], eye[1], eye[2]);

        mWidgetButtonObject.getTransform().setPosition(0, 0, -EYE_TO_OBJECT - 1.5f);
        mWidgetButtonObject.getTransform().rotateByAxis(40.0f, 0.0f, 1.0f, 0.0f);
        mWidgetButtonObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);

        mWdgetButtonObject2.getTransform().setPosition(0, 0, -EYE_TO_OBJECT - 1.5f);
        mWdgetButtonObject2.getTransform().rotateByAxis(-40.0f, 0.0f, 1.0f, 0.0f);
        mWdgetButtonObject2.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        mScene.addSceneObject(mWidgetButtonObject);
        mScene.addSceneObject(mWdgetButtonObject2);
    }

    private void makeThumbnails() throws IOException
    {
        GVRAssetLoader assetLoader = mGVRContext.getAssetLoader();
        GVRMesh glass_mesh = assetLoader.loadMesh(new GVRAndroidResource(mGVRContext, "glass.obj"));
        GVRMesh board_mesh = assetLoader.loadMesh(new GVRAndroidResource(mGVRContext, "board.obj"));

        int i;
        ThumbnailTargetPosition[0][0] = -2.2f;
        ThumbnailTargetPosition[0][1] = 0.0f;
        ThumbnailTargetPosition[0][2] = -EYE_TO_OBJECT - 2.8f;
        ThumbnailTargetPosition[1][0] = -1.0f;
        ThumbnailTargetPosition[1][1] = 0.0f;
        ThumbnailTargetPosition[1][2] = -EYE_TO_OBJECT - 1.5f;
        ThumbnailTargetPosition[2][0] = 0.0f;
        ThumbnailTargetPosition[2][1] = 0.0f;
        ThumbnailTargetPosition[2][2] = -EYE_TO_OBJECT - 0.0f;
        ThumbnailTargetPosition[3][0] = 1.0f;
        ThumbnailTargetPosition[3][1] = 0.0f;
        ThumbnailTargetPosition[3][2] = -EYE_TO_OBJECT - 1.5f;
        ThumbnailTargetPosition[4][0] = 2.4f;
        ThumbnailTargetPosition[4][1] = 0.0f;
        ThumbnailTargetPosition[4][2] = -EYE_TO_OBJECT - 2.8f;

        for (i = 0; i < THUMBNAIL_NUM; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                ThumbnailCurrentPosition[i][j] = ThumbnailTargetPosition[i][j];
            }
        }
        for (i = 0; i < THUMBNAIL_NUM; i++)
        {
            ThumbnailTargetIndex[i] = i;
        }
        ThumbnailOrder[0] = THUMBNAIL_RENDER_ORDER;
        ThumbnailOrder[1] = THUMBNAIL_RENDER_ORDER + 1;
        ThumbnailOrder[2] = THUMBNAIL_RENDER_ORDER + 2;
        ThumbnailOrder[3] = THUMBNAIL_RENDER_ORDER + 3;
        ThumbnailOrder[4] = THUMBNAIL_RENDER_ORDER + 4;
        for (i = 0; i < THUMBNAIL_NUM; i++)
        {
            ThumbnailObject[i] = new GVRSceneObject(mGVRContext);
            ThumbnailRotation[i] = new GVRSceneObject(mGVRContext);
            GVRSceneObject obj = new GVRSceneObject(mGVRContext, glass_mesh, mReflectionMaterial);
            ThumbnailGlasses[i] = obj.getRenderData();
            obj.getRenderData().setRenderingOrder(ThumbnailOrder[i] + 10);
            ThumbnailRotation[i].addChildObject(obj);
            ThumbnailObject[i].addChildObject(ThumbnailRotation[i]);

            Thumbnails[i] = new GVRSceneObject(mGVRContext, board_mesh /*ThumbnailTextures[i]*/);
            Thumbnails[i].getRenderData().setRenderingOrder(ThumbnailOrder[i]);
            Thumbnails[i].getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            Thumbnails[i].getTransform().setScale(1.0f, 1.2f, 1.0f);
            ThumbnailRotation[i].addChildObject(Thumbnails[i]);

            ThumbnailObject[i].getTransform().setPosition(
                    ThumbnailTargetPosition[i][0],
                    ThumbnailTargetPosition[i][1],
                    ThumbnailTargetPosition[i][2]);
            mScene.addSceneObject(ThumbnailObject[i]);

            GVRSphereCollider sphereCollider = new GVRSphereCollider(mGVRContext);
            sphereCollider.setRadius(0.5f);
            ThumbnailObject[i].attachComponent(sphereCollider);
        }
        for (i = 0; i < THUMBNAIL_NUM; ++i)
        {
            ThumbnailGlasses[i].setRenderMask(0);
            Thumbnails[i].getRenderData().setRenderMask(0);
        }
    }
}
