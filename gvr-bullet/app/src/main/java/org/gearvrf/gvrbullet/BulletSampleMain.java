package org.gearvrf.gvrbullet;

import android.graphics.Color;
import android.util.Log;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;

import java.io.IOException;

public class BulletSampleMain extends GVRMain {

    private GVRContext mGVRContext = null;

    private GVRRigidBody mSphereRigidBody = null;

    private static final float CUBE_MASS = 0.5f;
    private static final float BALL_MASS = 2.5f;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;
        final GVRScene scene = mGVRContext.getNextMainScene();


        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);

        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 0.0f);

        addGroundMesh(scene, 0.0f, 0.4f, 0.0f, 0.0f);

        /*
         * Create Some cubes in Bullet world and hit it with a sphere
         */
        addCube(scene, 0.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 1.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 1.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 1.0f, -11.0f, CUBE_MASS);

        addCube(scene, 0.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 2.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 2.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 2.0f, -11.0f, CUBE_MASS);

        addCube(scene, 0.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 0.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 0.0f, 3.0f, -11.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 1.0f, 3.0f, -11.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -9.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -10.0f, CUBE_MASS);
        addCube(scene, 2.0f, 3.0f, -11.0f, CUBE_MASS);

        /*
         * Throw a sphere from top
         */
        addSphere(scene, 1.0f, 1.5f, 40.0f, -10.0f, BALL_MASS);

        scene.getRoot().attachComponent(new GVRWorld(gvrContext));
    }

    public void touchEvent() {
        mSphereRigidBody.applyCentralForce(-20.0f, 500.0f, 0.0f);
        mSphereRigidBody.applyTorque(5.0f, 0.5f, 0.0f);
    }

    @Override
    public void onStep() {

    }

    private GVRSceneObject quadWithTexture(float width, float height,
            String texture) {
        // TODO: Check about future mesh to bullet
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                mGVRContext.createQuad(width, height));
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, futureMesh,
                    mGVRContext.loadFutureTexture(new GVRAndroidResource(
                            mGVRContext, texture)));
            // TODO: Create mesh collider to ground and add GVRCollision component
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private GVRSceneObject meshWithTexture(String mesh, String texture) {
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, new GVRAndroidResource(
                    mGVRContext, mesh), new GVRAndroidResource(mGVRContext,
                    texture));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void addGroundMesh(GVRScene scene, float x, float y, float z, float mass) {
        try {
            GVRMesh mesh = mGVRContext.createQuad(100.0f, 100.0f);
            GVRTexture texture =
                    mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, "floor.jpg"));
            GVRSceneObject meshObject = new GVRSceneObject(mGVRContext, mesh, texture);

            meshObject.getTransform().setPosition(x, y, z);
            meshObject.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

            // Collider
            GVRMeshCollider meshCollider = new GVRMeshCollider(mGVRContext, mesh);
            meshObject.attachCollider(meshCollider);

            // Physics body
            GVRRigidBody body = new GVRRigidBody(mGVRContext);

            meshObject.attachComponent(body);


            body.setRestitution(0.5f);
            body.setFriction(1.0f);

            scene.addSceneObject(meshObject);
        } catch (IOException exception) {
            Log.d("gvrf", exception.toString());
        }
    }

    /*
     * Function to add a cube of unit size with mass at the specified position
     * in Bullet physics world and scene graph.
     */
    private void addCube(GVRScene scene, float x, float y, float z, float mass) {

        GVRSceneObject cubeObject = meshWithTexture("cube.obj", "cube.jpg");
        cubeObject.getTransform().setPosition(x, y, z);

        // Collider
        GVRBoxCollider boxCollider = new GVRBoxCollider(mGVRContext);
        boxCollider.setHalfExtents(0.5f, 0.5f, 0.5f);
        cubeObject.attachCollider(boxCollider);

        // Physics body
        GVRRigidBody body = new GVRRigidBody(mGVRContext);

        cubeObject.attachComponent(body);

        body.setMass(mass);
        body.setRestitution(0.5f);
        body.setFriction(1.0f);

        scene.addSceneObject(cubeObject);
    }

    /*
     * Function to add a sphere of dimension and position specified in the
     * Bullet physics world and scene graph
     */
    private void addSphere(GVRScene scene, float radius, float x, float y,
            float z, float mass) {

        GVRSceneObject sphereObject = meshWithTexture("sphere.obj",
                "sphere.jpg");
        sphereObject.getTransform().setPosition(x, y, z);

        // Collider
        GVRSphereCollider sphereCollider = new GVRSphereCollider(mGVRContext);
        sphereCollider.setRadius(1.0f);
        sphereObject.attachCollider(sphereCollider);

        // Physics body
        mSphereRigidBody = new GVRRigidBody(mGVRContext);

        sphereObject.attachComponent(mSphereRigidBody);

        mSphereRigidBody.setMass(mass);
        mSphereRigidBody.setRestitution(1.5f);
        mSphereRigidBody.setFriction(0.5f);

        scene.addSceneObject(sphereObject);
    }

}
