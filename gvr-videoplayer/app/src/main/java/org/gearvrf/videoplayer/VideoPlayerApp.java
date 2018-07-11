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

package org.gearvrf.videoplayer;

import android.app.Application;
import android.graphics.Typeface;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class VideoPlayerApp extends Application {

    private final String TAG;
    private static VideoPlayerApp sInstance;

    public static VideoPlayerApp getInstance() {
        return sInstance;
    }

    public VideoPlayerApp() {
        TAG = getClass().getSimpleName();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(VideoPlayerApp.this);
        loadCustomFonts();
    }

    private static void setInstance(VideoPlayerApp instance) {
        synchronized (VideoPlayerApp.class) {
            sInstance = instance;
        }
    }

    private void loadCustomFonts() {
        try {
            for (String s : getResources().getAssets().list("fonts")) {
                injectCustomTypeface(s.substring(0, s.length() - 4), Typeface.createFromAsset(getResources().getAssets(), "fonts/" + s));
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void injectCustomTypeface(String fontFamily, Typeface typeface) {
        try {
            Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);
            Object fieldValue = field.get(null);
            Map<String, Typeface> map = (Map<String, Typeface>) fieldValue;
            map.put(fontFamily, typeface);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

}


