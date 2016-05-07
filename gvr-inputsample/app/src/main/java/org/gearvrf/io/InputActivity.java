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

package org.gearvrf.io;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.view.GVRTextView;

public class InputActivity extends GVRActivity {
    private static final String TAG = InputActivity.class.getSimpleName();
    InputScript script;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // create a textview on the main thread for the script
        GVRTextView textView = new GVRTextView(this, 1000, 500);
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        textView.setGravity(Gravity.CENTER);

        script = new InputScript(textView);
        setScript(script, "gvr.xml");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        script.close();
    }
}
