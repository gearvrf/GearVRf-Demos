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

package org.gearvrf.io.cursorsimple;

import android.os.Bundle;

import org.gearvrf.GVRActivity;

public class CursorActivity extends GVRActivity {
    private static final String TAG = CursorActivity.class.getSimpleName();
    private CursorMain cursorMain;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        cursorMain = new CursorMain();
        setMain(cursorMain, "gvr.xml");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursorMain.close();
    }
}
