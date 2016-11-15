/* Copyright 2016 Samsung Electronics Co., LTD
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
package org.gearvrf.demos.webview;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;


public class WebViewMain extends GVRMain
{
    private GVRContext mGVRContext;
    private WebViewActivity mActivity;

    /** Called when the activity is first created. */
    @Override
    public void onInit(GVRContext gvrContext) {
                // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;
        mActivity = (WebViewActivity) gvrContext.getActivity();

        GVRScene scene = gvrContext.getNextMainScene();

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();


        GVRViewSceneObject webViewObject = createWebViewObject(gvrContext);

        // add the scene object to the scene graph
        scene.addSceneObject(webViewObject);
    }

    private GVRViewSceneObject createWebViewObject(GVRContext gvrContext) {
        GVRView webView = mActivity.getWebView();
        GVRViewSceneObject webObject = new GVRViewSceneObject(gvrContext,
                webView, 8.0f, 4.0f);
        webObject.setName("web view object");
        webObject.getRenderData().getMaterial().setOpacity(1.0f);
        webObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);

        return webObject;
    }
    
    @Override
    public void onStep() {
    }
}
