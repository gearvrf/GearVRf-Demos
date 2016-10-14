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

package org.gearvrf.util;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.modelviewer2.R;
import org.gearvrf.utility.TextFile;

import android.content.Context;

public class NoTextureShader extends GVRPhongShader {
    private static String surfShader = null;

    public NoTextureShader(GVRContext gvrContext) {
        super(gvrContext);
        if (surfShader == null) {
            Context context = gvrContext.getContext();
            surfShader = TextFile.readTextFile(context, R.raw.notexture_surface);
            setSegment("FragmentSurface", surfShader);
        }
    }
}
