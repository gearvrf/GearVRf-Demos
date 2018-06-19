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
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRSharedTexture;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRWidgetViewer.R;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.utility.Log;
import org.gearvrf.widgetplugin.GVRWidgetPlugin;
import org.gearvrf.widgetplugin.GVRWidgetSceneObject;
import org.gearvrf.widgetplugin.GVRWidgetSceneObjectMeshInfo;

import java.io.IOException;

public class ViewerMain extends GVRMain {
    private static final String TAG = "ViewerMain";

    private GVRWidgetPlugin mPlugin = null;
    private GVRContext mGVRContext = null;
    private GVRScene mScene = null;

    private final float EYE_TO_OBJECT = 2.4f;
    public int ThumbnailSelected = 2;
    private GVRSceneObject mWidgetButtonObject;
    private GVRSceneObject mWdgetButtonObject2;
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
    public int mTexColor = 1;
    public boolean mLookInside = false;
    public float mZoomLevel = -2.0f;

    ViewerMain(GVRWidgetPlugin plugin) {
        mPlugin = plugin;
    }
    GVRTexture mWidgetTexture = null;
    GVRMaterial mWidgetMaterial;
    GVRMaterial mWidgetMaterial2;

    private GVRSceneObject mLightNode;

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.MANUAL;
    }

    public class PickHandler extends GVREventListeners.TouchEvents
    {
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            if ((mWidgetButtonObject == sceneObj) ||
                (mWdgetButtonObject2 == sceneObj))
            {
                mButtonPointed = false;
            }
            mObjectPointed = true;
        }

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            mButtonPointed = false;
            if ((mWidgetButtonObject == sceneObj) ||
                (mWdgetButtonObject2 == sceneObj))
            {
                mObjectPointed = false;
                mButtonPointed = true;
            }
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
        mScene = mGVRContext.getMainScene();
        mScene.setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        gvrContext.getInputManager().selectController(mControllerSelector);
        mScene.addSceneObject(addEnvironment());

        mLightNode = createLight(mGVRContext, 1, 1, 1, 2.8f);
        mScene.addSceneObject(mLightNode);

        try
        {
            mScene.addSceneObject(makeObjects(gvrContext));
            makeWidgetButtons();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, "Assets were not loaded. Stopping application!");
            gvrContext.getActivity().finish();
        }
        gvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                updateState();
                closeSplashScreen();
            }
        });
    }


    private GVRTexture mEnvTex;
    private GVRSceneObject addEnvironment()
    {
        mEnvTex = mGVRContext.getAssetLoader().loadCubemapTexture(new GVRAndroidResource(mGVRContext, R.raw.envmap));
        GVRMaterial mtl = new GVRMaterial(mGVRContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        mtl.setMainTexture(mEnvTex);
        GVRSceneObject env = new GVRCubeSceneObject(mGVRContext, false, mtl);
        env.getRenderData().disableLight();
        env.getTransform().setScale(100, 100, 100);

        return env;
    }

    public void selectObject()
    {
        GVRSwitch selector = (GVRSwitch) mObjectRot.getComponent(GVRSwitch.getComponentType());
        selector.setSwitchIndex(ThumbnailSelected);
    }

    private void updateState()
    {
        if (mRotateY - mLastRotateY != 0.0f)
        {
            mObjectRot.getTransform().setRotationByAxis(mRotateY, 0.0f, 1.0f, 0.0f);
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

        updateTexColor();
    }

    void updateTexColor()
    {
        switch (mTexColor)
        {
            case 1:
                setLightColor(mLightNode, 1,1,1);
                break;
            case 2:
                setLightColor(mLightNode, 1,0,0);
                break;
            case 3:
                setLightColor(mLightNode, 0,0,1);
                break;
            case 4:
                setLightColor(mLightNode, 0,1,0);
                break;
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

    private GVRSceneObject makeObjects(GVRContext ctx) throws IOException
    {
        mObjectPos = new GVRSceneObject(ctx);
        mObjectPos.getTransform().setPositionZ(-EYE_TO_OBJECT);
        mObjectRot = new GVRSceneObject(ctx);
        GVRSwitch selector = new GVRSwitch(ctx);
        mObjectRot.attachComponent(selector);
        mObjectPos.addChildObject(mObjectRot);

        addModeltoScene("/Suzanne/glTF/Suzanne.gltf", 1,1,1, false);
        addModeltoScene("/WaterBottle/glTF-pbrSpecularGlossiness/WaterBottle.gltf", 8,8,8, true);
        addModeltoScene("/BoomBox/glTF-pbrSpecularGlossiness/BoomBox.gltf", 70,70,70, true);
        addModeltoScene("/SciFiHelmet/glTF/SciFiHelmet.gltf", 1,1,1, true);
        addModeltoScene("/Corset/glTF/Corset.gltf", 50,50,50, true);

        return mObjectPos;
    }

    private void addModeltoScene(String filePath, float scaleX, float scaleY, float scaleZ, boolean hasSpecularEnv) throws IOException {

        GVRSceneObject.BoundingVolume bv;
        GVRAssetLoader loader = mGVRContext.getAssetLoader();
        GVRSceneObject root = loader.loadModel(filePath);
        if(hasSpecularEnv)
            setEnvironmentTex(root, mEnvTex);
        root.getTransform().setScale(scaleX,scaleY,scaleZ);
        bv = root.getBoundingVolume();
        root.getTransform().setPosition(-bv.center.x, -bv.center.y, -bv.center.z);
        mObjectRot.addChildObject(root);
    }


    private void setEnvironmentTex( GVRSceneObject obj, GVRTexture tex)
    {
        if(obj.getRenderData() != null)
            if(obj.getRenderData().getMaterial()!= null)
                obj.getRenderData().getMaterial().setTexture("specularEnvTexture", tex);

        for (GVRSceneObject child: obj.getChildren())
            setEnvironmentTex(child, tex);
    }

    private GVRSceneObject createLight(GVRContext context, float r, float g, float b, float y)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRSpotLight light = new GVRSpotLight(context);

        lightNode.attachLight(light);
        lightNode.getTransform().setPosition(0, 0.5f, 0);
        light.setAmbientIntensity(0.7f * r, 0.7f * g, 0.7f * b, 1);
        light.setDiffuseIntensity(r , g , b , 1);
        light.setSpecularIntensity(r, g, b, 1);
        light.setInnerConeAngle(20);
        light.setOuterConeAngle(30);
        return lightNode;
    }

    private void setLightColor( GVRSceneObject lightNode, float r, float g, float b)
    {
        GVRPointLight light = (GVRPointLight)lightNode.getLight();
        light.setAmbientIntensity(0.4f * r, 0.4f * g, 0.4f * b, 1);
        light.setDiffuseIntensity(r * 0.5f, g * 0.5f, b * 0.5f, 1);
        light.setSpecularIntensity(r, g, b, 1);
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

        //@todo currently nothing shown in the second pane; the demo needs rework to actually take
        //advantage of a second panel
        mWdgetButtonObject2.setEnable(false);
        mScene.addSceneObject(mWdgetButtonObject2);
    }

}
