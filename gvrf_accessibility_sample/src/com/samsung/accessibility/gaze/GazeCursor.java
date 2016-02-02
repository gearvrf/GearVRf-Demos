
package com.samsung.accessibility.gaze;

import org.gearvrf.GVRTexture;

import java.util.ArrayList;

public class GazeCursor {
    private GazeCursorSceneObject gazeCursor;
    private ArrayList<GVRTexture> customCursorTextures = new ArrayList<GVRTexture>();

    protected GazeCursor(GazeCursorSceneObject cursor) {
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
    }

}
