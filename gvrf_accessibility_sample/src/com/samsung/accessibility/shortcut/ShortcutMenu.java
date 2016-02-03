package com.samsung.accessibility.shortcut;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

public class ShortcutMenu extends GVRSceneObject {

    private GVRContext mGvrContext;
    private static final int LOST_FOCUS_COLOR = 6186095;
    private static final int CLICKED_COLOR = 12631476;
    private List<ShortcutMenuItem> shortcutItems;

    public ShortcutMenu(GVRContext gvrContext) {
        super(gvrContext);
        this.getTransform().rotateByAxis(110, 0, 1, 0);
        shortcutItems = new ArrayList<ShortcutMenuItem>();
        mGvrContext = gvrContext;
        createDefaultMenu();

    }

    public void createDefaultMenu() {
        int offset = 0;
        float angle = 45;
        int size = 8;

        for (int i = 0; i < size; i++) {
            ShortcutMenuItem shortcutItem = new ShortcutMenuItem(mGvrContext);
            shortcutItem.getTransform().setPosition(0, -1f, 0);
            if (i % 2 == 1) {
                shortcutItem.getTransform().rotateByAxis((int) (offset + 1 / 2) * angle, 0, 1, 0);
            } else {
                shortcutItem.getTransform().rotateByAxis((int) (offset + 1 / 2) * -angle, 0, 1, 0);
                offset++;
            }

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

    public List<ShortcutMenuItem> getShortcutItems() {
        return shortcutItems;
    }

}
