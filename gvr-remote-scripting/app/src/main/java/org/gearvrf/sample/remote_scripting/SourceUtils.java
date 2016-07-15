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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.debug.DebugServer;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.script.GVRScriptManager;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.script.GVRScriptBehavior;
import org.gearvrf.script.GVRScriptException;
import org.gearvrf.script.GVRScriptFile;
import org.gearvrf.IErrorEvents;
import org.gearvrf.GVRResourceVolume;

public class SourceUtils {
    private GVRContext gvrContext;
    private GVRScriptManager mScriptManager;

    public SourceUtils(GVRContext context) {
        gvrContext = context;
        mScriptManager = gvrContext.getScriptManager();
    }

    private void logError(String message)
    {
        gvrContext.logError(message, this);
    }
    
    // from assets directory 
    public void script(String filename) {
        try {
            GVRResourceVolume.VolumeType volType = GVRResourceVolume.VolumeType.ANDROID_ASSETS;
            String lowerName = filename.toLowerCase();
            String language = FileNameUtils.getExtension(filename);
            
            if (lowerName.startsWith("sd:"))
            {
                volType = GVRResourceVolume.VolumeType.ANDROID_SDCARD;
                filename = filename.substring(3);
            }
            else if (lowerName.startsWith("http"))
            {
                volType = GVRResourceVolume.VolumeType.NETWORK;
            }
            GVRResourceVolume volume = new GVRResourceVolume(gvrContext, volType);
            GVRAndroidResource resource = volume.openResource(filename);
            GVRScriptFile script = mScriptManager.loadScript(resource, language);
            script.invoke();
            String err = script.getLastError();
            if (err != null) {
                logError(err);
            }
        } catch(IOException e) {
            logError(e.getMessage());
        } catch(GVRScriptException se) {
            logError(se.getMessage());
        }
    }
    
    public void scriptBundle(String filename)
    {
        GVRResourceVolume.VolumeType volType = GVRResourceVolume.VolumeType.ANDROID_ASSETS;
        String fname = filename.toLowerCase();
        if (fname.startsWith("sd:"))
        {
            volType = GVRResourceVolume.VolumeType.ANDROID_SDCARD;
        }
        else if (fname.startsWith("http"))
        {
            volType = GVRResourceVolume.VolumeType.NETWORK;
        }
        GVRResourceVolume volume = new GVRResourceVolume(gvrContext, volType, filename);
        try
        {
            mScriptManager.loadScriptBundle(filename, volume);
        }
        catch(IOException e)
        {
            logError(e.getMessage());
        }
    }
    
    public void attachScript(String filename, String sceneObjName)
    {
        GVRSceneObject sceneObj = gvrContext.getMainScene().getSceneObjectByName(sceneObjName);
        if (sceneObj == null)
        {
            logError("attachScript: scene object not found " + sceneObjName);
        }
        try
        {
            sceneObj.attachComponent(new GVRScriptBehavior(gvrContext, filename));
        }
        catch (IOException e)
        {
            logError(e.getMessage());
        }
        catch (GVRScriptException se)
        {
            logError(se.getMessage());
        }
    }

}

