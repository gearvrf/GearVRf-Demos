package org.gearvrf.balloons;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

class Particle extends GVRBehavior
{
    static private long TYPE_PARTICLE = newComponentType(org.gearvrf.balloons.Particle.class);
    public float            Velocity;
    public Vector3f         Direction;
    public float            Distance;
    public Vector3f         StartPos;
    
    Particle(GVRContext ctx, float velocity, Vector3f direction)
    {
        super(ctx);
        Velocity = velocity;
        Direction = direction;
        mType = TYPE_PARTICLE;
    }

    static public long getComponentType() { return TYPE_PARTICLE; }

    public Vector3f getPosition()
    {
        Matrix4f worldmtx = getOwnerObject().getTransform().getModelMatrix4f();
        Vector3f pos = new Vector3f();
        worldmtx.getTranslation(pos);
        return pos;
    }
    
    public void onAttach(GVRSceneObject owner)
    {
        super.onAttach(owner);
        StartPos = getPosition();
    }
    
    public void move(float time)
    {
        GVRSceneObject owner = getOwnerObject();
        
        if (owner == null)
        {
            return;
        }
        Vector3f pos = getPosition();
        Float x = pos.x + Direction.x * Velocity * time;
        Float y = pos.y + Direction.y * Velocity * time;
        Float z = pos.z + Direction.z * Velocity * time;
        
        Distance = pos.sub(StartPos).length();
        getOwnerObject().getTransform().setPosition(x, y, z);
    }
}
