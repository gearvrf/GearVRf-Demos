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

package org.gearvrf.sample.remote_scripting;

import org.gearvrf.GVRContext;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.GVRSensor;
import org.gearvrf.SensorEvent;
import org.gearvrf.ISensorEvents;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileBrowserUtils {
    private GVRContext gvrContext;
    private final GVRViewSceneObject layoutSceneObject;
    private GearVRScripting activity;

    private static final float QUAD_X = 2.0f;
    private static final float QUAD_Y = 1.0f;

    private String path;
    private ListView listView;
    private TextView dirView;
    private ProgressBar spinner;



    public FileBrowserUtils(GVRContext context) {
        gvrContext = context;
        activity = (GearVRScripting) context.getActivity();

        layoutSceneObject = new GVRViewSceneObject(gvrContext, R.layout.filebrowser,
                viewEventsHandler, gvrContext.createQuad(QUAD_X, QUAD_Y));

        layoutSceneObject.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        layoutSceneObject.setName("editor");
    }

    public void show() {
        gvrContext.getMainScene().addSceneObject(layoutSceneObject);
    }

    public void setPosition(float x, float y, float z) {
        layoutSceneObject.getTransform().setPosition(x, y, z);
    }

    public void setRotationByAxis(float angle, float x, float y, float z) {
        layoutSceneObject.getTransform().setRotationByAxis(angle, x, y, z);
    }

    public void hide() {
        gvrContext.getMainScene().removeSceneObject(layoutSceneObject);
    }

    private void init() {
        path = "/sdcard";
        chdir(path);
    }

    private void chdir(String filepath) {
        path = filepath;
        dirView.setText(path);
        spinner.setVisibility(View.GONE);

        List values = new ArrayList();
        File dir = new File(path);
        if(!dir.canRead()) {
            dirView.setText(dirView.getText() + " (inaccessible)");
        }

        // only allow model extensions we can read
        File[] list = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String filename = dir.getName() + File.separator + name;
                    if(new File(filename).isDirectory()) {
                        return true;
                    }
                    
                    if(
                        (name.toLowerCase().endsWith(".fbx")) ||
                        (name.toLowerCase().endsWith(".dae")) ||
                        (name.toLowerCase().endsWith(".gltf")) ||
                        (name.toLowerCase().endsWith(".glb")) ||
                        (name.toLowerCase().endsWith(".blend")) ||
                        (name.toLowerCase().endsWith(".3ds")) ||
                        (name.toLowerCase().endsWith(".ase")) ||
                        (name.toLowerCase().endsWith(".obj")) ||
                        (name.toLowerCase().endsWith(".xgl")) ||
                        (name.toLowerCase().endsWith(".dxf")) ||
                        (name.toLowerCase().endsWith(".lwo")) ||
                        (name.toLowerCase().endsWith(".lws")) ||
                        (name.toLowerCase().endsWith(".lxo")) ||
                        (name.toLowerCase().endsWith(".stl")) ||
                        (name.toLowerCase().endsWith(".ac")) ||
                        (name.toLowerCase().endsWith(".ms3d")) ||
                        (name.toLowerCase().endsWith(".cob")) ||
                        (name.toLowerCase().endsWith(".mdl")) ||
                        (name.toLowerCase().endsWith(".md2")) ||
                        (name.toLowerCase().endsWith(".md3")) ||
                        (name.toLowerCase().endsWith(".3d")) ||
                        (name.toLowerCase().endsWith(".ogex"))
                        ) {
                        return true;
                    }

                    return false;
                }
        });

        // add .. so the user can go up a level
        if(list != null) {
            values.add("..");
            for(File file : list) {
                values.add(file.getName());
            }
        }

        // sort alphabetically
        Collections.sort(values);

        ArrayAdapter adapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_2, android.R.id.text1, values);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String filename = (String) listView.getItemAtPosition(position);
                    if(filename.endsWith("..")) {
                        // strip out the /..
                        int index = path.lastIndexOf(File.separator);
                        filename = path.substring(0, index);
                    } else if(path.endsWith(File.separator)) {
                        filename = path + filename;
                    } else {
                        filename = path + File.separator + filename;
                    }

                    if(new File(filename).isDirectory()) {
                        chdir(filename);
                    } else if(!filename.isEmpty()) {
                        // strip out /sdcard
                        filename = filename.substring(8);
                        spinner.setVisibility(View.VISIBLE);
                        // try to load the model
                        try {
                            GVRSceneObject
                                model = gvrContext.getAssetLoader().loadModel("sd:/" +filename);
                            gvrContext.getMainScene().addSceneObject(model);

                            // base the name for the model on the filename, minus the suffix.  Also add a prefix since there may already be (and i've already seen) a node inside the model based on the filename
                            int end = filename.lastIndexOf(".");
                            int start = filename.lastIndexOf(File.separator, end) + 1;
                            String name = "so_" + filename.substring(start, end);
                            model.setName(name);
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                        spinner.setVisibility(View.GONE);
                    }
                }
            });
    }

    private IViewEvents viewEventsHandler = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            listView = (ListView) view.findViewById(R.id.list);
            dirView = (TextView) view.findViewById(R.id.dirname);
            spinner = (ProgressBar) view.findViewById(R.id.progressBar);
            spinner.setVisibility(View.GONE);
            init();
        }

        @Override
        public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {

        }
    };
}

