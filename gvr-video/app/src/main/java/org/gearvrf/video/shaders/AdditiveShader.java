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
import org.gearvrf.GVRShaderTemplate;

public class AdditiveShader extends GVRShaderTemplate{

    public static final String TEXTURE_KEY = "texture";
    public static final String WEIGHT_KEY = "weight";
    public static final String FADE_KEY = "fade";

    private static final String VERTEX_SHADER = "" //
            + "precision highp float;\n"
            + "attribute vec4 a_position;\n" //
            + "attribute vec2 a_texcoord;\n"
            + "uniform mat4 u_mvp;\n" //
            + "varying vec2 coord;\n"
            + "void main() {\n" //
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision highp float;\n"
            + "varying vec2  coord;\n" //
            + "uniform sampler2D texture;\n"
            + "uniform float u_weight;\n" //
            + "uniform float u_fade;\n"
            + "void main() {\n"
            + "  vec3 color1 = texture2D(texture, coord).rgb;\n"
            + "  vec3 color2 = vec3(0.0);\n"
            + "  vec3 color  = color1*(1.0-u_weight)+color2*u_weight;\n"
            + "  float alpha = length(color);\n"
            + "  gl_FragColor = vec4( u_fade*color, alpha );\n" //
            + "}\n";

    public AdditiveShader(GVRContext gvrContext) {
        super("float u_weight, float u_fade, sampler2D texture");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

}
