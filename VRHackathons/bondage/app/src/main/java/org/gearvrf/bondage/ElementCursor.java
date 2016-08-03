package org.gearvrf.bondage;

import android.view.GestureDetector;
import android.view.MotionEvent;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

public class ElementCursor extends GVRBehavior
{
    static private long TYPE_ELEMENT_CURSOR = newComponentType(ElementCursor.class);
    static public long getComponentType() { return TYPE_ELEMENT_CURSOR; }

    class MoveCursor extends GestureDetector.SimpleOnGestureListener
    {
        public boolean onSingleTapUp(MotionEvent ev) { return true; }
        public void onShowPress(MotionEvent ev)  {}
        public void onLongPress(MotionEvent ev)  {}
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            GVRSceneObject owner = getOwnerObject();

            if ((owner != null) && owner.isEnabled())
            {
                float zcur = owner.getTransform().getPositionZ();
                float z = distanceX;
                if (Math.abs(z) > 50.0f)
                {
                    return true;
                }
                z = z / 200.0f;
                zcur += z;
                if (Math.abs(zcur) > 0.05f)
                {
                    owner.getTransform().setPositionZ(zcur);
                }
             }
            return true;
        }
        public boolean onDown(MotionEvent ev)  { return true; }
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return true; }
    }

    private GestureDetector mGestureDetector;
    private MoveCursor mGestureListener;

    public ElementCursor(GVRContext ctx)
    {
        super(ctx);
        mType = TYPE_ELEMENT_CURSOR;
        mGestureListener = new MoveCursor();
    }

    public void onAttach(GVRSceneObject sceneObj)
    {
        mGestureDetector = new GestureDetector(sceneObj.getGVRContext().getActivity(), mGestureListener);
    }

    public void onDetach(GVRSceneObject sceneObj)
    {
        mGestureDetector = null;
    }

    public boolean onTouchEvent(MotionEvent ev)
    {
        if (mGestureDetector != null)
        {
            return mGestureDetector.onTouchEvent(ev);
        }
        return false;
    }
}
