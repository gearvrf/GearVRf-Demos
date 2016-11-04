package org.gearvrf.samples.transparencyTest;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRMain;

/* 
 * This test will demonstrate transparency sorting.
 *
 * Transparency sorting will only work when frustum culling is enabled.  (the sorting relies on distance from the camera which is only calculated when culling is enabled).
 * In this app, frustum culling is disabled on start up.
 * Transparent objects (text) are added in random-ish order.
 * In the beginning, you should see "He_________".
 * Once you tap the touchpad, frustum culling will be enabled and transparent objects will be sorted by camera distance.
 * You should see "Hello_World!" at this point.
 * Tapping the touchpad any further will have no effect since the objects will already be sorted.
 */
public class TransparencyTest extends GVRActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setMain(new TransparencyTestMain(), "gvr.xml");
    }
}
