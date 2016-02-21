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

package org.gearvrf.input.events;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRScript;

import org.gearvrf.scene_objects.view.GVRFrameLayout;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class EventsActivity extends GVRActivity {
    private static final String TAG = EventsActivity.class.getSimpleName();
    private GVRScript script;
    private GVRFrameLayout frameLayout;
    private TextView buttonTextView, keyTextView;
    private Button button1, button2;
    private CheckBox checkBox;
    private String buttonPressed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frameLayout = new GVRFrameLayout(this);
        frameLayout.setBackgroundColor(Color.WHITE);
        View.inflate(this, R.layout.activity_main, frameLayout);
        button1 = (Button) frameLayout.findViewById(R.id.button1);
        button2 = (Button) frameLayout.findViewById(R.id.button2);
        checkBox = (CheckBox) frameLayout.findViewById(R.id.checkBox);
        keyTextView = (TextView) frameLayout.findViewById(R.id.keyTextView);
        buttonTextView = (TextView) frameLayout
                .findViewById(R.id.buttonTextView);
        button1.setOnClickListener(clickListener);
        button2.setOnClickListener(clickListener);
        checkBox.setOnClickListener(clickListener);
        buttonPressed = getResources().getString(R.string.buttonPressed);
        script = new EventsScript(this, frameLayout, keyTextView);
        setScript(script, "gvr.xml");
    }

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String button = new String();
            switch (v.getId()) {
            case R.id.button1:
                button = "1";
                break;
            case R.id.button2:
                button = "2";
                break;
            case R.id.checkBox:
                button = "Check Box";
                break;
            default:
                break;
            }

            buttonTextView
                    .setText(String.format("%s %s", buttonPressed, button));
        }
    };
}
