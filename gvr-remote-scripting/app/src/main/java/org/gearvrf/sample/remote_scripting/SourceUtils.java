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

import java.io.IOException;
import java.net.URL;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.script.GVRScriptManager;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.script.GVRScriptException;
import org.gearvrf.script.GVRScriptFile;

public class SourceUtils {
    private GVRContext gvrContext;
    private GVRScriptManager mScriptManager;

    public SourceUtils(GVRContext context) {
        gvrContext = context;
        mScriptManager = gvrContext.getScriptManager();
    }

    // from assets directory 
    public void script(String filename, String language) {
        try {
            GVRAndroidResource resource = new GVRAndroidResource(gvrContext, filename);
            GVRScriptFile script = mScriptManager.loadScript(resource, language);
            script.invoke();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(GVRScriptException se) {
            se.printStackTrace();
        }
    }

    // from /sdcard directory 
    public void scriptFromSD(String filename, String language) {
        try {
            GVRAndroidResource resource = new GVRAndroidResource(filename);
            GVRScriptFile script = mScriptManager.loadScript(resource, language);
            script.invoke();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(GVRScriptException se) {
            se.printStackTrace();
        }
    }

    // from a URL 
    public void scriptFromURL(URL url, String language) {
        try {
            GVRAndroidResource resource = new GVRAndroidResource(gvrContext, url);
            GVRScriptFile script = mScriptManager.loadScript(resource, language);
            script.invoke();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(GVRScriptException se) {
            se.printStackTrace();
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
            e.printStackTrace();
        }
    }

}

