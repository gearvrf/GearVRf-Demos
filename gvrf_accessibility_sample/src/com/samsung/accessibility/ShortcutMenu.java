package com.samsung.accessibility;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.accessibility.GVRAccessibilityManager;

import com.samsung.accessibility.focus.OnClickListener;

public class ShortcutMenu extends GVRSceneObject {

    private GVRContext mGvrContext;
    private static final int LOST_FOCUS_COLOR = 6186095;
    private static final int CLICKED_COLOR = 12631476;
    private List<ShortcutMenuItem> shortcutItems;

    public ShortcutMenu(GVRContext gvrContext) {
        super(gvrContext);
        this.getTransform().rotateByAxis(20, 0, 1, 0);
        shortcutItems = new ArrayList<ShortcutMenuItem>();
        mGvrContext = gvrContext;
        createDefaultMenu();

    }

    public void createDefaultMenu() {
        int offset = 2;
        float angle = 45;
        int size = 8;
        
        for (int i = 0; i < size; i++) {
            offset++;
            ShortcutMenuItem shortcutItem = new ShortcutMenuItem(mGvrContext);
            shortcutItem.getTransform().setPosition(0, -1f, 0);
            shortcutItem.getTransform().rotateByAxis(offset * angle, 0, 1, 0);
            shortcutItem.setEmpty(true);
            addChildObject(shortcutItem);
            shortcutItems.add(shortcutItem);
        }

    }

    public void clickEffectMenu(ShortcutMenuItem shortcutItem) {
        if (!shortcutItem.isClicked()) {
            shortcutItem.setClicked(true);
            shortcutItem.getRenderData().getMaterial().setColor(CLICKED_COLOR);
        } else {
            shortcutItem.setClicked(false);
            shortcutItem.getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);
        }
    }

    public List<ShortcutMenuItem> getMenuItems() {
        return shortcutItems;
    }

}
