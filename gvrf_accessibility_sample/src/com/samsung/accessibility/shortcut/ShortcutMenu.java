package com.samsung.accessibility.shortcut;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import android.util.Log;

import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;

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

            addChildObject(shortcutItem);
            shortcutItem.setTypeItem(TypeItem.EMPTY);
            shortcutItem.createIcon(shortcutItem.getEmptyIcon(), TypeItem.EMPTY);
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

    public void removeShortcut(int... position) {

        for (int i = 0; i < position.length; i++) {
            Log.e("test", "position: " + position[i]);
            shortcutItems.get(position[i]).removeIcon();
        }

        sort(position[0]);
    }

    private void sort(int position) {

        // for (int i = position; i < shortcutItems.size() - 1; i++) {
        //
        // if (shortcutItems.get(i - 1).getTypeItem() == shortcutItems.get(i + 1).getTypeItem()
        // /* || (i >= 2 && shortcutItems.get(i) == shortcutItems.get(i - 2)) */) {
        //
        // } else {
        // shortcutItems.get(i).createIcon(shortcutItems.get(i + 1).getIcon().getRenderData().getMaterial().getMainTexture(),
        // shortcutItems.get(i + 1).getTypeItem());
        // }
        // }
        // shortcutItems.get(7).removeIcon();
        //
        // // for (int i = 2; i < shortcutItems.size(); i++) {
        // // if (shortcutItems.get(i - 2).getTypeItem() == TypeItem.EMPTY) {
        // // shortcutItems.get(i - 2).createIcon(shortcutItems.get(i).getIcon().getRenderData().getMaterial().getMainTexture(),
        // // shortcutItems.get(i).getTypeItem());
        // // }
        // // }

        // for (int i = 0; i < shortcutItems.size() - 1; i++) {
        // if (shortcutItems.get(i).getTypeItem() == TypeItem.EMPTY) {
        // for (int j = i + 1; j < shortcutItems.size(); j++) {
        // if (shortcutItems.get(j).getTypeItem() != TypeItem.EMPTY) {
        // shortcutItems.get(i).createIcon(shortcutItems.get(j).getIcon().getRenderData().getMaterial().getMainTexture(),
        // shortcutItems.get(j).getTypeItem());
        // if (j + 2 < shortcutItems.size() && shortcutItems.get(j).getTypeItem() == shortcutItems.get(j + 2).getTypeItem()) {
        // shortcutItems.get(j).createIcon(shortcutItems.get(j + 1).getIcon().getRenderData().getMaterial().getMainTexture(),
        // shortcutItems.get(j + 1).getTypeItem());
        // shortcutItems.get(i + 2).createIcon(shortcutItems.get(j + 2).getIcon().getRenderData().getMaterial().getMainTexture(),
        // shortcutItems.get(j + 2).getTypeItem());
        // } else {
        // // shortcutItems.get(j).removeIcon();
        // }
        // break;
        // }
        // }
        // }
        // }

        for (int i = position + 1; i < shortcutItems.size() - 1; i++) {
            if (shortcutItems.get(i).getTypeItem() != TypeItem.EMPTY && shortcutItems.get(i).getTypeItem() != shortcutItems.get(i - 2).getTypeItem()) {
                for (int j = 0; j < i; j++) {
                    if (shortcutItems.get(j).getTypeItem() == TypeItem.EMPTY) {
                        shortcutItems.get(j).createIcon(shortcutItems.get(i).getIcon().getRenderData().getMaterial().getMainTexture(),
                                shortcutItems.get(i).getTypeItem());
                        shortcutItems.get(i).removeIcon();

                        if (shortcutItems.get(j).getTypeItem() == shortcutItems.get(j - 1).getTypeItem()) {
                            shortcutItems.get(j).createIcon(shortcutItems.get(j + 1).getIcon().getRenderData().getMaterial().getMainTexture(),
                                    shortcutItems.get(j + 1).getTypeItem());
                            shortcutItems.get(j + 1).createIcon(shortcutItems.get(j).getIcon().getRenderData().getMaterial().getMainTexture(),
                                    shortcutItems.get(j).getTypeItem());
                        }
                        break;
                    }
                }
            }
        }

        shortcutItems.get(shortcutItems.size() - 1).removeIcon();
    }
}
