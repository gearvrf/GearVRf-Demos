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

package org.gearvrf.renderableview;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRMain;
import org.gearvrf.scene_objects.view.GVRFrameLayout;
import org.gearvrf.scene_objects.view.GVRTextView;

public class MainActivity extends GVRActivity {
    private GVRMain mMain;

    private FrameLayout mFrameLayoutLeft;
    private WebView mWebView;
    private TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createView();

        mMain = new Main(this);
        setMain(mMain, "gvr.xml");
    }

    private void createView() {
        mFrameLayoutLeft = new FrameLayout(this);
        mFrameLayoutLeft.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mFrameLayoutLeft.setBackgroundColor(Color.CYAN);
        View.inflate(this, R.layout.activity_main, mFrameLayoutLeft);

        mWebView = new WebView(this);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("https://resources.samsungdevelopers.com/Gear_VR/020_GearVR_Framework_Project");
        mWebView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mTextView = new TextView(this);
        mTextView.setText("Android's Renderable Views");
        mTextView.setTextColor(Color.WHITE);
    }

    public FrameLayout getFrameLayoutLeft() {
        return mFrameLayoutLeft;
    }

    public WebView getWebView() {
        return mWebView;
    }

    public TextView getTextView() {
        return mTextView;
    }
}
