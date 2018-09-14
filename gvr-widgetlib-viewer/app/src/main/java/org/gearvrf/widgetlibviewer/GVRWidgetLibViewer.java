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

package org.gearvrf.widgetlibviewer;

import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;

import static org.gearvrf.utility.Log.tag;


public class GVRWidgetLibViewer extends GVRActivity
{
    private ViewerMain mMain;
    private final static String TAG = tag(GVRWidgetLibViewer.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Log.init(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate!");
        super.onCreate(savedInstanceState);

        mMain = new ViewerMain(this);
        setMain(mMain, "gvr.xml");
    }

    @Override
    protected void onDestroy() {
        try {
            if (WidgetLib.isInitialized()) {
                WidgetLib.destroy();
            }
        } finally {
            // make sure super is called last always, it clears internal gvrf
            // references and things may crash
            super.onDestroy();
        }
    }
}
