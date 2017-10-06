package org.gearvrf.gvrshadowssample;

import java.io.IOException;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRSpotLight;

import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import android.graphics.Color;
import android.view.MotionEvent;

public class ShadowsMain extends GVRMain {

    private GVRContext mGVRContext = null;
    private GVRSceneObject cubeObject = null;
    private GVRSceneObject rotateObject = null;
    private GVRSceneObject lightObject = null;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getMainScene();
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();

        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.setFarClippingDistance(100.0f);
        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 8.0f);

        GVRSceneObject groundScene = createBackdrop(gvrContext);
        groundScene.getTransform().setRotationByAxis(-80.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(groundScene);

        addSphere(scene, 1.0f, 0, 1.0f, -1.0f);
        addSphere(scene, 2, -4, 2.0f, -2.0f);
        addCube(scene, 2, 6f, 2, -3.0f);
        addStormtrooper(scene, 0, 2.6f, -2.0f);
        lightObject = createSpotLight(gvrContext);
        scene.addSceneObject(lightObject);
    }

    private GVRSceneObject createBackdrop(GVRContext context) throws IOException
    {
        GVRTexture tex = context.getAssetLoader().loadTexture(new GVRAndroidResource(mGVRContext, "floor.jpg"));
        GVRSceneObject backdrop = new GVRSceneObject(context, 100.0f, 100.0f, tex);
        GVRRenderData rdata = backdrop.getRenderData();
        GVRMaterial material = new GVRMaterial(context,new GVRShaderId(GVRPhongShader.class));

        material.setVec4("diffuse_color", 0.8f, 0.8f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.3f, 0.3f, 0.3f, 1.0f);
        material.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        material.setTexture("diffuseTexture", tex);
        backdrop.setName("Backdrop");
        rdata.setMaterial(material);
        return backdrop;
    }

    private GVRSceneObject createDirectLight(GVRContext context)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRDirectLight light = new GVRDirectLight(context);

        light.setCastShadow(true);
        lightNode.attachLight(light);
        light.setShadowRange(1.0f, 150.0f);
        lightNode.getTransform().setRotationByAxis(-70, 1, 0, 0);
        light.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1);
        light.setDiffuseIntensity(1, 1, 1, 1);
        light.setSpecularIntensity(1, 1, 1, 1);
        light.setShadowRange(1f, 150.0f);
        lightNode.setName("DirectLight");
        return lightNode;
    }

    private GVRSceneObject createSpotLight(GVRContext context)
    {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRSpotLight light = new GVRSpotLight(context);

        light.setCastShadow(true);
        lightNode.attachLight(light);
        lightNode.getTransform().setRotationByAxis(-35, 1, 0, 0);
        lightNode.getTransform().setPosition(-4, 7, 10);
        light.setAmbientIntensity(0.3f, 0.3f, 0.3f, 1);
        light.setDiffuseIntensity(1, 1, 1, 1);
        light.setSpecularIntensity(1, 1, 1, 1);
        light.setInnerConeAngle(40);
        light.setOuterConeAngle(55);
        light.setShadowRange(0.2f, 100.0f);
        lightNode.setName("SpotLight");

        return lightNode;
    }

    private float theta = 0.0f;

    @Override
    public void onStep()
    {
       if (rotateObject == null)
            return;
        GVRTransform trans = rotateObject.getTransform();
        float xrot = trans.getRotationPitch();
        float yrot = trans.getRotationYaw();
        float xpos = trans.getPositionX();
        float ypos = trans.getPositionY();
        float zpos = trans.getPositionZ();

        if (yrot < -45.0f)
        {
            yrot = 45.0f;
            if (xrot <= -80.0f)
            {
                xrot = -20.0f;
            }
            else
            {
                xrot -= 2.0f;
            }
        }
        else
        {
            --yrot;
        }
        trans.reset();
        trans.rotateByAxis(xrot, 1, 0, 0);
        trans.rotateByAxis(yrot, 0, 1, 0);
        trans.setPosition(xpos, ypos, zpos);

    }

    public void onTouchEvent(MotionEvent event)
    {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            if (rotateObject == null)
            {
                rotateObject = lightObject;
            }
            else
            {
                rotateObject = null;
            }
            break;

            default:
            break;
        }
    }

    private GVRMaterial createCustomMaterial(GVRContext context, String textureFile) throws IOException
    {
        GVRMaterial litMaterial = new GVRMaterial(context, GVRMaterial.GVRShaderType.Phong.ID);

        litMaterial.setVec4("diffuse_color", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("ambient_color", 0.5f, 0.5f, 0.5f, 0.0f);
        litMaterial.setVec4("specular_color", 1.0f, 1.0f, 1.0f, 1.0f);
        litMaterial.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        litMaterial.setFloat("specular_exponent", 10.0f);

        GVRTexture texture = context.getAssetLoader().loadTexture(new GVRAndroidResource(context, textureFile));
        litMaterial.setTexture("diffuseTexture", texture);
        return litMaterial;
    }

    private void addCube(GVRScene scene, float size, float x, float y, float z) throws IOException
    {
        cubeObject = new GVRCubeSceneObject(mGVRContext, true, createCustomMaterial(mGVRContext, "cube.jpg"));
        cubeObject.getTransform().setPosition(x, y, z);
        cubeObject.getTransform().setScale(size, size, size);
        cubeObject.setName("cube");
        scene.addSceneObject(cubeObject);
    }

    private void addSphere(GVRScene scene, float radius, float x, float y, float z) throws IOException
    {
        GVRSceneObject sphereObject  = new GVRSphereSceneObject(mGVRContext, true, createCustomMaterial(mGVRContext, "sphere.jpg"));

        sphereObject.setName("sphere");
        sphereObject.getTransform().setPosition(x, y, z);
        sphereObject.getTransform().setScale(radius, radius, radius);
        scene.addSceneObject(sphereObject);
    }

    private GVRSceneObject addStormtrooper(GVRScene scene, float x, float y, float z) throws IOException
    {
        GVRSceneObject model = mGVRContext.getAssetLoader().loadModel("storm.obj", scene);
        model.getTransform().setPosition(x, y, z);
        model.getTransform().setScale(1.5f, 1.5f, 1.5f);
        model.getTransform().setRotationByAxis((float) -90, 0, 1, 0);
        return model;
    }

}
