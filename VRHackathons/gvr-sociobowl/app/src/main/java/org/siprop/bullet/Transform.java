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

import org.siprop.bullet.util.Matrix3x3;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.Quaternion;

public class Transform {
	
	public Point3 originPoint;
	public Matrix3x3 basis;
	public Quaternion rotation;
	public Point3 invXform;
	
	public float[] option_param = new float[9];
	
	public Transform(Point3 originPoint) {
		this.originPoint = originPoint;
	}
	public Transform(Point3 originPoint, Matrix3x3 basis) {
		this.originPoint = originPoint;
		this.basis = basis;
	}
	public Transform(Point3 originPoint, Matrix3x3 basis, Quaternion rotation) {
		this.originPoint = originPoint;
		this.basis = basis;
		this.rotation = rotation;
	}
	public Transform(Point3 originPoint, Matrix3x3 basis, Quaternion rotation, Point3 invXform) {
		this.originPoint = originPoint;
		this.basis = basis;
		this.rotation = rotation;
		this.invXform = invXform;
	}
	
}
