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
import org.gearvrf.GVRTexture;

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
        float angleInc = 45;
        float angle = 0;
        int size = 8;

        for (int i = 0; i < size; i++) {
            ShortcutMenuItem shortcutItem = new ShortcutMenuItem(mGvrContext);
            shortcutItem.getTransform().setPosition(0, -1f, 0);
            shortcutItem.getTransform().rotateByAxis(angle, 0, 1, 0);
            addChildObject(shortcutItem);
            shortcutItem.setTypeItem(TypeItem.EMPTY);
            shortcutItem.createIcon(textures.getEmptyIcon(), TypeItem.EMPTY);
            shortcutItems.add(shortcutItem);
            angle += angleInc;
        }
    }
    
    public void addShortcut(TypeItem type, GVRTexture iconTexture) {
        for (int i = 0; i < shortcutItems.size(); i++) {
            if (shortcutItems.get(i).getTypeItem() == TypeItem.EMPTY) {
                shortcutItems.get(i).createIcon(iconTexture, type);
                break;
            }
        }
    }

    public ShortcutMenuItem removeShortcut(TypeItem type) {
        int i = -1;
        int shuffle = -1;
        ShortcutMenuItem removed = null;
        
        while (++i < shortcutItems.size()) {
            ShortcutMenuItem shortcut = shortcutItems.get(i);
            if (shortcut.getTypeItem() == type) {
                shortcut.removeIcon();
                if (shuffle < 0) {
                    shuffle = i;
                    removed = shortcut;
                }
            }
            else if (shortcut.getTypeItem() == TypeItem.EMPTY)
                break;
            else if (shuffle >= 0) {
                shortcutItems.get(shuffle).createIcon(shortcut.getIconTexture(), shortcut.getTypeItem());
                shortcut.removeIcon();
                ++shuffle;               
            }
        }
        return removed;
    }
    
    public List<ShortcutMenuItem> getShortcutItems() {
        return shortcutItems;
    }

}
