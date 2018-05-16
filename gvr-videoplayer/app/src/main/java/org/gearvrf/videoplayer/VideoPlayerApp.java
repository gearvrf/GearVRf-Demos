package org.gearvrf.videoplayer;

import android.app.Application;

public class VideoPlayerApp extends Application {

    private static VideoPlayerApp sInstance;

    public static VideoPlayerApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(VideoPlayerApp.this);
    }

    private static void setInstance(VideoPlayerApp instance) {
        synchronized (VideoPlayerApp.class) {
            sInstance = instance;
        }
    }
}


