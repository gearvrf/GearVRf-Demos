/*
Bullet Continuous Collision Detection and Physics Library for Android NDK
Copyright (c) 2006-2009 Noritsuna Imamura  http://www.siprop.org/

This software is provided 'as-is', without any express or implied warranty.
In no event will the authors be held liable for any damages arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it freely,
subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package org.siprop.bullet;

import java.util.HashMap;
import java.util.Map;

import org.siprop.bullet.interfaces.Constraint;
import org.siprop.bullet.interfaces.DynamicsWorld;
import org.siprop.bullet.interfaces.ResultSimulationCallback;
import org.siprop.bullet.interfaces.Shape;
import org.siprop.bullet.interfaces.Solver;
import org.siprop.bullet.util.Vector3;

import android.util.Log;


public class Bullet {

	
	private Map<Integer, PhysicsWorld> physicsWorlds = new HashMap<Integer, PhysicsWorld>();
	private Map<Integer, Geometry> geometries = new HashMap<Integer, Geometry>();
	private Map<Integer, RigidBody> rigidBodies = new HashMap<Integer, RigidBody>();
	
	// PhysicsWorld
	private PhysicsWorld defaultPhysicsWorld;
	
	public void setDefaultPhysicsWorld(PhysicsWorld defaultPhysicsWorld) {
		this.defaultPhysicsWorld = defaultPhysicsWorld;
	}
	public PhysicsWorld getDefaultPhysicsWorld() {
		return defaultPhysicsWorld;
	}
	
	public Map<Integer, PhysicsWorld> getPhysicsWorlds() {
		return physicsWorlds;
	}
	public PhysicsWorld getPhysicsWorld(int id) {
		return physicsWorlds.get(id);
	}
	public PhysicsWorld createPhysicsWorld(Vector3 worldAabbMin,
										  Vector3 worldAabbMax, 
										  int maxProxies, 
										  Vector3 gravity) {
		
		PhysicsWorld phyWorld = new PhysicsWorld();
		phyWorld.worldAabbMin = worldAabbMin;
		phyWorld.worldAabbMax = worldAabbMax;
		phyWorld.maxProxies = maxProxies;
		phyWorld.gravity = gravity;
		
		phyWorld.id = createNonConfigPhysicsWorld(phyWorld);
		
		defaultPhysicsWorld = phyWorld;
		physicsWorlds.put(phyWorld.id, phyWorld);
		return phyWorld;
	}
	
	public PhysicsWorld createPhysicsWorld(CollisionConfiguration cllisionConfiguration,
								   CollisionDispatcher collisionDispatcher,
								   Solver solver,
								   DynamicsWorld dynamicsWorld,
								   Vector3 worldAabbMin,
								   Vector3 worldAabbMax, 
								   int maxProxies, 
								   Vector3 gravity) {
		
		PhysicsWorld phyWorld = new PhysicsWorld();
		phyWorld.cllisionConfiguration = cllisionConfiguration;
		phyWorld.collisionDispatcher = collisionDispatcher;
		phyWorld.solver = solver;
		phyWorld.dynamicsWorld = dynamicsWorld;
		phyWorld.worldAabbMin = worldAabbMin;
		phyWorld.worldAabbMax = worldAabbMax;
		phyWorld.maxProxies = maxProxies;
		phyWorld.gravity = gravity;
		
		phyWorld.id = createPhysicsWorld(phyWorld);
		
		defaultPhysicsWorld = phyWorld;
		physicsWorlds.put(phyWorld.id, phyWorld);
		return phyWorld;
	}
	
	public native int createNonConfigPhysicsWorld(PhysicsWorld physicsWorld);
	public native int createPhysicsWorld(PhysicsWorld physicsWorld);
	
	public native int changePhysicsWorldConfiguration(PhysicsWorld physicsWorld);
	
	

	// Geometry
	public Map<Integer, Geometry> getGeometries() {
		return geometries;
	}
	public Geometry getGeometry(int id) {
		return geometries.get(id);
	}
	public Geometry createGeometry(Shape collisionShape,
								   float mass,
								   Vector3 localInertia) {
		
		Geometry geometry = new Geometry();
		geometry.shape = collisionShape;
		geometry.mass = mass;
		geometry.localInertia = localInertia;
		
		geometry.id = createGeometry(geometry);
		geometry.shape.setID(geometry.id);
		geometries.put(geometry.id, geometry);
		return geometry;
	}
	
	public native int createGeometry(Geometry geometry);

	
	
	// RigidBody
	public RigidBody createAndAddRigidBody(Geometry geometry,
										   MotionState motionState) {
		return createAndAddRigidBody(defaultPhysicsWorld, geometry, motionState);
	}
	public RigidBody createAndAddRigidBody(PhysicsWorld physicsWorld, 
			Geometry geometry,
			MotionState motionState) {
		
		RigidBody rigidBody = new RigidBody();
		rigidBody.geometry = geometry;
		rigidBody.motionState = motionState;
		rigidBody.physicsWorldId = physicsWorld.id;
		rigidBody.id = createAndAddRigidBody(physicsWorld.id, rigidBody);
		
		rigidBodies.put(rigidBody.id , rigidBody);
		
		return rigidBody;
	}
	public RigidBody createAndAddRigidBody(RigidBody rigidBody) {
		rigidBody.physicsWorldId = defaultPhysicsWorld.id;
		rigidBody.id = createAndAddRigidBody(rigidBody.physicsWorldId, rigidBody);
		rigidBodies.put(rigidBody.id , rigidBody);
		return rigidBody;
	}
	public native int createAndAddRigidBody(int physicsWorldId, RigidBody rigidBody);
	
	
	public void removeRigidBody(RigidBody body) {
		if(body.physicsWorldId >= 0) {
			removeRigidBody(body.physicsWorldId, body);
			body.physicsWorldId = 0;
			rigidBodies.remove(body.id);
		}
	}
	public native void removeRigidBody(int worldID, RigidBody body);	


	
	// applyForce
	public void applyForce(RigidBody body, Vector3 force, Vector3 applyPoint) {
		if(body.id > 0 && body.physicsWorldId > 0) {
			applyForce(body.physicsWorldId, body.id, force, applyPoint);
		}
	}
	public native int applyForce(int physicsWorldId, int rigidBodyId, Vector3 force, Vector3 applyPoint);

	
	public void applyTorque(RigidBody body, Vector3 torque) {
		if(body.id > 0 && body.physicsWorldId > 0) {
			applyTorque(body.physicsWorldId, body.id, torque);
		}
	}
	public native int applyTorque(int physicsWorldId, int rigidBodyId, Vector3 torque);
	
	
	public void applyCentralImpulse(RigidBody body, Vector3 impulse) {
		//if(body.id > 0 && body.physicsWorldId > 0) {
			applyCentralImpulse(body.physicsWorldId, body.id, impulse);
		//}
	}
	public native int applyCentralImpulse(int physicsWorldId, int rigidBodyId, Vector3 impulse);

	
  	public void applyTorqueImpulse(RigidBody body, Vector3 torque) {
		if(body.id > 0 && body.physicsWorldId > 0) {
			applyTorqueImpulse(body.physicsWorldId, body.id, torque);
		}
	}
	public native int applyTorqueImpulse(int physicsWorldId, int rigidBodyId, Vector3 torque);

	
	public void applyImpulse(RigidBody body, Vector3 impulse, Vector3 applyPoint)  {
		if(body.id > 0 && body.physicsWorldId > 0) {
			applyImpulse(body.physicsWorldId, body.id, impulse, applyPoint);
		}
	}
	public native int applyImpulse(int physicsWorldId, int rigidBodyId, Vector3 impulse, Vector3 applyPoint);

	
	public void clearForces(RigidBody body) {
		if(body.id > 0 && body.physicsWorldId > 0) {
			clearForces(body.physicsWorldId, body.id);
		}
	}
	public native int clearForces(int physicsWorldId, int rigidBodyId);
	

	public void setActive(PhysicsWorld physicsWorld, boolean isActive) {
		setActivePhysicsWorldAll(physicsWorld.id, isActive);
	}
	public void setActive(RigidBody body, boolean isActive) {
		setActive(body.physicsWorldId, body.id, isActive);
	}
	public native int setActive(int physicsWorldId, int rigidBodyId, boolean isActive);
	public native int setActivePhysicsWorldAll(int physicsWorldId, boolean isActive);
	public native int setActiveAll(boolean isActive);

	
	// addConstraint
	public native int addConstraint(Constraint constraint);
		
	
	public Map<Integer, RigidBody> doSimulation(float execTime, int count) {
		return doSimulation(defaultPhysicsWorld, execTime, count);
	}
	public Map<Integer, RigidBody> doSimulation(PhysicsWorld physicsWorld, float execTime, int count) {
		doSimulationNative(physicsWorld.id, execTime, count);
		return rigidBodies;
	}
	public Map<Integer, RigidBody> doSimulationWithCallback(ResultSimulationCallback resultCallback, float execTime, int count) {
		return doSimulationWithCallback(resultCallback, defaultPhysicsWorld, execTime, count);
	}
	public Map<Integer, RigidBody> doSimulationWithCallback(ResultSimulationCallback resultCallback, PhysicsWorld physicsWorld, float execTime, int count) {
		doSimulationNative(physicsWorld.id, execTime, count);
		if(resultCallback != null) {
			resultCallback.resultSimulation(rigidBodies);
		}
		return rigidBodies;
	}
	private native int doSimulationNative(int worldId, float execTime, int count); 

	private void resultSimulation(int rigidBodyID, int shapeType, float[] rot, float[] pos, float[] shapeOption) {
		RigidBody body = rigidBodies.get(rigidBodyID);
		if(body == null) {
			Log.d("resultSimulation", "body is null.");
			return;
		}
		if(rot.length < 9) {
			Log.d("resultSimulation", "rot is " + rot.length);
			return;
		}
		if(pos.length < 3) {
			Log.d("resultSimulation", "pos is " + pos.length);
			return;
		}
		body.motionState.resultSimulation.basis.xx = rot[0];
		body.motionState.resultSimulation.basis.xy = rot[1];
		body.motionState.resultSimulation.basis.xz = rot[2];
		body.motionState.resultSimulation.basis.yx = rot[3];
		body.motionState.resultSimulation.basis.yy = rot[4];
		body.motionState.resultSimulation.basis.yz = rot[5];
		body.motionState.resultSimulation.basis.zx = rot[6];
		body.motionState.resultSimulation.basis.zy = rot[7];
		body.motionState.resultSimulation.basis.zz = rot[8];
		
		body.motionState.resultSimulation.originPoint.x = pos[0];
		body.motionState.resultSimulation.originPoint.y = pos[1];
		body.motionState.resultSimulation.originPoint.z = pos[2];
		
		body.motionState.resultSimulation.option_param[0] = shapeOption[0];
		body.motionState.resultSimulation.option_param[1] = shapeOption[1];
		body.motionState.resultSimulation.option_param[2] = shapeOption[2];
		body.motionState.resultSimulation.option_param[3] = shapeOption[3];
		body.motionState.resultSimulation.option_param[4] = shapeOption[4];
		body.motionState.resultSimulation.option_param[5] = shapeOption[5];
		body.motionState.resultSimulation.option_param[6] = shapeOption[6];
		body.motionState.resultSimulation.option_param[7] = shapeOption[7];
		body.motionState.resultSimulation.option_param[8] = shapeOption[8];
//		if(shapeType == ShapeType.BOX_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.STATIC_PLANE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.SPHERE_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.CAPSULE_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.CONE_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.CYLINDER_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.TETRAHEDRAL_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.EMPTY_SHAPE_PROXYTYPE) {
//		} else if(shapeType == ShapeType.TRIANGLE_SHAPE_PROXYTYPE) {
//		}
	}
	
	
	public void destory() {
		defaultPhysicsWorld = null;
		destroyNative();
	}
	public native void destroyPhysicsWorld(PhysicsWorld physicsWorld);
	private native void destroyNative();
	
	
    static {
        System.loadLibrary("bullet");
    }
}
