package org.gearvrf.modelviewer2;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.util.BoundingBoxCreator;
import org.joml.Vector3f;

import java.io.IOException;

public class CameraPosition {
    private Vector3f position = new Vector3f();
    GVRModelSceneObject cameraModel;
    private float angle;
    private Vector3f axis = new Vector3f();

    public CameraPosition(float defaultX, float defaultY, float defaultZ, float angleIn, float axisX, float axisY, float axisZ) {
        position = (new Vector3f(defaultX, defaultY, defaultZ));
        angle = angleIn;
        axis = (new Vector3f(axisX, axisY, axisZ));
    }

    public Vector3f getCameraPosition() {
        return position;
    }

    public float getCameraAngle() {
        return angle;
    }

    public Vector3f getRotationAxis() {
        return axis;
    }

    public GVRModelSceneObject loadNavigator(GVRContext context) {
        if (cameraModel != null)
            return cameraModel;

        try {
            cameraModel = context.getAssetLoader().loadModel("camera_icon.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        attachEyePointee(context);
        cameraModel.getTransform().setPosition(position.x, position.y, position.z);
        return cameraModel;
    }

    private void attachEyePointee(GVRContext context) {
        GVRSphereCollider sphereCollider = new GVRSphereCollider(context);
        sphereCollider.setRadius(0.5f);
        cameraModel.attachComponent(sphereCollider);
    }
}
