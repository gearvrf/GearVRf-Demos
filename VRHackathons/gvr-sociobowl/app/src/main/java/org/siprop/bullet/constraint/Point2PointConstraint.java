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
import org.siprop.bullet.util.ConstraintType;
import org.siprop.bullet.util.Pivot3;

public class Point2PointConstraint implements Constraint {
	
	private static final int type = ConstraintType.POINT2POINT_CONSTRAINT_TYPE;

	
	public final RigidBody rbA;
	public final RigidBody rbB;
	public final Pivot3 pivotInA;
	public final Pivot3 pivotInB;

	public Point2PointConstraint(RigidBody rbA, Pivot3 pivotInA) {
		this.rbA = rbA;
		this.pivotInA = pivotInA;
		
		this.rbB = null;
		this.pivotInB = null;
	}

	public Point2PointConstraint(RigidBody rbA, RigidBody rbB, Pivot3 pivotInA, Pivot3 pivotInB) {
		this.rbA = rbA;
		this.rbB = rbB;
		this.pivotInA = pivotInA;
		this.pivotInB = pivotInB;
	}
	
	@Override
	public int getType() {
		return type;
	}

}
