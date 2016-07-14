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

import android.view.Gravity;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.debug.DebugServer;

import org.gearvrf.IErrorEvents;

import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.script.GVRScriptManager;

import smcl.samsung.com.debugwebserver.DebugWebServer;

public class GearVRScriptingMain extends GVRMain
{
    private static final String TAG = GearVRScriptingMain.class.getSimpleName();
    private static final int DEBUG_SERVER_PORT = 5000;
    DebugWebServer server;
    private GVRContext gvrContext;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        final DebugServer debug = gvrContext.startDebugServer();
        GVRScene scene = gvrContext.getNextMainScene();
        IErrorEvents errorHandler = new IErrorEvents()
        {
            public void onError(String message, Object source)
            {
                debug.logError(message);
            }
        };
        gvrContext.getEventReceiver().addListener(errorHandler);
        // get the ip address
        GearVRScripting activity = (GearVRScripting) gvrContext.getActivity();
        String ipAddress = activity.getIpAddress();
        String debugUrl = "http://" + ipAddress + ":" + DEBUG_SERVER_PORT;
        String telnetString = "telnet " + ipAddress + " " + DebugServer.DEFAULT_DEBUG_PORT;

        // create text object to tell the user where to connect
        GVRTextViewSceneObject textViewSceneObject = new GVRTextViewSceneObject(gvrContext, 2.0f,
                0.5f, debugUrl + "\n" + telnetString);
        textViewSceneObject.setGravity(Gravity.CENTER);
        textViewSceneObject.setTextSize(5);
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
        scriptManager.addVariable("source", new SourceUtils(gvrContext));

        gvrContext.startDebugServer();
        server = new DebugWebServer(gvrContext);
        server.listen(DEBUG_SERVER_PORT);
    }

    @Override
    public void onStep() {
    }

    public void stop() {
        if(server != null) {
            server.stop();
        }
        if (null != gvrContext){
            gvrContext.stopDebugServer();
        }
    }
}
