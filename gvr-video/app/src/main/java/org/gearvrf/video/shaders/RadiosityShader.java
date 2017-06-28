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

public class RadiosityShader extends GVRShaderTemplate{

    public static final String TEXTURE_OFF_KEY = "u_texture_off";
    public static final String TEXTURE_ON_KEY = "u_texture_on";
    public static final String SCREEN_KEY = "u_screen";
    public static final String WEIGHT_KEY = "u_weight";
    public static final String FADE_KEY = "u_fade";
    public static final String LIGHT_KEY = "u_lightness";

    private static final String VERTEX_SHADER = "" //
            + "#extension GL_OES_EGL_image_external : enable\n"
            + "#extension GL_OES_EGL_image_external_essl3 : enable\n"
            + "precision highp float;\n"
            + "attribute vec4 a_position;\n"
            + "attribute vec3 a_normal;\n"
            + "attribute vec2 a_texcoord;\n"
            + "uniform mat4 u_mvp;\n"
            + "uniform samplerExternalOES u_screen;\n"
            + "varying float depth;\n"
            + "varying vec2 v_tex_coord;\n"
            + "varying vec3 v_screen_color;\n"
            + "void main() {\n"
            + "  v_tex_coord = a_texcoord.xy;\n"
            + "  vec2 uv = vec2( a_normal.x*0.5, 1.0-a_normal.y );\n"
            + "  depth = a_normal.z;\n"
            + "  float u_kernel = 12.0;\n"
            + "  float v_kernel =  6.0;\n"
            + "  float u0 = floor(u_kernel*uv.x)/u_kernel;\n"
            + "  float v0 = floor(v_kernel*uv.y)/v_kernel;\n"
            + "  v_screen_color = vec3(0.0);\n"
            + "  for(int i=-1; i<=1; i++) for(int j=-1; j<=1; j++)  v_screen_color += texture2D( u_screen, vec2(u0,v0) + vec2(float(i)/u_kernel, float(j)/v_kernel) ).rgb;\n"
            + "  v_screen_color /= 9.0;\n"
            + "  gl_Position = u_mvp * a_position;\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "precision highp float;\n"
            + "uniform sampler2D u_texture_off;\n"
            + "uniform sampler2D u_texture_on;\n"
            + "uniform float u_weight;\n"
            + "uniform float u_fade;\n"
            + "uniform float u_lightness;\n"
            + "varying float depth;\n"
            + "varying vec2 v_tex_coord;\n"
            + "varying vec3 v_screen_color;\n"
            + "void main() {\n"
            + "  vec3 color1 = vec3(0.0);\n"
            + "  vec3 color2 = vec3(0.0);\n"
            + "  vec3 bg = texture2D(u_texture_off, v_tex_coord).rgb;\n"
            + "  if( u_weight < 0.999 ) { color1 = bg*(u_lightness*(1.0-0.4*depth)*v_screen_color); }\n"
            + "  if( u_weight > 0.001 ) { color2 = bg*0.15; }\n"
            + "  float alpha = min( 1.0, 2.0 - u_weight );\n"
            + "  gl_FragColor = vec4( u_fade*(color1*(1.0-u_weight)+color2*u_weight), alpha);\n"
            + "}\n";

    public RadiosityShader(GVRContext gvrContext) {
        super("float u_weight, float u_fade, float u_lightness, samplerExternalOES u_screen sampler2D u_texture_off");
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }


}
