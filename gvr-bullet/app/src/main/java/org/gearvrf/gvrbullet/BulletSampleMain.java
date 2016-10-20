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
import org.gearvrf.physics.ICollisionEvents;

import java.io.IOException;

public class BulletSampleMain extends GVRMain {

    public class CollisionHandler implements ICollisionEvents {
        private GVRTexture blueObject;

        CollisionHandler() {
            try {
                blueObject = mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, "sphereblue.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void swapTextures(GVRSceneObject sceneObj0) {
            GVRTexture tmp = sceneObj0.getRenderData().getMaterial().getMainTexture();

            sceneObj0.getRenderData().getMaterial().setMainTexture(blueObject);

            blueObject = tmp;
        }

        public void onEnter(GVRSceneObject sceneObj0, GVRSceneObject sceneObj1, float normal[], float distance) {
            swapTextures(sceneObj0);
        }

       public void onExit(GVRSceneObject sceneObj0, GVRSceneObject sceneObj1, float normal[], float distance) {
            swapTextures(sceneObj0);
        }

    }

    private static final float CUBE_MASS = 0.5f;
    private static final float BALL_MASS = 2.5f;
    private GVRContext mGVRContext = null;
    private GVRRigidBody mSphereRigidBody = null;
    private CollisionHandler mCollisionHandler;
    private GVRScene mScene = null;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;
        mCollisionHandler = new CollisionHandler();
        GVRScene scene = mGVRContext.getNextMainScene();

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

        mScene = scene;
    }

    long randomActions = -1;

    public void touchEvent() {
        if (randomActions < 0) {
            /*
            0 - Enable/disable world simulation
            1 - Enable/disable body simulation
            2 - Add/Remove scene object
            3 - apply force on the ball
             */
            randomActions = System.currentTimeMillis() % 4;
        }

        if (randomActions == 0) {
            if (mSphereRigidBody.getWorld().isEnabled()) {
                mSphereRigidBody.getWorld().disable();
            } else {
                mSphereRigidBody.getWorld().enable();
                randomActions = -1;
            }
        } else if (randomActions == 1) {
            if (mSphereRigidBody.isEnabled()) {
                mSphereRigidBody.disable();
            } else {
                mSphereRigidBody.enable();
                randomActions = -1;
            }
        } else if (randomActions == 2) {
            if (mSphereRigidBody.getWorld() != null) {
                mScene.removeSceneObject(mSphereRigidBody.getOwnerObject());
            } else {
                mScene.addSceneObject(mSphereRigidBody.getOwnerObject());
                randomActions = -1;
            }
        } else {
            mSphereRigidBody.applyCentralForce(-20.0f, 900.0f, 0.0f);
            mSphereRigidBody.applyTorque(5.0f, 0.5f, 0.0f);
            randomActions = -1;
        }
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

            body.setRestitution(0.5f);
            body.setFriction(1.0f);

            meshObject.attachComponent(body);

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

        body.setMass(mass);
        body.setRestitution(0.5f);
        body.setFriction(1.0f);

        cubeObject.attachComponent(body);

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

        mSphereRigidBody.setMass(mass);
        mSphereRigidBody.setRestitution(1.5f);
        mSphereRigidBody.setFriction(0.5f);
        sphereObject.getEventReceiver().addListener(mCollisionHandler);

        sphereObject.attachComponent(mSphereRigidBody);

        scene.addSceneObject(sphereObject);
    }

}
