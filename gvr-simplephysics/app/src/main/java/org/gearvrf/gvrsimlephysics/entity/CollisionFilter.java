package org.gearvrf.gvrsimlephysics.entity;

import org.gearvrf.physics.GVRCollisionType;

/**
 * Created by d.alipio@samsung.com on 11/4/16.
 */

public class CollisionFilter {

    public static GVRCollisionType GROUND_ID;
    public static GVRCollisionType CYLINDER_ID;
    public static GVRCollisionType BALL_ID;
    public static GVRCollisionType INVISIBLE_GROUND_ID;

    static {
        GROUND_ID = new GVRCollisionType((short) 1);
        CYLINDER_ID = new GVRCollisionType((short) 2);
        BALL_ID = new GVRCollisionType((short) 3);
        INVISIBLE_GROUND_ID = new GVRCollisionType((short) 4);

        BALL_ID.colideWith(CYLINDER_ID);
        BALL_ID.colideWith(GROUND_ID);
        BALL_ID.colideWith(BALL_ID);

        GROUND_ID.colideWith(BALL_ID);
        GROUND_ID.colideWith(CYLINDER_ID);

        CYLINDER_ID.colideWith(BALL_ID);
        CYLINDER_ID.colideWith(INVISIBLE_GROUND_ID);
        CYLINDER_ID.colideWith(GROUND_ID);
        CYLINDER_ID.colideWith(CYLINDER_ID);

    }
}
