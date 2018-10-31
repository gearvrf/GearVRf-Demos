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

package org.gearvrf.sample.sceneobjects;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.gearvrf.GVRActivity;

public class SceneObjectActivity extends GVRActivity {
    private static final String TAG = "SceneObjectActivity";
    private SampleMain mMain;
    private long lastDownTime = 0;
    private WebView webView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createWebView();

        mMain = new SampleMain(this);
        setMain(mMain);
    }

    private void createWebView() {
        webView = new WebView(this);
        webView.setInitialScale(100);
        webView.setLayoutParams(new ViewGroup.LayoutParams(2000, 1000));
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

    WebView getWebView() {
        return webView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMain.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
                // pass it as a tap to the Main
                mMain.onTap();
            }
        }

        return true;
    }
}
