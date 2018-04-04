package org.gearvrf.simplephysics.main;

import android.os.Bundle;

import org.gearvrf.GVRActivity;

public class MainActivity extends GVRActivity
{
    private MainScript main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = new MainScript();
        setMain(main, "gvr.xml");

        enableGestureDetector();
    }
}
