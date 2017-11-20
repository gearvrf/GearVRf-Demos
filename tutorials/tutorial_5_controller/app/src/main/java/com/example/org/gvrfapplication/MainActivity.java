package com.example.org.gvrfapplication;

import android.os.Bundle;

import org.gearvrf.GVRActivity;

public class MainActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set Main Scene
         * It will be displayed when app starts
         */
        setMain(new MainScene(), "gvr.xml");
    }
}
