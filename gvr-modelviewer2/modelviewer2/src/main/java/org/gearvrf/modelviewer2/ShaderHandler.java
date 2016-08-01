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

package org.gearvrf.modelviewer2;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCustomMaterialShaderId;
import org.gearvrf.GVRMaterialMap;
import org.gearvrf.GVRMaterialShaderManager;

public class ShaderHandler {
    private GVRCustomMaterialShaderId mShaderID;
    private GVRMaterialShaderManager mShaderManager;
    private GVRMaterialMap mCustomShader;

    private static final String VERTEX_SHADER = "uniform mat4 u_mvp;\n"
            + "attribute vec4 a_position;\n"
            + "void main() {\n"
            + "gl_Position = u_mvp * a_position;\n"
            + "}";

    private static final String FRAGMENT_SHADER = "precision highp float;\n"
            + "uniform vec4  u_color;\n"
            + "void main() {\n"
            + "gl_FragColor = vec4(1,1,0,1);\n"
            + "}";


    public ShaderHandler(GVRContext gvrContext){
        mShaderManager = gvrContext.getMaterialShaderManager();
    }

    public void addVSandFS(String vertexShader, String fragmentShader){
        mShaderID = mShaderManager.addShader(VERTEX_SHADER, FRAGMENT_SHADER);
        mCustomShader = mShaderManager.getShaderMap(mShaderID);
        mCustomShader.addUniformVec4Key("u_color", "color");
    }

    public GVRCustomMaterialShaderId getShaderID(){
        return mShaderID;
    }
}
