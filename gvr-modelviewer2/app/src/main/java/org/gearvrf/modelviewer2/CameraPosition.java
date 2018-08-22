package org.gearvrf.modelviewer2;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.util.BoundingBoxCreator;
import org.joml.Vector3f;

import java.io.IOException;

public class CameraPosition {
    private Vector3f position = new Vector3f();
    GVRSceneObject cameraModel;
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

    public GVRSceneObject loadNavigator(GVRContext context) {
        if (cameraModel != null)
            return cameraModel;

        try {
            cameraModel = context.getAssetLoader().loadModel("camera_icon.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        attachCollider(context);
        cameraModel.getTransform().setPosition(position.x, position.y, position.z);
        return cameraModel;
    }

    private void attachCollider(GVRContext context) {
        GVRSphereCollider sphereCollider = new GVRSphereCollider(context);
        sphereCollider.setRadius(0.5f);
        cameraModel.attachComponent(sphereCollider);
    }
}
