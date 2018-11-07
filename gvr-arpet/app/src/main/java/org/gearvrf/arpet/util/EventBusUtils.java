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

package org.gearvrf.arpet.util;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public final class EventBusUtils {

    private static final String TAG = EventBusUtils.class.getSimpleName();

    private static EventBus mEventBus = EventBus.getDefault();

    private EventBusUtils() {
    }

    public static void post(Object object) {
        mEventBus.post(object);
    }

    public static void register(Object object) {
        if (!mEventBus.isRegistered(object)) {
            Log.i(TAG, "Subscriber registered: " + object);
            mEventBus.register(object);
        }
    }

    public static void unregister(Object object) {
        if (mEventBus.isRegistered(object)) {
            Log.i(TAG, "Subscriber unregistered: " + object);
            mEventBus.unregister(object);
        }
    }
}
