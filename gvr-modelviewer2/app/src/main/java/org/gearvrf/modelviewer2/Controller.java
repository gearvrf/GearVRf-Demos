package org.gearvrf.modelviewer2;

import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.util.AssetsReader;
import org.gearvrf.util.Banner;
import org.gearvrf.util.NoTextureShader;
import org.gearvrf.util.OutlineShader;
import org.gearvrf.widgetplugin.GVRWidgetSceneObject;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.util.ArrayList;

public class Controller {
    private static final String TAG = "GVRModelViewer2";

    // Variables related to SkyBox
    private ArrayList<SkyBox> aODefaultSkyBox;
    private ArrayList<SkyBox> aOSDSkyBox;
    private final String sSDSkyBoxDirectory = "GVRModelViewer2/SkyBox";
    private final String sDefaultSkyBoxDirectory = "skybox";
    private GVRSphereSceneObject currentSkyBox;

    // Variables related to Camera
    private ArrayList<CameraPosition> oDefaultCameraPosition;
    private CameraPosition oCurrentPosition;
    private boolean lookInsideFlag = false;
    private Vector3f lookInsidePosition = new Vector3f(0, 1.75f, 0);

    // Variables related to Model
    private final String sEnvironmentPath = Environment.getExternalStorageDirectory().getPath();
    private ArrayList<Model> aModel;
    private Model currentDisplayedModel;
    public boolean currentModelFlag = false;
    private GVRAnimation currentAnimation;

    // Variables related to Banner
    private Banner oBannerCount;
    private Banner oBannerLoading;

    // Variables related to Custom Shader
    private ArrayList<String> aSCustomShaderList;

    // Lights
    private Lights oLight;
    private boolean oLightFlag = false;

    private Vector3f defaultCenterPosition;

    private GVRActivity activity;
    private GVRContext context;

    public void setDefaultCenterPosition(Vector3f defaultPosition) {
        defaultCenterPosition = new Vector3f(defaultPosition);
    }

    public Controller(GVRActivity activity, GVRContext context) {
        this.activity = activity;
        this.context = context;
    }

    void initializeController() {
        loadDefaultSkyBoxList();
        loadSDSkyBoxList();

        loadModelsList();
        loadCameraPositionList();
        loadCustomShaderList();
        loadLights();
    }

    // START Lights
    private void loadLights() {
        oLight = new Lights();
        oLight.createLight(context);

        //Add white light source
        oLight.addAmbient(0.5f, 0.5f, 0.5f, 0.5f);
        oLight.addDiffuse(0.5f, 0.5f, 0.5f, 0.5f);
        oLight.addSpecular(0.5f, 0.5f, 0.5f, 0.5f);
        
        // Add Ambient
        oLight.addAmbient(0.3f, 0.0f, 0.0f, 0.5f);

        // Add Diffuse
        oLight.addDiffuse(0.3f, 0.0f, 0.0f, 0.5f);

        // Add Specular
        oLight.addSpecular(0.3f, 0.0f, 0.0f, 0.5f);

        // Add Ambient
        oLight.addAmbient(0.0f, 0.0f, 0.5f, 0.5f);

        // Add Diffuse
        oLight.addDiffuse(0.0f, 0.0f, 0.5f, 0.5f);

        // Add Specular
        oLight.addSpecular(0.0f, 0.0f, 0.5f, 0.5f);

        // Add Ambient
        oLight.addAmbient(0.0f, 0.3f, 0.0f, 0.5f);

        // Add Diffuse
        oLight.addDiffuse(0.0f, 0.3f, 0.0f, 0.5f);

        // Add Specular
        oLight.addSpecular(0.0f, 0.3f, 0.0f, 0.5f);
    }

    public ArrayList<String> getAmbient() {
        ArrayList<String> list = new ArrayList<String>();
        for (Vector4f grbaValue : oLight.getAmbient()) {
            String sPosition = "R:" + Float.toString(grbaValue.x) + " G:" + Float.toString
                    (grbaValue.y) + " B:" + Float.toString(grbaValue.z) + " A:" + Float.toString
                    (grbaValue.w);
            list.add(sPosition);
        }
        return list;
    }

    public ArrayList<String> getDiffuse() {
        ArrayList<String> list = new ArrayList<String>();
        for (Vector4f grbaValue : oLight.getDiffuse()) {
            String sPosition = "R:" + Float.toString(grbaValue.x) + " G:" + Float.toString
                    (grbaValue.y) + " B:" + Float.toString(grbaValue.z) + " A:" + Float.toString
                    (grbaValue.w);
            list.add(sPosition);
        }
        return list;
    }

    public ArrayList<String> getSpecular() {
        ArrayList<String> list = new ArrayList<String>();
        for (Vector4f grbaValue : oLight.getSpecular()) {
            String sPosition = "R:" + Float.toString(grbaValue.x) + " G:" + Float.toString
                    (grbaValue.y) + " B:" + Float.toString(grbaValue.z) + " A:" + Float.toString
                    (grbaValue.w);
            list.add(sPosition);
        }
        return list;
    }

    public void setAmbient(int index) {
        oLight.setAmbient(index);
    }

    public void setDiffuse(int index) {
        oLight.setDiffuse(index);
    }

    public void setSpecular(int index) {
        oLight.setSpecular(index);
    }

    public void addLight(GVRScene scene) {
        oLight.getLightScene().getTransform().setPosition(0, 10, 0);
        oLight.getLightScene().getTransform().rotateByAxis(-90, 1, 0, 0);
        scene.addSceneObject(oLight.getLightScene());
        scene.bindShaders();
    }

    public void enableDisableLightOnModel(GVRSceneObject model, boolean flag) {
        ArrayList<GVRRenderData> rdata = model.getAllComponents(GVRRenderData.getComponentType());
        for (GVRRenderData r : rdata) {

            if (r != null) {
                if (flag) {
                    r.enableLight();
                } else {
                    r.disableLight();
                }
            }
        }
    }

    public void turnOnOffLight(boolean flag) {
        if (flag) {
            oLight.setSelected(0);
            oLightFlag = true;

            if (currentDisplayedModel != null) {
                enableDisableLightOnModel(currentDisplayedModel.getModel(context), true);
            }
        } else {
            oLightFlag = false;

            if (currentDisplayedModel != null) {
                enableDisableLightOnModel(currentDisplayedModel.getModel(context), false);
            }
        }
    }
    // END Lights

    // START Custom Shader Feature
    private void loadCustomShaderList() {
        aSCustomShaderList = new ArrayList<String>();
        aSCustomShaderList.add("Original");
        aSCustomShaderList.add("No Texture");
        aSCustomShaderList.add("Outline");
        aSCustomShaderList.add("Lines");
        aSCustomShaderList.add("Lines_Loop");
        aSCustomShaderList.add("Points");
    }

    public ArrayList<String> getListOfCustomShaders() {
        return aSCustomShaderList;
    }

    public void applyCustomShader(int index, GVRScene scene) {
        if (currentDisplayedModel == null)
            return;
        ArrayList<GVRRenderData> renderDatas = currentDisplayedModel.getModel(context)
                .getAllComponents(GVRRenderData.getComponentType());
        GVRMaterial outlineMaterial = new GVRMaterial(context);

        switch (index) {
            case 0:
                for (int i = 0; i < renderDatas.size(); i++) {
                    renderDatas.get(i).setMaterial(currentDisplayedModel.originalMaterial.get(i));
                    renderDatas.get(i).setShaderTemplate(GVRPhongShader.class);
                    renderDatas.get(i).setCullFace(GVRRenderPass.GVRCullFaceEnum.Back);
                    renderDatas.get(i).setDrawMode(4);
                    scene.bindShaders();
                    enableDisableLightOnModel(currentDisplayedModel.getModel(context), false);
                }
                break;
            case 1:
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setShaderTemplate(NoTextureShader.class);
                    rdata.setDrawMode(4);
                }
                break;

            case 2:
                outlineMaterial.setVec4(OutlineShader.COLOR_KEY, 0.4f, 0.1725f, 0.1725f, 1.0f);
                outlineMaterial.setFloat(OutlineShader.THICKNESS_KEY, 2.0f);
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setMaterial(outlineMaterial);
                    rdata.setShaderTemplate(OutlineShader.class);
                    rdata.setCullFace(GVRRenderPass.GVRCullFaceEnum.Front);
                    rdata.setDrawMode(4);
                }
                break;
            case 3:
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setShaderTemplate(GVRPhongShader.class);
                    rdata.setDrawMode(1);
                }

                break;
            case 4:
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setShaderTemplate(GVRPhongShader.class);
                    rdata.setDrawMode(3);
                }

                break;
            case 5:
                for (GVRRenderData rdata : renderDatas) {
                    rdata.setShaderTemplate(GVRPhongShader.class);
                    rdata.setDrawMode(0);
                }

                break;
        }

        scene.bindShaders();
    }
    // END Custom Shader Feature

    // START Banner Feature
    void displayCountInRoom(GVRScene room) {
        if (oBannerCount == null) {
            oBannerCount = new Banner(context, "Total Models " + String.valueOf(aModel.size()),
                    10, Color.BLUE, defaultCenterPosition.x - 2, defaultCenterPosition.y + 5,
                    defaultCenterPosition.z);
        }
        room.addSceneObject(oBannerCount.getBanner());
    }

    void displayLoadingInRoom(GVRScene room) {
        if (oBannerLoading == null) {
            oBannerLoading = new Banner(context, "Loading", 10, Color.BLUE, defaultCenterPosition
                    .x, defaultCenterPosition.y, defaultCenterPosition.z);
        }
        room.addSceneObject(oBannerLoading.getBanner());
    }

    void removeLoadingInRoom(GVRScene room) {
        if (oBannerLoading == null) {
            return;
        }
        room.removeSceneObject(oBannerLoading.getBanner());
    }
    // END Banner Feature

    // START Camera Position Feature
    private void loadCameraPositionList() {
        oDefaultCameraPosition = new ArrayList<CameraPosition>();

        int offset = 15;
        // User Position Or Front
        oDefaultCameraPosition.add(new CameraPosition(defaultCenterPosition.x,
                defaultCenterPosition.y + 5, defaultCenterPosition.z + offset, 0, 0, 0, 0));

        // Top
        oDefaultCameraPosition.add(new CameraPosition(defaultCenterPosition.x,
                defaultCenterPosition.y + offset, defaultCenterPosition.z, -90, 1, 0, 0));

        // Bottom
        oDefaultCameraPosition.add(new CameraPosition(defaultCenterPosition.x,
                defaultCenterPosition.y - offset, defaultCenterPosition.z, 90, 1, 0, 0));

        // Back
        oDefaultCameraPosition.add(new CameraPosition(defaultCenterPosition.x,
                defaultCenterPosition.y + 5, defaultCenterPosition.z - offset, 180, 0, 1, 0));

        // Left
        oDefaultCameraPosition.add(new CameraPosition(defaultCenterPosition.x - offset,
                defaultCenterPosition.y + 5, defaultCenterPosition.z, -90, 0, 1, 0));

        // Right
        oDefaultCameraPosition.add(new CameraPosition(defaultCenterPosition.x + offset,
                defaultCenterPosition.y + 5, defaultCenterPosition.z, 90, 0, 1, 0));
    }

    public ArrayList<String> getCameraPositionList() {
        ArrayList<String> list = new ArrayList<String>();
        for (CameraPosition position : oDefaultCameraPosition) {
            Vector3f coordinate = position.getCameraPosition();
            String sPosition = "X:" + Float.toString(coordinate.x) + " Y:" + Float.toString
                    (coordinate.y) + " Z:" + Float.toString(coordinate.z);
            list.add(sPosition);
        }
        return list;
    }

    public void displayNavigators(GVRScene room) {
        oCurrentPosition = oDefaultCameraPosition.get(0);
        oDefaultCameraPosition.get(0).loadNavigator(context);

        for (int i = 1; i < oDefaultCameraPosition.size(); i++) {
            GVRModelSceneObject temp = oDefaultCameraPosition.get(i).loadNavigator(context);
            room.addSceneObject(temp);
        }
    }

    protected void lookAt(GVRTransform modeltransform, GVRTransform camera, GVRModelSceneObject
            mCharacter) {
        Vector3f cameraV = new Vector3f(camera.getPositionX(), camera.getPositionY(), camera
                .getPositionZ());

        Vector3f modeltransformV = new Vector3f(modeltransform.getPositionX(), modeltransform
                .getPositionY(), modeltransform.getPositionZ());

        Vector3f delta = cameraV.sub(modeltransformV);
        Vector3f direction = delta.normalize();

        Vector3f up;

        if (Math.abs(direction.x) < 0.00001
                && Math.abs(direction.z) < 0.00001) {
            if (direction.y > 0) {
                up = new Vector3f(0.0f, 0.0f, -1.0f); // if direction points in +y
            } else {
                up = new Vector3f(0.0f, 0.0f, 1.0f); // if direction points in -y
            }
        } else {
            up = new Vector3f(0.0f, 1.0f, 0.0f); // y-axis is the general up
        }

        up.normalize();
        Vector3f right = new Vector3f();
        up.cross(direction, right);
        right.normalize();
        direction.cross(right, up);
        up.normalize();

        float[] matrix = new float[]{right.x, right.y, right.z, 0.0f, up.x, up.y,
                up.z, 0.0f, direction.x, direction.y, direction.z, 0.0f,
                modeltransform.getPositionX(), modeltransform.getPositionY(), modeltransform
                .getPositionZ(), 0.0f};
        mCharacter.getTransform().setModelMatrix(matrix);
    }

    public void setCameraPositionByNavigator(GVRCollider picked, GVRScene scene, GVRScene
            room, GVRWidgetSceneObject widget, float original[]) {
        CameraPosition campos = null;
        int camIndex = 0;
        if (picked != null) {
            for (camIndex = 0; camIndex < oDefaultCameraPosition.size(); camIndex++) {
                CameraPosition cp = oDefaultCameraPosition.get(camIndex);
                if (picked.equals(cp.cameraModel.getCollider()))
                {
                    campos = cp;
                    break;
                }
            }
            if (campos == null) {
                return;
            }
        }
        else {
            campos = oDefaultCameraPosition.get(0);
        }

        // START Code to Attach Menu According to Camera Position
        scene.removeSceneObject(widget);
        widget.getTransform().setModelMatrix(original);
        campos.cameraModel.addChildObject(widget);
        Vector3f axis = campos.getRotationAxis();
        campos.cameraModel.getTransform().setRotationByAxis(campos.getCameraAngle(), axis.x, axis.y, axis.z);

        float temp[] = widget.getTransform().getModelMatrix();
        widget.getTransform().setModelMatrix(temp);
        campos.cameraModel.removeChildObject(widget);
        scene.addSceneObject(widget);

        // END Code to Attach Menu According to Camera Position

        Vector3f coordinates = campos.getCameraPosition();
        scene.getMainCameraRig().getTransform().setPosition(coordinates.x, coordinates.y,
                coordinates.z);

        axis = campos.getRotationAxis();
        scene.getMainCameraRig().getTransform().setRotationByAxis(campos.getCameraAngle(), axis.x, axis.y, axis.z);

        if (oCurrentPosition != null) {
            room.addSceneObject(oCurrentPosition.loadNavigator(context));
        }

        Log.i(TAG, "Removing navigator " + Integer.toString(camIndex));
        room.removeSceneObject(campos.cameraModel);
        oCurrentPosition = campos;

        for (int j = 0; j < oDefaultCameraPosition.size(); j++) {
            if (j != camIndex)
                lookAt(oDefaultCameraPosition.get(j).cameraModel.getTransform(), scene
                        .getMainCameraRig().getTransform(), oDefaultCameraPosition.get(j)
                        .cameraModel);
        }
    }

    public void lookInside(GVRScene scene, boolean flag) {
        if (flag && (currentDisplayedModel != null)) {
            lookInsideFlag = true;
            scene.getMainCameraRig().getTransform().setPosition(lookInsidePosition.x,
                    lookInsidePosition.y, lookInsidePosition.z);
        }
    }

    public void checkLookInside(GVRScene scene) {
        Log.i("", "Check look insdie");
        if (lookInsideFlag) {
            Vector3f coord = oCurrentPosition.getCameraPosition();
            Log.i("", "True Check look insdie" + Float.toString(coord.x) + Float.toString(coord
                    .y) + Float.toString(coord.z));
            scene.getMainCameraRig().getTransform().setPosition(coord.x, coord.y, coord.z);
            lookInsideFlag = false;
        }
    }
    // END Camera Position Feature

    // START Look Inside

    // START Models Features

    public int getCountOfAnimations() {
        if (currentDisplayedModel != null)
            return currentDisplayedModel.getAnimationsList().size();
        return 0;
    }

    public void setSelectedAnimation(int index) {
        // No Animation
        if (index == 0) {
            if (currentAnimation != null) {
                context.getAnimationEngine().stop(currentAnimation);
                currentAnimation = null;
            }
        } else {
            index -= 1;
            if (currentDisplayedModel != null && currentDisplayedModel.getAnimationsList().size()
                    > 0)
                currentAnimation = currentDisplayedModel.getAnimationsList().get(index);
            currentAnimation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
            context.getAnimationEngine().start(currentAnimation);
        }
    }

    private ArrayList<File> getListOfModels() {
        ArrayList<File> listOfAllModels = new ArrayList<File>();

        // Add All the Extensions you want to load
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add("fbx");
        extensions.add("FBX");
        extensions.add("3ds");
        extensions.add("dae");
        extensions.add("obj");
        extensions.add("ma");
        extensions.add("x3d");

        // Reads the List of Files from specified folder having extension specified in extensions.
        // Please place your models by creating GVRModelViewer2 folder in your internal phone memory
        CardReader cRObject = new CardReader(sEnvironmentPath + "/GVRModelViewer2", extensions);
        File list[] = cRObject.getModels();

        if (list == null)
            return listOfAllModels;

        // Adds all the models
        for (File file : list) {
            Log.i("path", file.getPath());

            listOfAllModels.add(file);
        }

        return listOfAllModels;
    }

    void loadModelsList() {
        aModel = new ArrayList<Model>();
        ArrayList<File> listOfAllModels = getListOfModels();
        for (File file : listOfAllModels) {
            Model tempModel = new Model(file.getName(), file.getPath().replaceAll
                    ("/storage/emulated/0/", ""));
            aModel.add(tempModel);
        }
    }

    ArrayList<String> getModelsList() {
        ArrayList<String> listOfModels = new ArrayList<String>();

        for (Model m : aModel)
            listOfModels.add(m.getModelName());

        return listOfModels;
    }

    void setModelWithIndex(int index, GVRScene room) {
        if (currentDisplayedModel != null) {
            room.removeSceneObject(currentDisplayedModel.getModel(context));
        }

        displayLoadingInRoom(room);
        GVRSceneObject tempModelSO = aModel.get(index).getModel(context);

        Log.d(TAG, "Loading Done");
        if (tempModelSO != null) {
            tempModelSO.getTransform().setPosition(defaultCenterPosition.x, defaultCenterPosition
                    .y, defaultCenterPosition.z);
            room.addSceneObject(tempModelSO);
            room.bindShaders();
            enableDisableLightOnModel(tempModelSO, oLightFlag);

            removeLoadingInRoom(room);
            Log.d(TAG, "Loading Done");
            currentDisplayedModel = aModel.get(index);
            currentModelFlag = true;
        } else {
            Log.d(TAG, "Loading Error");
        }
        removeLoadingInRoom(room);
    }

    void onScrollOverModel(GVRCollider holder, float scrollValue) {
        GVRAnimation animation = null;

        if (currentDisplayedModel.getModel(context).getCollider() == holder) {
                Log.d(TAG, "Angle mover applied");
                if (scrollValue > 0)
                    animation = new GVRRotationByAxisAnimation(currentDisplayedModel.getModel
                            (context), 0.1f, 35, 0, 1, 0).start(context.getAnimationEngine());
                else
                    animation = new GVRRotationByAxisAnimation(currentDisplayedModel.getModel
                            (context), 0.1f, 35, 0, -1, 0).start(context.getAnimationEngine());
        }
    }

    void onZoomOverModel(float zoomBy) {
        float zTransform = (int) ((zoomBy) / (10));
        Log.d(TAG, "Zoom by" + Float.toString(zTransform) + "  " + Float.toString(zoomBy));
        if (currentDisplayedModel != null) {
            float units = currentDisplayedModel.getCurrentZoom();
            if (units < zTransform) {
                float scaleFactor = zTransform - units;
                float sf = 1.1f;
                for (int i = 0; i < scaleFactor; i++) {
                    float x = currentDisplayedModel.getModel(context).getTransform().getScaleX();
                    float y = currentDisplayedModel.getModel(context).getTransform().getScaleY();
                    float z = currentDisplayedModel.getModel(context).getTransform().getScaleZ();
                    currentDisplayedModel.getModel(context).getTransform().setScale(sf * x, sf *
                            y, sf * z);
                }
            } else {
                float scaleFactor = units - zTransform;
                float sf = 0.9f;
                for (int i = 0; i < scaleFactor; i++) {
                    float x = currentDisplayedModel.getModel(context).getTransform().getScaleX();
                    float y = currentDisplayedModel.getModel(context).getTransform().getScaleY();
                    float z = currentDisplayedModel.getModel(context).getTransform().getScaleZ();
                    currentDisplayedModel.getModel(context).getTransform().setScale(sf * x, sf *
                            y, sf * z);
                }
            }
            currentDisplayedModel.setCurrentZoom(zTransform);
        }
    }
    // END Models Features

    // START SkyBox Features
    void loadSDSkyBoxList() {
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add("png");

        CardReader cRObject = new CardReader(sEnvironmentPath + "/" + sSDSkyBoxDirectory + "/",
                extensions);
        File list[] = cRObject.getModels();

        aOSDSkyBox = new ArrayList<SkyBox>();

        if (list != null)
            for (File sSkyBoxName : list) {
                aOSDSkyBox.add(new SkyBox(sSkyBoxName.getName()));
            }
    }

    void loadDefaultSkyBoxList() {
        String[] aSSkyBox = AssetsReader.getAssetsList(activity, sDefaultSkyBoxDirectory);
        aODefaultSkyBox = new ArrayList<SkyBox>();

        for (String sSkyBoxName : aSSkyBox) {
            aODefaultSkyBox.add(new SkyBox(sSkyBoxName));
        }
    }

    ArrayList<String> getSkyBoxList() {
        ArrayList<String> sAEntireList = new ArrayList<String>();

        // Default SkyBox List
        for (SkyBox oSkyBox : aODefaultSkyBox) {
            sAEntireList.add(oSkyBox.getSkyBoxName());
        }

        // SDCard SkyBox List
        if (aOSDSkyBox != null)
            for (SkyBox oSkyBox : aOSDSkyBox) {
                sAEntireList.add(oSkyBox.getSkyBoxName());
            }

        return sAEntireList;
    }

    void addSkyBox(int index, GVRScene scene) {
        Log.d(TAG, "Adding SkyBox");
        GVRSphereSceneObject current = null;
        if (currentSkyBox != null)
            scene.removeSceneObject(currentSkyBox);

        int count = aODefaultSkyBox.size();
        if (index < count) {
            current = aODefaultSkyBox.get(index).getSkyBox(context, sDefaultSkyBoxDirectory + "/");
        } else {
            current = aOSDSkyBox.get(index - count).getSkyBoxFromSD(context, sEnvironmentPath +
                    "/" + sSDSkyBoxDirectory + "/");
        }

        if (current != null) {
            scene.addSceneObject(current);
            currentSkyBox = current;
            current.getTransform().setPosition(defaultCenterPosition.x, defaultCenterPosition.y,
                    defaultCenterPosition.z);
        } else {
            Log.d(TAG, "SkyBox is null");
        }
    }

    // END SkyBox Features
}
