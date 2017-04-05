package org.gearvrf.immersivepedia.focus;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.immersivepedia.GazeController;
import org.gearvrf.immersivepedia.Main;
import org.gearvrf.utility.Log;

public class PickHandler implements IPickEvents
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
        FocusableController.process(sceneObj);
    }
    public void onPick(GVRPicker picker) { }

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
        GazeController.disableInteractiveCursor();
    }


}