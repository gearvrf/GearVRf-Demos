package org.gearvrf.immersivepedia.focus;

import android.view.MotionEvent;

import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.immersivepedia.GazeController;
import org.gearvrf.utility.Log;

public class PickHandler extends GVREventListeners.PickEvents
{
    public GVRSceneObject PickedObject = null;

    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
    {
        if (sceneObj instanceof FocusableSceneObject)
        {
            FocusableSceneObject fo = (FocusableSceneObject) sceneObj;
            PickedObject = fo;
            Log.v("PICKER", sceneObj.getName() + " onEnter");
            fo.setFocus(true);
            fo.dispatchInFocus();
            fo.hitLocation = pickInfo.getHitLocation();
        }
    }

    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
        if (sceneObj instanceof FocusableSceneObject)
        {
            FocusableSceneObject fo = (FocusableSceneObject) sceneObj;
            fo.dispatchInFocus();
        }
    }

    public void onExit(GVRSceneObject sceneObj)
    {
        FocusableSceneObject fo = (FocusableSceneObject) PickedObject;
        if (fo != null)
        {
            Log.v("PICKER", fo.getName() + " onExit");
            fo.setFocus(false);
        }
    }

    public void onNoPick(GVRPicker picker)
    {
        FocusableSceneObject fo = (FocusableSceneObject) PickedObject;
        if (fo != null)
        {
            fo.setFocus(false);
            Log.v("PICKER", fo.getName() + " onNoPick");
        }
        PickedObject = null;
        GazeController.get().disableInteractiveCursor();
    }


}