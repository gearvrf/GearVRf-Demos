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
package com.samsung.accessibility.shortcut;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import com.samsung.accessibility.shortcut.ShortcutMenuItem.TypeItem;
import com.samsung.accessibility.util.AccessibilityTexture;

public class ShortcutMenu extends GVRSceneObject {

    private GVRContext mGvrContext;
    private static final int LOST_FOCUS_COLOR = 6186095;
    private static final int CLICKED_COLOR = 12631476;
    private List<ShortcutMenuItem> shortcutItems;
    private ShortcutMenuItem sortAux;
    private AccessibilityTexture textures;

    public ShortcutMenu(GVRContext gvrContext) {
        super(gvrContext);
        this.getTransform().rotateByAxis(110, 0, 1, 0);
        shortcutItems = new ArrayList<ShortcutMenuItem>();
        textures = AccessibilityTexture.getInstance(gvrContext);
        mGvrContext = gvrContext;
        createDefaultMenu();
        sortAux = new ShortcutMenuItem(mGvrContext);
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
            shortcutItem.createIcon(textures.getEmptyIcon(), TypeItem.EMPTY);
            shortcutItems.add(shortcutItem);
        }
    }

    public List<ShortcutMenuItem> getShortcutItems() {
        return shortcutItems;
    }

    public void removeShortcut(int... position) {

        for (int i = 0; i < position.length; i++) {
            shortcutItems.get(position[i]).removeIcon();
        }

        sort(position[0]);
    }

    private void sort(int position) {
        for (int i = position + 1; i < shortcutItems.size(); i++) {
            if (shortcutItems.get(i).getTypeItem() != TypeItem.EMPTY && shortcutItems.get(i).getTypeItem() != shortcutItems.get(i - 2).getTypeItem()) {
                for (int j = 0; j < i; j++) {
                    if (shortcutItems.get(j).getTypeItem() == TypeItem.EMPTY) {
                        shortcutItems.get(j).createIcon(shortcutItems.get(i).getIcon().getRenderData().getMaterial().getMainTexture(),
                                shortcutItems.get(i).getTypeItem());
                        shortcutItems.get(i).removeIcon();

                        if (shortcutItems.get(j).getTypeItem() == shortcutItems.get(j - 1).getTypeItem()) {
                            switchItems(j, j + 1);
                        } else if (j < shortcutItems.size() - 2 && j > 1
                                && shortcutItems.get(j + 2).getTypeItem() == shortcutItems.get(j - 2).getTypeItem()) {
                            switchItems(j, j + 2);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void switchItems(int position1, int position2) {
        sortAux.createIcon(shortcutItems.get(position1).getIcon().getRenderData().getMaterial().getMainTexture(),
                shortcutItems.get(position1).getTypeItem());
        shortcutItems.get(position1).createIcon(shortcutItems.get(position2).getIcon().getRenderData().getMaterial().getMainTexture(),
                shortcutItems.get(position2).getTypeItem());
        shortcutItems.get(position2).createIcon(sortAux.getIcon().getRenderData().getMaterial().getMainTexture(),
                sortAux.getTypeItem());
    }
}
