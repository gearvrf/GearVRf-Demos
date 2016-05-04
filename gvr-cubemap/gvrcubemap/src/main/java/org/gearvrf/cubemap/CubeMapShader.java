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
package org.gearvrf.cubemap;

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.R;
import org.gearvrf.utility.TextFile;

import java.util.HashMap;

public class CubeMapShader extends org.gearvrf.GVRShaderTemplate {
    private static String fragTemplate = null;
    private static String vtxTemplate = null;
    private static String surfaceShader = null;
    private static String addLight = null;
    private static String vtxShader = null;
    private static String normalShader = null;
    private static String skinShader = null;

    public CubeMapShader(GVRContext gvrcontext) {
        super("float4 ambient_color; float4 diffuse_color; float4 specular_color; float4 emissive_color; float specular_exponent");
        if (fragTemplate == null) {
            Context context = gvrcontext.getContext();
            fragTemplate = TextFile.readTextFile(context, R.raw.fragment_template);
            vtxTemplate = TextFile.readTextFile(context, R.raw.vertex_template);
            surfaceShader = TextFile.readTextFile(context, org.gearvrf.cubemap.R.raw.phong_cubemap);
            vtxShader = TextFile.readTextFile(context, org.gearvrf.cubemap.R.raw.vertex_cubemap);
            skinShader = TextFile.readTextFile(context, R.raw.vertexskinning);
            addLight = TextFile.readTextFile(context, R.raw.addlight);
        }
        setSegment("FragmentTemplate", fragTemplate);
        setSegment("VertexTemplate", vtxTemplate);
        setSegment("FragmentSurface", surfaceShader);
        setSegment("FragmentAddLight", addLight);
        setSegment("VertexShader", vtxShader);
        setSegment("VertexSkinShader", skinShader);
    }

    public HashMap<String, Integer> getRenderDefines(GVRRenderData rdata) {
        HashMap<String, Integer> defines = new HashMap<String, Integer>();

        if (!rdata.isLightEnabled())
            defines.put("LIGHTSOURCES", 0);
        return defines;
    }
}

