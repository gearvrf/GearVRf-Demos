package org.gearvrf.gvrshadowssample;

import java.io.IOException;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSpotLight;

import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.joml.Quaternionf;

import android.graphics.Color;
import android.util.Log;

public class ShadowsMain extends GVRMain {

    private GVRContext mGVRContext = null;

    int countTime = 0;
    GVRSceneObject cubeObject = null;


    private GVRSceneObject stormtrooper;
    private static final float CUBE_WIDTH = 20.0f;

    GVRSceneObject rotateObject = null;
    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;

         GVRScene scene = mGVRContext.getNextMainScene();
//        mAnimationEngine = gvrContext.getAnimationEngine();
//
//        GVRScene scene = gvrContext.getNextMainScene(new Runnable() {
//            @Override
//            public void run() {
//                mAssimpAnimation.start(mAnimationEngine);
//            }
//        });

        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);

        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 8.0f);

                                                     
        GVRSceneObject light1= createLight(gvrContext);
        
        /*
         * Create the ground. A simple textured quad. In bullet it will be a
         * plane shape with 0 mass
         */        
        GVRSceneObject groundScene = createBackdrop(gvrContext);
        groundScene.getTransform().setRotationByAxis(-80.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(groundScene);

         addSphere(scene, 1.0f, 0, 1.0f, -1.0f);
        //
        // addSphere(scene, 1.0f, -2, 1.0f, -2.0f);
        // addSphere(scene, 1.0f, -2, 1.0f, 2.0f);
        // addSphere(scene, 1.0f, 2, 1.0f, -2.0f);
        // addSphere(scene, 1.0f, 2, 1.0f, 2.0f);
        //
    //     addSphere(scene, 1.0f, -2, 2.0f, -2.0f);
        // addSphere(scene, 1.0f, -2, 2.0f, 2.0f);
        // addSphere(scene, 1.0f, 2, 2.0f, -2.0f);
        // addSphere(scene, 1.0f, 2, 2.0f, 2.0f);
    //    //
        addSphere(scene, 1.0f, -4, 2.0f, -2.0f);
        // addSphere(scene, 1.0f, -6, 2.0f, -6.0f);
        // addSphere(scene, 1.0f, -8, 2.0f, -8.0f);
        //
         addCube(scene, 2, 6f, 2, -3.0f);
        setupShader(scene,cubeObject.getRenderData() );
        stormtrooper = addStormtrooper(scene, 0, 2.6f, -2.0f);
        
        light1.getTransform().setRotationByAxis(-35, 1, 0, 0);
         light1.getTransform().setPosition(4.0f, 7, 10);
      
         GVRSceneObject light2 = createPointLight(gvrContext, 0, 1, 0, 5.0f);
      //   scene.addSceneObject(light2);
         scene.addSceneObject(light1);
        rotateObject = light1;

    }
    private GVRSceneObject createBackdrop(GVRContext context) throws IOException
    {
        GVRTexture tex = context.loadTexture(new GVRAndroidResource(mGVRContext, "floor.jpg"));
        GVRSceneObject backdrop = new GVRSceneObject(context, 100.0f, 100.0f, tex);
        GVRRenderData rdata = backdrop.getRenderData();
        GVRMaterial material = new GVRMaterial(context);
        
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
        backdrop.setName("Backdrop");
        rdata.setMaterial(material);
        rdata.setShaderTemplate(GVRPhongShader.class);
        return backdrop;
    }
    
    private GVRSceneObject createPointLight(GVRContext context, float r, float g, float b, float y)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRPointLight light = new GVRPointLight(context);
        Quaternionf q = new Quaternionf();
        
        lightNode.attachLight(light);         
        lightNode.getTransform().setPosition(0, y, 3);
        light.setAmbientIntensity(0.3f * r, 0.3f * g, 0.3f * b, 1);
        light.setDiffuseIntensity(r, g, b, 1);
        light.setSpecularIntensity(r, g, b, 1);
        lightNode.setName("RedLight");
        return lightNode;
    }
    
    private GVRSceneObject createLight(GVRContext context)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRDirectLight light = new GVRDirectLight(context);
      
        // GVRSpotLight light= new GVRSpotLight(context);
        Quaternionf q = new Quaternionf();
        light.setCastShadow(true);
        lightNode.attachLight(light);         
       // lightNode.getTransform().setPosition(-0.4f, 0, 3);
        light.setAmbientIntensity(0.3f * 1, 0.3f * 1, 0, 1);
        light.setDiffuseIntensity(0, 1, 1, 1);
        light.setSpecularIntensity(0, 1,1, 1);
    //    light.setInnerConeAngle(8);
    //    light.setOuterConeAngle(12);
    //    lightNode.setName("RedLight");
        return lightNode;
    }
    long lasttime = System.currentTimeMillis();;
    int countFrame;
    int framerate;
    private float theta =0.0f;
    @Override
    public void onStep() {
        
        theta -= 0.01;
       
        if(theta <= -8.0f)
            theta = 0.0f;
        double sine = Math.cos(theta);
        double cosine = Math.sin(theta);
        float z = -CUBE_WIDTH * 0.25f;
        if (rotateObject != null) {
            GVRTransform trans = rotateObject.getTransform();
           // trans.setPosition((float) sine * 10, 0, (float) cosine * 10 + z);
          //  trans.translate(theta, 0, 0);
            trans.setPosition(4.0f + theta, 7, 10);
           // trans.setPosition(-3.0f, theta, 5);
        }

    }

    private GVRSceneObject meshWithTexture(String mesh, String texture) {
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, new GVRAndroidResource(
                    mGVRContext, mesh), new GVRAndroidResource(mGVRContext, texture));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }
    private GVRMaterial createCustomMaterial(GVRContext context) throws Exception
    {
        Future<GVRTexture> texture = context.loadFutureTexture(new GVRAndroidResource(context, "cube.jpg"));
        GVRMaterial litMaterial = new GVRMaterial(context);
        litMaterial.setVec4("diffuseColor", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("ambientColor", 0.5f, 0.5f, 0.5f, 0.0f);
        litMaterial.setVec4("specularColor", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("emissiveColor", 0.0f, 0.0f, 0.0f, 0.0f);
        litMaterial.setFloat("specularExponent", 10.0f);
        litMaterial.setTexture("diffuseTexture", texture);
        return litMaterial;
    }
    private void setupShader(GVRScene scene, GVRRenderData rdata) throws Exception
    {
        GVRContext context = scene.getGVRContext();
        GVRMaterial litMaterial = createCustomMaterial(context);
        
        rdata.setMaterial(litMaterial);
        rdata.setShaderTemplate(GVRPhongShader.class);
    }
    private void addCube(GVRScene scene, float size, float x, float y, float z) {
        cubeObject = meshWithTexture("cube.obj", "cube.jpg");
        cubeObject.getTransform().setPosition(x, y, z);
        cubeObject.getTransform().setScale(size, size, size);
        cubeObject.setName("cube");
        scene.addSceneObject(cubeObject);
    }

    private void addSphere(GVRScene scene, float radius, float x, float y,
            float z) throws IOException {

        GVRSceneObject sphereObject  = meshWithTexture("sphere.obj",
                "sphere.jpg");
       
        Future<GVRTexture> tex = mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "sphere.jpg"));
        
        GVRRenderData rdata = sphereObject.getRenderData();
        GVRMaterial material = new GVRMaterial(mGVRContext);
        
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
        sphereObject.setName("sphere");
   
        rdata.setMaterial(material);
        rdata.enableLight();

        sphereObject.getTransform().setPosition(x, y, z);
        sphereObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
        scene.addSceneObject(sphereObject);
    }

    private GVRSceneObject addStormtrooper(GVRScene scene, float x, float y, float z) throws IOException {
        GVRTexture tex = mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, "stormstrooper.jpg"));
        GVRSceneObject object = meshWithTexture("storm.obj", "stormstrooper.jpg");
        object.getTransform().setPosition(x, y, z);
        object.getTransform().setScale(1.5f, 1.5f, 1.5f);
        object.getTransform().setRotationByAxis((float) -90, 0, 1, 0);
        GVRRenderData rdata = object.getRenderData();
        GVRMaterial material = new GVRMaterial(mGVRContext);
        
        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
       
        rdata.setMaterial(material);
        object.getRenderData().setShaderTemplate(GVRPhongShader.class);
        scene.addSceneObject(object);
    
        return object;
    }

}
