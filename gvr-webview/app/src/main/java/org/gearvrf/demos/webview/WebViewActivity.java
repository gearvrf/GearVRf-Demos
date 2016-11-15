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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.scene_objects.view.GVRWebView;

public class WebViewActivity extends GVRActivity
{
    private WebViewMain mMain;
    private GVRWebView webView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        createWebView();

        mMain = new WebViewMain();
        setMain(mMain, "gvr.xml");
    }

    private void createWebView() {
        webView = new GVRWebView(this);
        webView.setInitialScale(100);
        webView.measure(2000, 1000);
        webView.layout(0, 0, 2000, 1000);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("http://gearvrf.org");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    GVRView getWebView() {
        return webView;
    }

}
