package org.gearvrf.gvrsimlephysics.main;

import android.graphics.Color;
import android.view.Gravity;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.gvrsimlephysics.R;
import org.gearvrf.physics.GVRCollisionMatrix;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by ragner on 11/9/16.
 */
public class MainHelper {
    private static final float CYLINDER_MASS = 0.3f;
    private static final float BALL_MASS = 0.5f;
    private static final int COLLISION_GROUP_INFINITY_GROUND = 0;
    private static final int COLLISION_GROUP_GROUND = 1;
    private static final int COLLISION_GROUP_BALL = 3;
    public static final int COLLISION_GROUP_CYLINDER = 2;

    public static GVRCollisionMatrix collisionMatrix;

    static {
        collisionMatrix = new GVRCollisionMatrix();

        collisionMatrix.setCollisionFilterMask(COLLISION_GROUP_INFINITY_GROUND, (short) 0x0);

        collisionMatrix.enableCollision(COLLISION_GROUP_CYLINDER, COLLISION_GROUP_GROUND);
        collisionMatrix.enableCollision(COLLISION_GROUP_BALL, COLLISION_GROUP_GROUND);
        collisionMatrix.enableCollision(COLLISION_GROUP_BALL, COLLISION_GROUP_CYLINDER);
    }

    public static GVRSceneObject createPointLight(GVRContext context, float x, float y, float z) {
        GVRSceneObject lightObject = new GVRSceneObject(context);
        GVRPointLight light = new GVRPointLight(context);

       float ambientIntensity = 0.5f;
       float diffuseIntensity = 1.0f;

        light.setAmbientIntensity(1.0f * ambientIntensity, 0.95f * ambientIntensity, 0.83f * ambientIntensity, 0.0f);
        light.setDiffuseIntensity(1.0f * diffuseIntensity, 0.95f * diffuseIntensity, 0.83f * diffuseIntensity, 0.0f);
        light.setSpecularIntensity(0.0f, 0.0f, 0.0f, 0.0f);

        //lightObject.getTransform().setScale(1.0f, 1.0f, 5.0f);
        lightObject.getTransform().setPosition(x, y, z);
        lightObject.attachLight(light);
        return lightObject;
    }

   public static GVRSceneObject createDirectLight(GVRContext context, float x, float y, float z) {
        Future<GVRTexture> texture = context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.drawable.yellow));
        GVRSceneObject lightObject = new GVRSceneObject(context);//GVRSphereSceneObject(context, true, texture);
        GVRDirectLight light = new GVRDirectLight(context);

        light.setCastShadow(true);

       float ambientIntensity = 0.1f;
       float diffuseIntensity = 1.0f;

        light.setAmbientIntensity(1.0f * ambientIntensity, 0.95f * ambientIntensity, 0.83f * ambientIntensity, 0.0f);
        light.setDiffuseIntensity(1.0f * diffuseIntensity, 0.95f * diffuseIntensity, 0.83f * diffuseIntensity, 0.0f);
        light.setSpecularIntensity(0.0f, 0.0f, 0.0f, 0.0f);

        lightObject.getTransform().setPosition(x, y, z);
        lightObject.attachLight(light);
        return lightObject;
    }

    public static GVRSceneObject createGround(GVRContext context, float x, float y, float z) {
        Future<GVRTexture> texture = context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.drawable.orange));
        GVRMaterial material = new GVRMaterial(context);

        GVRSceneObject groundObject = new GVRCubeSceneObject(context, true, texture);

        groundObject.getRenderData().setMaterial(material);
        groundObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
        groundObject.getRenderData().getMaterial().setMainTexture(texture);
        groundObject.getTransform().setScale(15.0f, 0.5f, 15.0f);
        groundObject.getTransform().setPosition(x, y, z);

        //set phong Shader
        groundObject.getRenderData().setShaderTemplate(GVRPhongShader.class);

        // Collider
        GVRMeshCollider meshCollider = new GVRMeshCollider(context, groundObject.getRenderData().getMesh());
        groundObject.attachCollider(meshCollider);

        // Physics body
        GVRRigidBody body = new GVRRigidBody(context, 0.0f, COLLISION_GROUP_GROUND);
        body.setRestitution(0.5f);
        body.setFriction(1.0f);
        groundObject.attachComponent(body);

        return groundObject;
    }

    public static GVRSceneObject createCylinder(GVRContext context, float x, float y, float z,
                                                 int drawable) throws IOException {
        Future<GVRTexture> texture = context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, drawable));
        Future<GVRMesh> mesh = context.getAssetLoader().loadFutureMesh(new GVRAndroidResource(context, "cylinder.fbx"));
        GVRSceneObject cylinderObject = new GVRSceneObject(context, mesh, texture);

        cylinderObject.getTransform().setPosition(x, y, z);
        cylinderObject.getTransform().setRotationByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        cylinderObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
        //set phong Shader
        cylinderObject.getRenderData().setShaderTemplate(GVRPhongShader.class);

        // Collider
        GVRMeshCollider meshCollider = new GVRMeshCollider(context, cylinderObject.getRenderData().getMesh());
        cylinderObject.attachCollider(meshCollider);

        // Physics body
        GVRRigidBody body = new GVRRigidBody(context, CYLINDER_MASS, COLLISION_GROUP_CYLINDER);
        body.setRestitution(0.5f);
        body.setFriction(5.0f);
        cylinderObject.attachComponent(body);

        return cylinderObject;
    }

    public static GVRSceneObject createBall(GVRContext context, float x, float y, float z,
                                            float[] force) throws IOException {
        Future<GVRTexture> texture = context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.drawable.grey));
        Future<GVRMesh> mesh = context.getAssetLoader().loadFutureMesh(new GVRAndroidResource(context, "ball.fbx"));

        GVRSceneObject ballObject = new GVRSceneObject(context, mesh, texture);
        ballObject.getTransform().setScale(0.7f, 0.7f, 0.7f);
        ballObject.getTransform().setPosition(x, y, z);

        ballObject.getRenderData().getMaterial().setTexture("diffuseTexture", texture);
        //set phong Shader
        ballObject.getRenderData().setShaderTemplate(GVRPhongShader.class);
        ballObject.getRenderData().getMaterial().setDiffuseColor(1.0f, 1.0f, 1.0f, 1.f);

        GVRSphereCollider sphereCollider = new GVRSphereCollider(context);
        sphereCollider.setRadius(1.0f);
        ballObject.attachCollider(sphereCollider);

        GVRRigidBody rigidBody = new GVRRigidBody(context, BALL_MASS, COLLISION_GROUP_BALL);
        rigidBody.setRestitution(1.5f);
        rigidBody.setFriction(0.5f);
        rigidBody.applyCentralForce(force[0], force[1], force[2]);
        ballObject.attachComponent(rigidBody);

        return ballObject;
    }

    public static GVRSceneObject createGaze(GVRContext context, float x, float y, float z) {
        GVRSceneObject gaze = new GVRSceneObject(context,
                new FutureWrapper<GVRMesh>(context.createQuad(0.1f, 0.1f)),
                context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.drawable.gaze)));

        gaze.getTransform().setPosition(x, y, z);
        gaze.getRenderData().setDepthTest(false);
        gaze.getRenderData().setRenderingOrder(100000);

        return gaze;
    }

    public static GVRTextViewSceneObject createLabel(GVRContext context, float x, float y, float z) {
        GVRTextViewSceneObject textObject = new GVRTextViewSceneObject(context, 5f, 2f, "00");
        textObject.setTextColor(Color.BLACK);
        textObject.setGravity(Gravity.CENTER);
        textObject.setTextSize(20);
        textObject.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.LOW);
        textObject.getTransform().setPosition(x, y, z);
        return textObject;
    }
}
