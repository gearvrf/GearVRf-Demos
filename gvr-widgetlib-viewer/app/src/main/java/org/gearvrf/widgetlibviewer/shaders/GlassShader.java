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


package org.gearvrf.widgetlibviewer.shaders;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderData;

public class GlassShader extends GVRShader {

    public static final String COLOR_KEY = "u_color";
    public static final String LIGHT_KEY = "u_light";
    public static final String EYE_KEY = "u_eye";
    public static final String RADIUS_KEY = "u_radius";
    public static final String TEXTURE_KEY = "u_texture";

    private static final String VERTEX_SHADER = "" //
            + "#extension GL_ARB_separate_shader_objects : enable\n"
            + "#extension GL_ARB_shading_language_420pack : enable\n"
            + "precision mediump float;\n"
            + "layout(location = 0) in vec3 a_position;\n"
            + "layout(location = 2) in vec3 a_normal;\n" //
            + "layout(location = 1) in vec2 a_texcoord;\n"
            + "@MATRIX_UNIFORMS\n"
            + "@MATERIAL_UNIFORMS\n"
            + "layout(location = 0) out vec3 normal;\n"
            + "layout(location = 1) out vec3 view;\n" //
            + "layout(location = 2) out vec3 light;\n"
            + "layout(location = 3) out vec3 p;\n" //
            + "layout(location = 4) out vec2 coord;\n"
            + "void main() {\n"
            + "  normal = a_normal;\n" //
            + "  view  = vec3(u_eye.x, u_eye.y, u_eye.z) - a_position.xyz;\n"
            + "  light = vec3(u_light.x, u_light.y, u_light.z) - a_position.xyz;\n"
            + "  p = a_position.xyz;\n" //
            + "  coord = a_texcoord;\n"
            + "  gl_Position = u_mvp * vec4(a_position,1.0);\n" //
            + "}\n";

    private static final String FRAGMENT_SHADER = "" //
            + "#extension GL_ARB_separate_shader_objects : enable\n"
            + "#extension GL_ARB_shading_language_420pack : enable\n"
            + "precision mediump float;\n"
            + "layout(location = 0) in vec3 normal;\n"
            + "layout(location = 1) in vec3 view;\n" //
            + "layout(location = 2) in vec3 light;\n"
            + "layout(location = 3) in vec3 p;\n" //
            + "layout(location = 4) in vec2 coord;\n"
            + "@MATERIAL_UNIFORMS\n"
            + "layout(set = 0, binding = 4) uniform sampler2D u_texture;\n" //
            + "layout(location = 0) out vec4 FragColor;\n"

            + "void main() {\n"
            + "  vec3  v = normalize(view);\n"
            + "  vec3  l = normalize(light);\n"
            + "  vec3  n = normalize(normal);\n"
            + "  vec3  r = normalize(reflect(v,n));\n"
            + "  float b =-dot(r,p);\n"
            + "  float c = dot(p,p)-u_radius*u_radius;\n"
            + "  float t = sqrt(b*b-c);\n"
            + "  if( -b + t > 0.0 ) t = -b + t;\n"
            + "  else               t = -b - t;\n"
            + "  vec3  target = normalize(p+t*r);\n" //
            + "  float u;\n"
            + "  if( target.x > 0.0 ) u =  target.z + 3.0;\n"
            + "  else                 u = -target.z + 1.0;\n"
            + "  vec2 uv = vec2( u/4.0, 0.5*target.y + 0.5 );\n"
            + "  vec3 color = texture(u_texture, uv).rgb;\n"
            + "  vec3  h = normalize(v+l);\n"
            + "  float diffuse  = max ( dot(l,n), 0.1 );\n"
            + "  float specular = max ( dot(h,n), 0.0 );\n"
            + "  specular = pow (specular, 300.0);\n" //
            + "  color *= diffuse;\n" //
            + "  color *= u_color.rgb;\n"
            + "  color += 0.5*(1.0- color)*specular;\n"
            + "  FragColor = vec4( color, 0.4 );\n" //
            + "}\n";

    public GlassShader(GVRContext gvrContext) {
        super("float3 u_eye, float3 u_light, float4 u_color, float u_radius ", "sampler2D u_texture", "float3 a_position, float2 a_texcoord, float3 a_normal", GLSLESVersion.VULKAN);
        setSegment("FragmentTemplate", FRAGMENT_SHADER);
        setSegment("VertexTemplate", VERTEX_SHADER);
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec4("u_color", 1, 1, 1, 1);
        material.setVec3("u_light", 1, 1, 1);
        material.setVec3("u_eye", 0, 0, 0);
        material.setFloat("u_radius", 1);
    }
}
