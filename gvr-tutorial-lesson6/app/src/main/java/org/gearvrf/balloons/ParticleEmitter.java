/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gearvrf.balloons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Vector2f;
import org.joml.Vector3f;


public class ParticleEmitter extends GVRBehavior
{
    static class Range<T>
    {
        public T    MinVal;
        public T    MaxVal;

        public Range(T oneVal)
        {
            MinVal = oneVal;
            MaxVal = oneVal;
        }
        public Range(T minval, T maxval)
        {
            MinVal = minval;
            MaxVal = maxval;
        }
        public boolean isRange()
        {
            return !MinVal.equals(MaxVal);
        }
    }

    public interface MakeParticle
    {
        GVRSceneObject create(GVRContext ctx);
    }
    
    /**
     * Total number of particles
     */
    public int TotalParticles = 50;
    
    /**
     * Maximum number of particles active
     */
    public int  MaxActiveParticles = 20;
    
    /**
     * Particles emitted per second
     */
    public float  EmissionRate = 2;
    
    /**
     * Velocity range of particle emitted in units per second
     */
    public  Range<Float>   Velocity = new Range<Float>(1.0f);

    /**
     * Direction vector for particles
     */
    public  Range<Vector3f>  Direction = new Range<Vector3f>(new Vector3f(0, 0, 1));

    public  Range<Vector2f> EmitterArea = new Range<Vector2f>(new Vector2f(-5.0f, -5.0f), new Vector2f(5.0f, 5.0f));

    /**
     * Maximum distance of particle from starting point
     * before it disappears
     */
    public  float     MaxDistance = 10.0f;
    
    private ArrayList<Particle> mFreeParticles;
    private ArrayList<Particle> mActiveParticles;
    private GVRScene    mScene;
    private Random      mRandom = new Random();
    private float       mLastEmitTime;
    private int         mNumParticles = 0;
    private MakeParticle mMakeParticle;
    static private long TYPE_PARTICLE_EMITTER = newComponentType(ParticleEmitter.class);

    public ParticleEmitter(GVRContext ctx, GVRScene scene, MakeParticle newParticle)
    {
        super(ctx);
        mFreeParticles = new ArrayList<Particle>();
        mActiveParticles = new ArrayList<Particle>();
        mMakeParticle = newParticle;
        mScene = scene;
        mType = TYPE_PARTICLE_EMITTER;
    }

    static public long getComponentType() { return TYPE_PARTICLE_EMITTER; }

    public void onEnable()
    {
        super.onEnable();
        mLastEmitTime = 0;
    }

    public void stop(Particle particle)
    {
        synchronized (mActiveParticles)
        {
            GVRSceneObject owner = particle.getOwnerObject();
            owner.setEnable(false);
            mActiveParticles.remove(particle);
            mFreeParticles.add(particle);
        }
    }

    public void onDrawFrame(float elapsed)
    {
        if (isEnabled())
        {
            step(elapsed);
        }
    }

    protected void step(float elapsed)
    {
        float emitTime = 1 / EmissionRate;
        mLastEmitTime += elapsed;
        synchronized (mActiveParticles)
        {
            for (Iterator<Particle> iter = mActiveParticles.iterator(); iter.hasNext(); )
            {
                Particle particle = iter.next();
                particle.move(elapsed);
                if (particle.Distance > MaxDistance)
                {
                    iter.remove();
                    mFreeParticles.add(particle);
                    particle.getOwnerObject().setEnable(false);
                }
            }
        }
        if (mLastEmitTime >= emitTime)
        {
            emit();
            mLastEmitTime = 0;
        }
    }

    private Vector3f getNextDirection()
    {
        Vector3f direction = new Vector3f(Direction.MaxVal);
        if (Direction.isRange())
        {
            direction.sub(Direction.MinVal, direction);
            direction.mul(mRandom.nextFloat());
            direction.add(Direction.MinVal, direction);
        }
        return direction;
    }

    private float getNextVelocity()
    {
        float velocity = Velocity.MinVal;
        if (Velocity.isRange())
        {
            velocity += mRandom.nextFloat() * (Velocity.MaxVal - Velocity.MinVal);
        }
        return velocity;
    }

    private Vector3f getNextPosition()
    {
        Vector3f v = new Vector3f(EmitterArea.MaxVal.x, EmitterArea.MaxVal.y, 0);
        if (EmitterArea.isRange())
        {
            v.sub(EmitterArea.MinVal.x, EmitterArea.MinVal.y, 0);
            v.mul(mRandom.nextFloat(), mRandom.nextFloat(), 0);
            v.add(EmitterArea.MinVal.x, EmitterArea.MinVal.y, 0);
        }
        return v;
    }

    protected void emit()
    {
        Particle particle = null;
        GVRSceneObject sceneObj = null;
        Vector3f direction = getNextDirection();
        float velocity = getNextVelocity();

        if (mFreeParticles.size() > 0)
        {
            int last = mFreeParticles.size() - 1;
            particle = mFreeParticles.get(last);
            mFreeParticles.remove(last);
            sceneObj = particle.getOwnerObject();
            particle.Velocity = velocity;
            particle.Direction = direction;
        }
        else
        {
            if (mNumParticles >= TotalParticles)
            {
                return; // cannot create any more
            }
            if (mActiveParticles.size() >= MaxActiveParticles)
            {
                return; // cannot emit any more
            }
            sceneObj = mMakeParticle.create(getGVRContext());
            sceneObj.setName(sceneObj.getName() + Integer.valueOf(mNumParticles).toString());
            ++mNumParticles;
            particle = new Particle(getGVRContext(), velocity, direction);
            sceneObj.attachComponent(particle);
            getOwnerObject().addChildObject(sceneObj);
            sceneObj.getRenderData().bindShader(mScene);
        }
        particle.setPosition(getNextPosition());
        mActiveParticles.add(particle);
        sceneObj.setEnable(true);
    }
 }
