package org.gearvrf.gvrsimlephysics.entity;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.physics.GVRRigidBody;


/**
 * Created by d.alipio@samsung.com on 10/20/16.
 */

public class Ball extends GVRSceneObject {


    private GVRRigidBody rigidBody;

    public Ball(GVRContext gvrContext, GVRAndroidResource mesh, GVRAndroidResource texture) {
        super(gvrContext, mesh, texture);
        //setLight();
    }

    public void setPhysic() {
        GVRSphereCollider sphereCollider = new GVRSphereCollider(getGVRContext());
        sphereCollider.setRadius(1.0f);
        attachCollider(sphereCollider);

        rigidBody = new GVRRigidBody(getGVRContext());
        rigidBody.setMass(.5f);
        rigidBody.setRestitution(1.5f);
        rigidBody.setFriction(0.5f);
        rigidBody.setEnable(true);
        rigidBody.setCollisionType(CollisionFilter.BALL_ID);
        attachComponent(rigidBody);
    }

    public GVRRigidBody getRigidBody() {
        return rigidBody;
    }

    public void enablePhysic(boolean enable) {
        rigidBody.setEnable(enable);
    }

    private void setLight() {
        GVRMaterial material = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.BeingGenerated.ID);
        material.setVec4("u_color", 0.2f, 0.2f, 0.8f, 1);
        material.setVec4("diffuse_color", 0.2f, 0.2f, 0.8f, 1.0f);
        material.setVec4("ambient_color", 0.2f, 0.2f, 0.8f, 1.0f);
        material.setVec4("specular_color", 0.2f, 0.2f, 0.8f, 1.0f);
        material.setVec4("emissive_color", 0.0f, 0.0f, 0.0f, 0.0f);
        material.setFloat("specular_exponent", 10.0f);
        getRenderData().setMaterial(material);
        getRenderData().setShaderTemplate(GVRPhongShader.class);
    }
}
