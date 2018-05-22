package org.gearvrf.videoplayer.focus;

import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

public class PickEventHandler extends GVREventListeners.PickEvents {

    private static final String TAG = PickEventHandler.class.getSimpleName();

    @Override
    public void onEnter(GVRSceneObject sceneObject, GVRPicker.GVRPickedObject pickedObject) {

        if (sceneObject instanceof Focusable) {
            Log.d(TAG, "onEnter: " + sceneObject.getName());
            ((Focusable) sceneObject).gainFocus();
        }
    }

    @Override
    public void onExit(GVRSceneObject sceneObject) {

        if (sceneObject instanceof Focusable) {
            Log.d(TAG, "onExit: " + sceneObject.getName());
            ((Focusable) sceneObject).loseFocus();
        }
    }
}