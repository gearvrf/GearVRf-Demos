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


package org.gearvrf.video.shaders;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class ScreenShader extends GVRShader {

    private static final String VERTEX_SHADER = "" //
            + "attribute vec3 a_position;\n"
            + "attribute vec2 a_texcoord;\n" //
            + "@MATRIX_UNIFORMS\n"
            + "varying vec2 v_tex_coord;\n" //
            + "void main() {\n" //
            + "  v_tex_coord = a_texcoord.xy;\n"
            + "  gl_Position = u_mvp * vec4(a_position,1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "#extension GL_OES_EGL_image_external_essl3 : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES u_screen;\n"
            + "varying vec2 v_tex_coord;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(u_screen, v_tex_coord);\n" //
            + "}\n";

    public ScreenShader(GVRContext gvrContext) {
        super("", "samplerExternalOES u_screen", "float3 a_position, float3 a_normal, float2 a_texcoord", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setFloat("u_weight", 1);
        material.setFloat("u_fade", 1);
    }
}