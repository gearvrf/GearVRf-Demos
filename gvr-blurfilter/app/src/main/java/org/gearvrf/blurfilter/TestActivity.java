package org.gearvrf.blurfilter;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.view.MotionEvent;

public class TestActivity extends GVRActivity
{
    TestMain main = new TestMain();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setMain(main);
    }
}

