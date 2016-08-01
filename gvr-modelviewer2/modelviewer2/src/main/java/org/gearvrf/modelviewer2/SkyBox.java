package org.gearvrf.modelviewer2;


import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class SkyBox {
    GVRSphereSceneObject skyBoxModel;
    String skyBoxName;

    public SkyBox(String skyBoxName) {
        this.skyBoxName = skyBoxName;
    }

    public String getSkyBoxName(){
        return skyBoxName;
    }

    private GVRSphereSceneObject loadSkyBoxModel(GVRContext gvrContext, String skyBoxPath, String skyBoxName) {
        GVRSphereSceneObject sphereObject = null;

        // load texture
        Future<GVRTexture> texture = null;
        try {
            texture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, skyBoxPath + skyBoxName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new GVRSphereSceneObject(gvrContext, false, texture);
        sphereObject.getTransform().setScale(100, 100, 100);
        return sphereObject;
    }

    public GVRSphereSceneObject getSkyBox(GVRContext gvrContext, String skyBoxPath) {
        if (skyBoxModel == null) {
            skyBoxModel = loadSkyBoxModel(gvrContext, skyBoxPath, skyBoxName);
            return skyBoxModel;
        } else {
            return skyBoxModel;
        }
    }

    private GVRSphereSceneObject loadSkyBoxModelFromSD(GVRContext gvrContext, String skyBoxPath, String skyBoxName) {
        GVRSphereSceneObject sphereObject = null;

        // load texture
        Future<GVRTexture> texture = null;
        try {
            texture = gvrContext.loadFutureTexture(new GVRAndroidResource(skyBoxPath + skyBoxName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new GVRSphereSceneObject(gvrContext, false, texture);
        sphereObject.getTransform().setScale(2000, 2000, 2000);
        return sphereObject;
    }

    public GVRSphereSceneObject getSkyBoxFromSD(GVRContext gvrContext, String skyBoxPath) {
        if (skyBoxModel == null) {
            skyBoxModel = loadSkyBoxModelFromSD(gvrContext, skyBoxPath, skyBoxName);
            return skyBoxModel;
        } else {
            return skyBoxModel;
        }
    }


}
