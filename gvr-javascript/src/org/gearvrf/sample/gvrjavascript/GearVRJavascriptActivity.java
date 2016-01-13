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

package org.gearvrf.sample.gvrjavascript;

import java.io.IOException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRScript;
import org.gearvrf.script.GVRScriptFile;
import org.gearvrf.script.GVRScriptManager;

import android.os.Bundle;

public class GearVRJavascriptActivity extends GVRActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Instantiate your script class
        // Note: you could just use GVRScript if everything is in lua script
        GVRScript script = new GearVRJavascriptScript();
        setScript(script, "gvr.xml");
 
        GVRScriptManager sm = getGVRContext().getScriptManager();

        // Add utils for scripts
        sm.addVariable("utils", new ScriptUtils());

        // Attach script file
        GVRScriptFile scriptFile;
        try {
            scriptFile = sm.loadScript(
                    new GVRAndroidResource(getGVRContext(), "script.js"),
                    GVRScriptManager.LANG_JAVASCRIPT);
            sm.attachScriptFile(script, scriptFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
