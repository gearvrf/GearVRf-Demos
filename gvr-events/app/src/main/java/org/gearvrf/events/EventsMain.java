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

import android.app.Activity;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;

import java.util.ArrayList;
import java.util.List;

public class EventsMain extends GVRMain {
    private static final String TAG = EventsMain.class.getSimpleName();

    private GVRViewSceneObject layoutSceneObject;
    private GVRContext context;
    private GVRScene mainScene;

    private static final float DEPTH = -1.5f;

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
    public void onInit(final GVRContext gvrContext) {
        context = gvrContext;
        mainScene = gvrContext.getMainScene();

        layoutSceneObject = new GVRViewSceneObject(gvrContext,
                R.layout.activity_main, viewSOEventsListener);

        gvrContext.getInputManager().selectController();
    }

    @Override
    public void onStep() {
        // unused
    }

    public void onKeyDown(int keyCode) {
        keyTextView.setText(String.format("Key Pressed: %s ",
                KeyEvent.keyCodeToString(keyCode)));
    }

    private IViewEvents viewSOEventsListener = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            final Activity activity = context.getActivity();

            button1 = view.findViewById(R.id.button1);
            button2 = view.findViewById(R.id.button2);
            checkBox = view.findViewById(R.id.checkBox);
            keyTextView = view.findViewById(R.id.keyTextView);
            buttonTextView = view.findViewById(R.id.buttonTextView);
            listTextView = view.findViewById(R.id.listTextView);
            listView = view.findViewById(R.id.listView);
            listView.setBackgroundColor(Color.LTGRAY);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.list_item, items);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(itemClickListener);
            button1.setOnClickListener(clickListener);
            button1.setOnHoverListener(buttonHoverListener);
            button2.setOnClickListener(clickListener);
            button2.setOnHoverListener(buttonHoverListener);
            checkBox.setOnClickListener(clickListener);
            buttonPressed = activity.getResources().getString(R.string.buttonPressed);
            listItemClicked = activity.getResources().getString(R.string.listClicked);
        }

        @Override
        public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
            mainScene.addSceneObject(gvrViewSceneObject);
            gvrViewSceneObject.getTransform().setPosition(0.0f, 0.0f, DEPTH);
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {

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

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            listTextView.setText(String.format("%s %s", listItemClicked,
                    items.get(position)));
        }
    };

    private View.OnHoverListener buttonHoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    ((Button) v).setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    ((Button) v).setTextColor(Color.BLACK);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
}
