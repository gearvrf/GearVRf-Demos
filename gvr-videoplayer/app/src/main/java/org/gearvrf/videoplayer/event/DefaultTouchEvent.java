package org.gearvrf.videoplayer.event;

import android.view.MotionEvent;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;

public class DefaultTouchEvent implements ITouchEvents {

    @Override
    public void onEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onTouchEnd(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onInside(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
    }

    @Override
    public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {
    }
}
