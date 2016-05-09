package org.gearvrf.gvrbullet;

import org.gearvrf.GVRActivity;

import android.os.Bundle;

public class BulletSampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScript(new BulletSampleViewManager(), "gvr.xml");
    }

}
