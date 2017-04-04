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


package org.gearvrf.gvrposteffect;

import org.gearvrf.GVRContext;

public class BlurShader extends org.gearvrf.GVRShaderTemplate
{
    private static final String VERTEX_SHADER =
              "in vec3 a_position;\n"
            + "in vec2 a_texcoord;\n"
            + "out vec2 vTextureCoord;\n"
            + "uniform mat4 u_mvp;\n"
            + "void main() {\n"
            + "  vec4 pos = vec4(a_position, 1.0);\n"
            + "  vTextureCoord = a_texcoord;\n"
            + "  gl_Position = u_mvp * pos;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external_essl3 : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES main_texture;\n"
            + "in vec2 vTextureCoord;\n"
            + "out vec4 outColor;\n"
            + "vec3 mosaic(vec2 position) {\n"
            + "    vec2 p = floor(position) / 10.;\n"
            + "    return texture(main_texture, p).rgb;\n"
            + "}\n"
            + "vec2 sw(vec2 p) {return vec2( floor(p.x) , floor(p.y) );}\n"
            + "vec2 se(vec2 p) {return vec2( ceil(p.x) , floor(p.y) );}\n"
            + "vec2 nw(vec2 p) {return vec2( floor(p.x) , ceil(p.y) );}\n"
            + "vec2 ne(vec2 p) {return vec2( ceil(p.x) , ceil(p.y) );}\n"
            + "vec3 blur(vec2 p) {\n"
            + "vec2 inter = smoothstep(0., 1., fract(p));\n"
            + "vec3 s = mix(mosaic(sw(p)), mosaic(se(p)), inter.x);\n"
            + "vec3 n = mix(mosaic(nw(p)), mosaic(ne(p)), inter.x);\n"
            + "return mix(s, n, inter.y);\n"
            + "}\n"
            + "void main() {\n"
            + "  outColor = vec4(blur(vTextureCoord * 10.), 1.0);\n"
            + "}\n";

    public BlurShader(GVRContext gvrcontext)
    {
        super("", 300);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }
}

