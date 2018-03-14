package com.example.org.gvrfapplication;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;

/**
 * Created by nite.luo on 3/2/2018.
 */

public class RotateBehavior extends GVRBehavior {

    private static final long TYPE_Rotate_behavior = newComponentType(RotateBehavior.class);

    float mRotationSpeed = 1.0f;

    protected RotateBehavior(GVRContext gvrContext) {
        super(gvrContext);
        mType = TYPE_Rotate_behavior;
    }

    public static long getComponentType(){ return TYPE_Rotate_behavior;}

    @Override
    public void onDrawFrame(float frameTime) {
        super.onDrawFrame(frameTime);

        getOwnerObject().getTransform().rotateByAxis(mRotationSpeed, 0,1,0);
    }
}
