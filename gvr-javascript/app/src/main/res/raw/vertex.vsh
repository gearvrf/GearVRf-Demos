// Copyright 2015 Samsung Electronics Co., LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

precision highp float;
@MATRIX_UNIFORMS

layout(location = 0) in vec4 a_position;

void main() {
    #ifdef HAS_MULTIVIEW
       bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
       mat4 mvp = u_mvp_[gl_ViewID_OVR];
       gl_Position = mvp  * vec4(a_position);
    #else
        gl_Position = u_mvp * a_position;
    #endif
}
