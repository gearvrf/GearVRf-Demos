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
package org.siprop.bullet.constraint;

import org.siprop.bullet.RigidBody;
import org.siprop.bullet.interfaces.Constraint;
import org.siprop.bullet.util.Axis3;
import org.siprop.bullet.util.ConstraintType;
import org.siprop.bullet.util.Pivot3;

public class HingeConstraint implements Constraint {
	
	private static final int type = ConstraintType.HINGE_CONSTRAINT_TYPE;

	
	public final RigidBody rbA;
	public final RigidBody rbB;
	public final Pivot3 pivotInA;
	public final Pivot3 pivotInB;	
	public final Axis3 axisInA;
	public final Axis3 axisInB;	
	
	public HingeConstraint(RigidBody rbA,
			Pivot3 pivotInA,
			Axis3 axisInA) {		
		this.rbA = rbA;
		this.pivotInA = pivotInA;
		this.axisInA = axisInA;
		
		this.pivotInB = null;
		this.rbB = null;
		this.axisInB = null;
	}

	public HingeConstraint(RigidBody rbA, 
							RigidBody rbB,
							Pivot3 pivotInA,
							Pivot3 pivotInB,
							Axis3 axisInA,
							Axis3 axisInB) {
		this.rbA = rbA;
		this.rbB = rbB;
		this.pivotInA = pivotInA;
		this.pivotInB = pivotInB;
		this.axisInA = axisInA;
		this.axisInB = axisInB;
	}

	
	@Override
	public int getType() {
		return type;
	}

}
