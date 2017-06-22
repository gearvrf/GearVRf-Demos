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
package com.samsung.accessibility.util;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.accessibility.GVRAccessibilityInvertedColors;
import org.gearvrf.accessibility.GVRAccessibilityTalkBack;
import org.gearvrf.accessibility.GVRAccessibilityZoom;

public class AccessibilityManager {

    private List<GVRAccessibilityTalkBack> mTalkBacks;

    private GVRAccessibilityInvertedColors mInvertedColors;
    private GVRAccessibilityZoom mZoom;

    public AccessibilityManager(GVRContext gvrContext) {
        mTalkBacks = new ArrayList<GVRAccessibilityTalkBack>();
        mInvertedColors = new GVRAccessibilityInvertedColors(gvrContext);
        mZoom = new GVRAccessibilityZoom();
    }

    public List<GVRAccessibilityTalkBack> getTalkBack() {
        return mTalkBacks;
    }

    public GVRAccessibilityInvertedColors getInvertedColors() {
        return mInvertedColors;
    }

    public GVRAccessibilityZoom getZoom() {
        return mZoom;
    }

}
