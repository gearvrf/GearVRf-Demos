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
package org.siprop.bullet.util;

public class Matrix3x3 {
	
	public float xx;
	public float xy;
	public float xz;

	public float yx;
	public float yy;
	public float yz;
	
	public float zx;
	public float zy;
	public float zz;
	
	public Matrix3x3() {
		
	}
	public Matrix3x3(float xx, float xy, float xz,
					 float yx, float yy, float yz,
					 float zx, float zy, float zz) {
		this.xx = xx;
		this.xy = xy;
		this.xz = xz;
		this.yx = yx;
		this.yy = yy;
		this.yz = yz;
		this.zx = zx;
		this.zy = zy;
		this.zz = zz;
	}
	
	public Matrix3x3(float[] mat) {
		if(mat.length >= 9) {
			this.xx = mat[0];
			this.xy = mat[1];
			this.xz = mat[2];
			this.yx = mat[3];
			this.yy = mat[4];
			this.yz = mat[5];
			this.zx = mat[6];
			this.zy = mat[7];
			this.zz = mat[8];
		}
	}
}
