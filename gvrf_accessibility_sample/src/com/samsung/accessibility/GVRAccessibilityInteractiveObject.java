package com.samsung.accessibility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRTexture;

import com.samsung.accessibility.focus.FocusableSceneObject;

class GVRAccessibilityInteractiveObject extends FocusableSceneObject {

    private GVRAccessibilityOnAction mOnAction;

    public GVRAccessibilityInteractiveObject(GVRContext gvrContext, GVRMesh mesh, GVRTexture texture) {
        super(gvrContext, mesh, texture);
    }

    public GVRAccessibilityInteractiveObject(GVRContext gvrContext) {
        super(gvrContext);
    }

    public void interact() {
        if (mOnAction != null)
            mOnAction.setOnAction();
    }

    public void setOnAction(GVRAccessibilityOnAction onAction) {
        this.mOnAction = onAction;
    }

}
