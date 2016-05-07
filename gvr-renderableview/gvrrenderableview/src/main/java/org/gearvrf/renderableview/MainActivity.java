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

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRScript;
import org.gearvrf.scene_objects.view.GVRFrameLayout;
import org.gearvrf.scene_objects.view.GVRTextView;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.scene_objects.view.GVRWebView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends GVRActivity {
    private GVRScript mScript;

    private GVRFrameLayout mFrameLayoutLeft;
    private GVRWebView mWebView;
    private GVRTextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createView();

        mScript = new MainScript(this);
        setScript(mScript, "gvr.xml");
    }

    private void createView() {
        mFrameLayoutLeft = new GVRFrameLayout(this);
        mFrameLayoutLeft.setLayoutParams(new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mFrameLayoutLeft.setBackgroundColor(Color.CYAN);

        View.inflate(this, R.layout.activity_main, mFrameLayoutLeft);

        mWebView = new GVRWebView(this);
        mWebView.loadUrl("http://www.gearvrf.org");
        mWebView.setLayoutParams(new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mTextView = new GVRTextView(this);
        mTextView.setText("Android's Rederable Views");
        mTextView.setTextColor(Color.WHITE);
    }

    public GVRView getFrameLayoutLeft() {
        return mFrameLayoutLeft;
    }

    public GVRView getWebView() {
        return mWebView;
    }

    public GVRView getTextView() {
        return mTextView;
    }
}
