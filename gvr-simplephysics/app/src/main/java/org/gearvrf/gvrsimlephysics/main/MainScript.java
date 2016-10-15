package org.gearvrf.gvrsimlephysics.main;

import android.util.Log;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.gvrsimlephysics.R;
import org.gearvrf.gvrsimlephysics.entity.Ball;
import org.gearvrf.gvrsimlephysics.entity.Countdown;
import org.gearvrf.gvrsimlephysics.util.MathUtils;
import org.gearvrf.gvrsimlephysics.util.VRTouchPadGestureDetector;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.physics.ICollisionEvents;

import java.io.IOException;
import java.util.concurrent.Future;

import static org.gearvrf.gvrsimlephysics.entity.CollisionFilter.CYLINDER_ID;
import static org.gearvrf.gvrsimlephysics.entity.CollisionFilter.GROUND_ID;
import static org.gearvrf.gvrsimlephysics.entity.CollisionFilter.INVISIBLE_GROUND_ID;

public class MainScript extends GVRMain {

    private GVRContext gvrContext = null;
    private GVRScene scene;
    private GVRCameraRig mainCameraRig;
    private static final float CUBE_MASS = 0.3f;


    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        this.gvrContext = gvrContext;
        scene = this.gvrContext.getNextMainScene();
        scene.addSceneObject(createLight(getGVRContext()));

        mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(245f, 244f, 214f, 255f);
        mainCameraRig.getRightCamera().setBackgroundColor(245f, 244f, 214f, 255f);
        mainCameraRig.getTransform().setPosition(0.0f, 6.0f, 20f);

        addInvisibleGround();
        addGroundMesh();
        addCylinderGroup();
        addGaze();
        addTimer();

        scene.getRoot().attachComponent(new GVRWorld(gvrContext));
        scene.getEventReceiver().addListener(this);
    }

    private void addInvisibleGround() {

        GVRMesh mesh = gvrContext.createQuad(300.0f, 300.0f);
        Future<GVRTexture> texture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.black));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSceneObject groundObject = new GVRSceneObject(gvrContext, mesh);
        groundObject.getRenderData().setMaterial(material);
        groundObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
        groundObject.getTransform().setPosition(0.0f, -4f, 0.0f);
        groundObject.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
        groundObject.getEventReceiver().addListener(new ICollisionEvents() {
            @Override
            public void onEnter(GVRSceneObject gvrSceneObject, GVRSceneObject gvrSceneObject1, float[] floats, float v) {

                if (gvrSceneObject.getName().equals("cylinder")) {

                    gvrSceneObject.setEnable(false);
                    GVRRigidBody rigidBody = (GVRRigidBody) gvrSceneObject.getComponent(GVRRigidBody.getComponentType());
                    rigidBody.getCollisionType().colideNotWith(INVISIBLE_GROUND_ID);
                    gvrSceneObject.setEnable(true);
                    scene.removeSceneObject(gvrSceneObject);
                    Log.d("douglas", "removi");
                }
            }

            @Override
            public void onExit(GVRSceneObject gvrSceneObject, GVRSceneObject gvrSceneObject1, float[] floats, float v) {

            }
        });

        //set phong Shader
        groundObject.getRenderData().setShaderTemplate(GVRPhongShader.class);


        // Collider
        GVRMeshCollider meshCollider = new GVRMeshCollider(gvrContext, mesh);
        groundObject.attachCollider(meshCollider);

        // Physics body
        GVRRigidBody body = new GVRRigidBody(gvrContext);
        body.setRestitution(0.5f);
        body.setFriction(1.0f);
        body.setCollisionType(INVISIBLE_GROUND_ID);
        groundObject.attachComponent(body);
        scene.addSceneObject(groundObject);
    }


    private void addCylinderGroup() throws IOException {

        scene.addSceneObject(createCylinder(7f, .5f, 1.0f, CUBE_MASS, R.drawable.black));
        scene.addSceneObject(createCylinder(5f, .5f, 1.0f, CUBE_MASS, R.drawable.brown));
        scene.addSceneObject(createCylinder(3f, .5f, 1.0f, CUBE_MASS, R.drawable.green));
        scene.addSceneObject(createCylinder(7f, 1.8f, 1.0f, CUBE_MASS, R.drawable.grey));
        scene.addSceneObject(createCylinder(4f, .5f, 2.5f, CUBE_MASS, R.drawable.orange));
        scene.addSceneObject(createCylinder(-3f, .5f, 2.5f, CUBE_MASS, R.drawable.pink));
        scene.addSceneObject(createCylinder(0.5f, .5f, 2f, CUBE_MASS, R.drawable.red));
        scene.addSceneObject(createCylinder(2.5f, .5f, 3.5f, CUBE_MASS, R.drawable.yellow));
        scene.addSceneObject(createCylinder(2.5f, 1.8f, 3.3f, CUBE_MASS, R.drawable.light_blue));
        scene.addSceneObject(createCylinder(3.0f, .5f, 5.5f, CUBE_MASS, R.drawable.light_green));
        scene.addSceneObject(createCylinder(-5f, 5f, -1.5f, CUBE_MASS, R.drawable.dark_blue));
        scene.addSceneObject(createCylinder(5.5f, 15f, 7f, CUBE_MASS, R.drawable.cy));
    }

    private Ball createBall(float x, float y, float z) throws IOException {
        Ball ball = new Ball(gvrContext, new GVRAndroidResource(gvrContext, "ball.fbx"),
                new GVRAndroidResource(gvrContext, R.drawable.orange));
        ball.getTransform().setPosition(x, y, z);
        scene.addSceneObject(ball);
        return ball;

    }

    private GVRSceneObject createCylinder(float x, float y, float z, float mass, int drawable) throws IOException {
        GVRSceneObject cubeObject = new GVRSceneObject(gvrContext, new GVRAndroidResource(gvrContext, "cylinder.fbx"),
                new GVRAndroidResource(gvrContext, drawable));
        cubeObject.getTransform().setPosition(x, y, z);
        cubeObject.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);


        // Collider
        GVRMeshCollider meshCollider = new GVRMeshCollider(gvrContext, cubeObject.getRenderData().getMesh());
        cubeObject.attachCollider(meshCollider);

        // Physics body
        GVRRigidBody body = new GVRRigidBody(gvrContext);
        body.setMass(mass);
        body.setCollisionType(CYLINDER_ID);
        body.setRestitution(0.5f);
        body.setFriction(5.0f);
        cubeObject.attachComponent(body);

        return cubeObject;
    }

    private GVRSceneObject createLight(GVRContext context) {
        GVRSceneObject lightNode = new GVRSceneObject(context);
        GVRDirectLight light = new GVRDirectLight(context);
        light.setCastShadow(true);
        light.setAmbientIntensity(0.3f * 1, 0.3f * 1, 0, 1);
        light.setDiffuseIntensity(1, .9f, .8f, 1);
        light.setSpecularIntensity(1, .9f, .8f, 1);
        lightNode.getTransform().setPosition(0, 9f, 1);
        lightNode.getTransform().setRotationByAxis(-90, 1, 0, 0);
        lightNode.attachLight(light);
        return lightNode;
    }


    private void addGaze() {

        GVRSceneObject gaze = new GVRSceneObject(gvrContext,
                new FutureWrapper<GVRMesh>(gvrContext.createQuad(0.1f, 0.1f)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.gaze)));

        gaze.getTransform().setPosition(0.0f, 0.0f, -1f);
        gaze.getRenderData().setDepthTest(false);
        gaze.getRenderData().setRenderingOrder(100000);
        scene.getMainCameraRig().addChildObject(gaze);

    }

    private void addTimer() {

        GVRMesh mesh = getGVRContext().createQuad(10f, 10f);
        GVRTexture texture = getGVRContext().loadTexture(new GVRAndroidResource(getGVRContext(), R.drawable.light_blue));
        Countdown countdownObject = new Countdown(gvrContext, mesh);
        scene.addSceneObject(countdownObject);
    }

    private void addGroundMesh() {

        GVRMesh mesh = gvrContext.createQuad(30.0f, 30.0f);
        Future<GVRTexture> texture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.orange));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSceneObject groundObject = new GVRSceneObject(gvrContext, mesh);
        groundObject.getRenderData().setMaterial(material);
        groundObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
        groundObject.getTransform().setPosition(0.0f, 0f, 0.0f);
        groundObject.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

        //set phong Shader
        groundObject.getRenderData().setShaderTemplate(GVRPhongShader.class);


        // Collider
        GVRMeshCollider meshCollider = new GVRMeshCollider(gvrContext, mesh);
        groundObject.attachCollider(meshCollider);

        // Physics body
        GVRRigidBody body = new GVRRigidBody(gvrContext);
        body.setRestitution(0.5f);
        body.setCollisionType(GROUND_ID);
        body.setFriction(1.0f);
        groundObject.attachComponent(body);
        scene.addSceneObject(groundObject);
    }

    public void onSwipe(VRTouchPadGestureDetector.SwipeDirection swipeDirection, float velocityX) {

        if (swipeDirection == VRTouchPadGestureDetector.SwipeDirection.Forward) {
            try {

                int force = MathUtils.calculateForce(velocityX);
                float[] vector = MathUtils.calculateRotation(mainCameraRig.getHeadTransform()
                        .getRotationPitch(), mainCameraRig.getHeadTransform().getRotationYaw());
                Ball ball = createBall(mainCameraRig.getTransform().getPositionX(),
                        mainCameraRig.getTransform().getPositionY(), mainCameraRig.getTransform().getPositionZ());
                ball.setPhysic();
                ball.getRigidBody().applyCentralForce(vector[0] * force, vector[1] * force, vector[2] * force);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStep() {

    }

}
