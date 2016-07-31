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

public class ShapeType {

	// polyhedral convex shapes
	public static int	BOX_SHAPE_PROXYTYPE = 1;
	public static int 	TRIANGLE_SHAPE_PROXYTYPE = 2;
	public static int	TETRAHEDRAL_SHAPE_PROXYTYPE = 3;
	public static int	CONVEX_TRIANGLEMESH_SHAPE_PROXYTYPE = 4;
	public static int	CONVEX_HULL_SHAPE_PROXYTYPE = 5;
	//implicit convex shapes
//	public static int	IMPLICIT_CONVEX_SHAPES_START_HERE = 6;
	public static int	SPHERE_SHAPE_PROXYTYPE = 7;
	public static int	MULTI_SPHERE_SHAPE_PROXYTYPE = 8;
	public static int	CAPSULE_SHAPE_PROXYTYPE = 9;
	public static int	CONE_SHAPE_PROXYTYPE = 10;
	public static int	CONVEX_SHAPE_PROXYTYPE = 11;
	public static int	CYLINDER_SHAPE_PROXYTYPE = 12;
	public static int	UNIFORM_SCALING_SHAPE_PROXYTYPE = 13;
	public static int	MINKOWSKI_SUM_SHAPE_PROXYTYPE = 14;
	public static int	MINKOWSKI_DIFFERENCE_SHAPE_PROXYTYPE = 14;
	//concave shapes
//	public static int	CONCAVE_SHAPES_START_HERE = 16;
		//keep all the convex shapetype below here, for the check IsConvexShape in broadphase proxy!
	public static int	TRIANGLE_MESH_SHAPE_PROXYTYPE = 17;
		///used for demo integration FAST/Swift collision library and Bullet
//	public static int	FAST_CONCAVE_MESH_PROXYTYPE = 18;
		//terrain
	public static int	TERRAIN_SHAPE_PROXYTYPE = 19;
	///Used for GIMPACT Trimesh integration
	public static int	GIMPACT_SHAPE_PROXYTYPE = 20;
		
	public static int	EMPTY_SHAPE_PROXYTYPE = 21;
	public static int	STATIC_PLANE_PROXYTYPE = 22;
//	public static int	CONCAVE_SHAPES_END_HERE = 23;

	public static int	COMPOUND_SHAPE_PROXYTYPE = 24;

//	public static int	MAX_BROADPHASE_COLLISION_TYPES = 25;
	
}
