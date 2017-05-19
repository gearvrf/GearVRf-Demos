
/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.controls.shaders;

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.controls.R;
import org.gearvrf.utility.TextFile;

public class TileShader extends GVRShaderTemplate{

    public static final String TEXTURE_KEY = "texture";
    public static final String TILE_COUNT = "tile";

    public TileShader(GVRContext gvrContext) {
        super("float tile, sampler2D texture");
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.tileshader_fragment));
        setSegment("VertexTemplate", TextFile.readTextFile(context,R.raw.tileshader_vertex));

    }
}
