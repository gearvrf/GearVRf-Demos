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

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.IViewEvents;
import org.gearvrf.debug.cli.LineProcessor;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.script.GVRScriptManager;

import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class EditorUtils {
    private final GVRContext gvrContext;
    private final GVRViewSceneObject layoutSceneObject;
    private GearVRScripting activity;

    private static final float QUAD_X = 2.0f;
    private static final float QUAD_Y = 1.0f;
    private final ScriptHandler mScriptHandler;

    private TextView updateButton;


    public EditorUtils(GVRContext context) {
        gvrContext = context;
        activity = (GearVRScripting) context.getActivity();

        layoutSceneObject = new GVRViewSceneObject(gvrContext, R.layout.main, viewEventsHandler,
                gvrContext.createQuad(QUAD_X, QUAD_Y));

        layoutSceneObject.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        layoutSceneObject.setName("editor");

        mScriptHandler = new ScriptHandler(gvrContext);
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


    private IViewEvents viewEventsHandler = new IViewEvents() {
        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            final EditText editor = (EditText) view.findViewById(R.id.editor);
            editor.requestFocus();
            editor.setDrawingCacheEnabled(false);
            editor.setBackgroundColor(Color.BLACK);

            updateButton = (TextView) view.findViewById(R.id.update);
            updateButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    android.util.Log.d("Editor", "update was clicked");
                    // get text
                    String script = editor.getText().toString();
                    // execute script
                    mScriptHandler.processLine(script);
                }
            });
        }

        @Override
        public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {

        }
    };

    class ScriptHandler implements LineProcessor {
        protected String prompt;
        protected ScriptEngine mScriptEngine;
        protected ScriptContext mScriptContext;
        protected StringWriter mWriter;

        public ScriptHandler(GVRContext gvrContext) {
            prompt = "";
            mScriptEngine = gvrContext.getScriptManager().getEngine(GVRScriptManager.LANG_JAVASCRIPT);
            mScriptContext = mScriptEngine.getContext();

            mWriter = new StringWriter();
            mScriptContext.setWriter(mWriter);
            mScriptContext.setErrorWriter(mWriter);
        }

        @Override
        public String processLine(String line) {
            try {
                mWriter.getBuffer().setLength(0);
                mScriptEngine.eval(line, mScriptContext);
                mWriter.flush();
                if (mWriter.getBuffer().length() != 0)
                    return mWriter.toString();
                else
                    return "";
            } catch (ScriptException e) {
                return e.toString();
            }
        }

        @Override
        public String getPrompt() {
            return prompt;
        }
    }
}

