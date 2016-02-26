/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.samsung.accessibility.focus;

import java.util.ArrayList;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;

public final class FocusableController {

    public static ArrayList<FocusableSceneObject> interactiveObjects = new ArrayList<FocusableSceneObject>();

    public static void process(GVRContext context) {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(context
                .getMainScene());

        ArrayList<FocusableSceneObject> needToDisableFocus = new ArrayList<FocusableSceneObject>();

        for (FocusableSceneObject obj : interactiveObjects) {
            needToDisableFocus.add(obj);
        }

        if (eyePointeeHolders.length == 0) {
            // GazeController.disableInteractiveCursor();
        } else {
            for (GVREyePointeeHolder holder : eyePointeeHolders) {
                for (FocusableSceneObject object : interactiveObjects) {
                    if (holder.getOwnerObject().equals(object)) {
                        object.setFocus(true);
                        object.dispatchInFocus();
                        needToDisableFocus.remove(object);
                    }
                }
            }
        }

        for (FocusableSceneObject obj : needToDisableFocus) {
            obj.setFocus(false);
        }

    }

    public static boolean clickProcess(GVRContext context) {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(context
                .getMainScene());

        for (GVREyePointeeHolder holder : eyePointeeHolders) {
            for (FocusableSceneObject object : interactiveObjects) {
                if (holder.getOwnerObject().equals(object)) {
                    object.dispatchInClick();
                    return true;
                }
            }
        }

        return false;
    }

}
