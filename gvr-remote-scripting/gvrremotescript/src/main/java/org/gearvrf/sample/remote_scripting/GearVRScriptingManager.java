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

package org.gearvrf.sample.remote_scripting;

import org.gearvrf.GVRScript;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import android.view.Gravity;
import org.gearvrf.script.GVRScriptManager;

public class GearVRScriptingManager extends GVRScript
{

    @Override
    public void onInit(GVRContext gvrContext) {
        gvrContext.startDebugServer();
        GVRScene scene = gvrContext.getNextMainScene();

        // get the ip address
        GearVRScripting activity = (GearVRScripting) gvrContext.getActivity();
        String ipAddress = activity.getIpAddress();
        String telnetString = "telnet " + ipAddress + " 1645";

        // create text object to tell the user where to connect
        GVRTextViewSceneObject textViewSceneObject = new GVRTextViewSceneObject(gvrContext, telnetString);
        textViewSceneObject.setGravity(Gravity.CENTER);
        textViewSceneObject.setTextSize(8);
        textViewSceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // make sure to set a name so we can reference it when we log in
        textViewSceneObject.setName("text");

        // add it to the scene
        scene.addSceneObject(textViewSceneObject);

        // Add display utils for scripts
        GVRScriptManager scriptManager = gvrContext.getScriptManager();
        scriptManager.addVariable("display", new DisplayUtils(gvrContext));
        scriptManager.addVariable("cursor", new CursorUtils(gvrContext));
        scriptManager.addVariable("editor", new EditorUtils(gvrContext));
        scriptManager.addVariable("passthrough", new PassthroughUtils(gvrContext, activity));
        scriptManager.addVariable("filebrowser", new FileBrowserUtils(gvrContext));
    }

    @Override
    public void onStep() {
    }

}
