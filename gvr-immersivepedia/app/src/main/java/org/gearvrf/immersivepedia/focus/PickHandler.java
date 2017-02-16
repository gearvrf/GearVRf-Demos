package org.gearvrf.immersivepedia.focus;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.utility.Log;

public class PickHandler implements IPickEvents
{
    public GVRSceneObject PickedObject = null;

    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    public void onExit(GVRSceneObject sceneObj) { }
    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    public void onNoPick(GVRPicker picker)
    {
        PickedObject = null;
    }
    public void onPick(GVRPicker picker)
    {
        GVRPicker.GVRPickedObject picked = picker.getPicked()[0];
        PickedObject = picked.hitObject;

        if(PickedObject != null)
            Log.d("Abhi", "picked object");
    }
}