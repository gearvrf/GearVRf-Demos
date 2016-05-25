package org.gearvrf.sample.LodTest;

import org.gearvrf.GVRActivity;
import org.gearvrf.sample.LodTest.LODTestMain;

import android.app.Activity;
import android.os.Bundle;

public class LODTestActivity extends GVRActivity
{
    LODTestMain lodMain;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        lodMain = new LODTestMain();
        setMain(lodMain, "gvr.xml");
    }
}
