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
import org.joml.Vector3f;


public class ParticleEmitter extends GVRBehavior
{
    public interface MakeParticle
    {
        GVRSceneObject create(GVRContext ctx);
    }
    
    /**
     * Total number of particles
     */
    public int TotalParticles = 100;
    
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
    public  float   MinVelocity = 1;
    public  float   MaxVelocity = 1;
    
    /**
     * Direction vector for particles
     */
    public  Vector3f  Direction = new Vector3f(0, 1, 0);

    /**
     * Maximum distance of particle from starting point
     * before it disappears
     */
    public  float     MaxDistance = 100.0f;
    
    private ArrayList<Particle> mFreeParticles;
    private ArrayList<Particle> mActiveParticles;
    private GVRScene    mScene;
    private Random      mRandom = new Random();
    private float       mLastEmitTime;
    private boolean     mIsEmitting = false;
    private int         mNumParticles = 0;
    private Vector3f    mEmitterAreaMin = new Vector3f(-10.0f, 0.0f, -10.0f);
    private Vector3f    mEmitterAreaMax = new Vector3f(10.0f, 0.01f, 0.0f);
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

    /**
     * Returns true if particle system is emitting now
     */
    public boolean isEmitting() { return mIsEmitting; }
    
    /**
     * Designates the 3D volume from which the particles are emitted
     */
    public void setEmitterArea(Vector3f minCorner, Vector3f maxCorner)
    {
        mEmitterAreaMin = minCorner;
        mEmitterAreaMax = maxCorner;
    }
    
    public void start()
    {
        mLastEmitTime = 0;
        mIsEmitting = true;
    }
    
    public void stop()
    {
        mIsEmitting = false;
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
        if (!mIsEmitting)
        {
            return;
        }
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
    
    protected void emit()
    {
        Particle particle = null;
        GVRSceneObject sceneObj = null;
        float velocity = MinVelocity;

        if (MinVelocity < MaxVelocity)
        {
            velocity = MinVelocity + mRandom.nextFloat() * (MaxVelocity - MinVelocity);
        }
        if (mFreeParticles.size() > 0)
        {
            int last = mFreeParticles.size() - 1;
            particle = mFreeParticles.get(last);
            mFreeParticles.remove(last);
            sceneObj = particle.getOwnerObject();
            particle.Velocity = velocity;
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
            particle = new Particle(getGVRContext(), velocity, Direction);
            sceneObj.attachComponent(particle);
            getOwnerObject().addChildObject(sceneObj);
            sceneObj.getRenderData().bindShader(mScene);
        }
        Float x = mEmitterAreaMin.x + mRandom.nextFloat() * (mEmitterAreaMax.x - mEmitterAreaMin.x);
        Float y = mEmitterAreaMin.y + mRandom.nextFloat() * (mEmitterAreaMax.y - mEmitterAreaMin.y);
        Float z = mEmitterAreaMin.z + mRandom.nextFloat() * (mEmitterAreaMax.z - mEmitterAreaMin.z);
        sceneObj.getTransform().setPosition(x,  y,  z);
        mActiveParticles.add(particle);
        sceneObj.setEnable(true);
    }    
 }
