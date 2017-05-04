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

package org.gearvrf.vuforiasample;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderTemplate;

public class ModelShader extends GVRShaderTemplate{

    public static final String TEXTURE_KEY = "texSampler2D";
    public static final String MVP_KEY = "modelViewProjectionMatrix";

    private static final String VERTEX_SHADER = "" //
            + "attribute vec4 a_position;\n"
            + "attribute vec4 a_normal;\n"
            + "attribute vec2 a_texcoord;\n"
            + "varying vec2 v_tex_coord;\n"
            + "uniform mat4 u_mvp;\n"
            + "uniform mat4 modelViewProjectionMatrix;\n"
            + "void main() {\n"
            + "  v_tex_coord = a_texcoord.xy;\n"
            + "  gl_Position = modelViewProjectionMatrix * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision highp float;\n"
            + "varying vec2 v_tex_coord; \n"
            + "uniform sampler2D texSampler2D;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(texSampler2D, v_tex_coord);\n"
            + "}\n";

    public ModelShader(GVRContext gvrContext) {
        super("mat4 modelViewProjectionMatrix, sampler2D texSampler2D");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
