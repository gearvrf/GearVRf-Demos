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

package org.gearvrf.x3ddemo;

import org.gearvrf.GVRActivity;
//import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.scene_objects.view.GVRWebView;

import android.os.Bundle;

public class X3DparserActivity extends GVRActivity {

    X3DparserScript script = new X3DparserScript(this);
    private GVRWebView mWebView;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setScript(script, "gvr_note4.xml");
        
        mWebView = new GVRWebView( this );
    }
  
  public GVRWebView getWebView() {
      return mWebView;
  }
}
