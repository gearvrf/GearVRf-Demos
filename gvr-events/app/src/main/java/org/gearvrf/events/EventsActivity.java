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

package org.gearvrf.events;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRMain;
import org.gearvrf.scene_objects.view.GVRFrameLayout;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends GVRActivity {
    private GVRMain main;
    private GVRFrameLayout frameLayout;
    private TextView buttonTextView, keyTextView, listTextView;
    private Button button1, button2;
    private CheckBox checkBox;
    private String buttonPressed, listItemClicked;
    private ListView listView;

    private static final List<String> items = new ArrayList<String>(5);

    static {
        items.add("Note 4");
        items.add("GS 6");
        items.add("GS 6 Edge");
        items.add("Note 5");
        items.add("GS 6 Edge Plus");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frameLayout = new GVRFrameLayout(this);
        frameLayout.setBackgroundColor(Color.WHITE);
        frameLayout.getLayoutParams().height = frameLayout.getLayoutParams().width = 700;
        View.inflate(this, R.layout.activity_main, frameLayout);

        button1 = (Button) frameLayout.findViewById(R.id.button1);
        button2 = (Button) frameLayout.findViewById(R.id.button2);
        checkBox = (CheckBox) frameLayout.findViewById(R.id.checkBox);
        keyTextView = (TextView) frameLayout.findViewById(R.id.keyTextView);
        buttonTextView = (TextView) frameLayout
                .findViewById(R.id.buttonTextView);
        listTextView = (TextView) frameLayout.findViewById(R.id.listTextView);
        listView = (ListView) findViewById(R.id.listView);
        listView.setBackgroundColor(Color.LTGRAY);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(itemClickListener);
        button1.setOnClickListener(clickListener);
        button2.setOnClickListener(clickListener);
        checkBox.setOnClickListener(clickListener);
        buttonPressed = getResources().getString(R.string.buttonPressed);
        listItemClicked = getResources().getString(R.string.listClicked);
        main = new EventsMain(this, frameLayout, keyTextView);
        setMain(main, "gvr.xml");
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

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                long arg3) {
            listTextView.setText(String.format("%s %s", listItemClicked,
                    items.get(position)));
        }
    };
}
