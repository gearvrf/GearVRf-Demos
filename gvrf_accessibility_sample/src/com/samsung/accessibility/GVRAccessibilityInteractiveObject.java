
package com.samsung.accessibility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

class GVRAccessibilityInteractiveObject extends GVRSceneObject {

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
