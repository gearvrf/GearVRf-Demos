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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessiblityObject;

public class FocusableSceneObject extends GVRAccessiblityObject {

    private boolean focus = false;
    private OnFocusListener onFocusListener = null;
    public String tag = null;
    public boolean showInteractiveCursor = true;
    private OnClickListener onClickListener;
    private int focusCount = 0;

    public FocusableSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        FocusableController.interactiveObjects.add(this);
    }

    public FocusableSceneObject(GVRContext gvrContext, GVRMesh gvrMesh,
            GVRTexture gvrTexture) {
        super(gvrContext, gvrMesh, gvrTexture);
        FocusableController.interactiveObjects.add(this);
    }

    public FocusableSceneObject(GVRContext gvrContext, float width,
            float height, GVRTexture t) {
        super(gvrContext, width, height, t);
        FocusableController.interactiveObjects.add(this);
    }

    public void dispatchGainedFocus() {
        if (this.onFocusListener != null) {
            this.onFocusListener.gainedFocus(this);
        }
        if (showInteractiveCursor) {
            // GazeController.enableInteractiveCursor();
        }
    }

    public void dispatchLostFocus() {
        if (this.onFocusListener != null) {
            onFocusListener.lostFocus(this);
            focusCount = 0;
        }
        if (showInteractiveCursor) {
            // GazeController.disableInteractiveCursor();
        }
    }

    public void setFocus(boolean state) {
        if (state == true && focus == false && focusCount > 1) {
            focus = true;
            this.dispatchGainedFocus();
            return;
        }

        if (state == false && focus == true) {
            focus = false;
            this.dispatchLostFocus();
            return;
        }
    }

    public void dispatchInFocus() {
        if (this.onFocusListener != null) {
            if (focusCount > 1)
                this.onFocusListener.inFocus(this);
            if (focusCount <= 2)
                focusCount++;
        }
        if (showInteractiveCursor) {
            // GazeController.enableInteractiveCursor();
        }
    }

    public void dispatchInClick() {
        if (this.onClickListener != null) {
            this.onClickListener.onClick();
        }

    }

    public boolean hasFocus() {
        return focus;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnFocusListener(OnFocusListener onFocusListener) {
        this.onFocusListener = onFocusListener;
    }

}
