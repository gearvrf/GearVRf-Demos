/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.gesture;

import android.view.MotionEvent;

public abstract class GestureDetector {

    private boolean mEnabled;

    public void setEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public abstract void onTouchEvent(MotionEvent event);

    public abstract float getValue();
}
