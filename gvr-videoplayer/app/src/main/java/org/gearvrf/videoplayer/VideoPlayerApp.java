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


