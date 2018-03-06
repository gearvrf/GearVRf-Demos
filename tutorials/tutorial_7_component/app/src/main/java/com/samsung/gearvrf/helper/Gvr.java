package com.samsung.gearvrf.helper;

import android.graphics.Color;

import com.example.org.gvrfapplication.R;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IEvents;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Gvr {
    private static GVRContext s_Context = null;
    private static String TAG = "GVR Helper";

    public static void init(GVRContext context){
        s_Context = context;
    }

    /***************************************
     * Scene Objects
     ***************************************/

    public static GVRCubeSceneObject createCube(){
        if (s_Context == null) {
            Log.e(TAG, "GVRContext is not initialized");
            return null;
        }

        GVRCubeSceneObject cube = new GVRCubeSceneObject(s_Context);
        cube.getRenderData().getMaterial().setColor(Color.WHITE);
        return cube;
    }

    public static GVRSceneObject createMesh(int meshID, int textureID) {
        if (s_Context == null) {
            Log.e(TAG, "GVRContext is not initialized");
            return null;
        }

        GVRMesh mesh = s_Context.getAssetLoader().loadMesh(new GVRAndroidResource(s_Context, meshID));
        GVRTexture texture = s_Context.getAssetLoader().loadTexture(new GVRAndroidResource(s_Context, textureID));
        GVRSceneObject sceneObject = new GVRSceneObject(s_Context, mesh, texture);

        return sceneObject;
    }

    public static GVRSceneObject createQuad(float width, float height, int textureID){
        GVRSceneObject quad = new GVRSceneObject(s_Context,
                s_Context.createQuad(width, height),
                s_Context.getAssetLoader().loadTexture(new GVRAndroidResource(s_Context, textureID)));

        return quad;
    }

    /***************************************
     * Utils
     ***************************************/

    static Matrix4f reverseMatrix(GVRSceneObject object, Matrix4f worldMat){
        Matrix4f mat = new Matrix4f();
        Matrix4f newMat = new Matrix4f(worldMat);

        object.getTransform().getLocalModelMatrix4f().invert(mat);
        newMat.mul(mat);

        return newMat;
    }

    public static Vector3f getWorldDirection(GVRSceneObject object) {
        Matrix4f tmp = object.getTransform().getModelMatrix4f();

        Vector3f dir = new Vector3f(-tmp.m20(), -tmp.m21(), -tmp.m22());

        return dir;
    }

    public static Quaternionf getWorldRotation(GVRSceneObject object) {
        Matrix4f tmp = object.getTransform().getModelMatrix4f();

        Quaternionf rot = new Quaternionf();
        tmp.getNormalizedRotation(rot);

        return rot;
    }

    public static Vector3f getWorldPosition(GVRSceneObject object) {
        Matrix4f tmp = object.getTransform().getModelMatrix4f();

        Vector3f pos = tmp.getTranslation(new Vector3f());

        return pos;
    }
}
