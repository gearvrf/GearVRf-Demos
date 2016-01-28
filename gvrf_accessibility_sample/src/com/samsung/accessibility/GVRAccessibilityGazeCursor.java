
package com.samsung.accessibility;

import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRGazeCursorSceneObject;

import java.util.ArrayList;

public class GVRAccessibilityGazeCursor {
    private GVRGazeCursorSceneObject gazeCursor;
    private ArrayList<GVRTexture> customCursorTextures = new ArrayList<GVRTexture>();

    protected GVRAccessibilityGazeCursor(GVRGazeCursorSceneObject cursor) {
        gazeCursor = cursor;
    }

    public void addCustomCursorTexture(GVRTexture cursorTexture) {
        customCursorTextures.add(cursorTexture);
    }

    public void removeCustomCursorTexture(GVRTexture cursorTexture) {
        // cursorTexture.getRenderData().getMaterial().getMainTexture();
        customCursorTextures.remove(cursorTexture);
    }

    public ArrayList<GVRTexture> getCustomCursorTextures() {
        return customCursorTextures;
    }

    public void selectCursor() {
        if (mAccessibilityItem.isActive) {

        }
    }

}
