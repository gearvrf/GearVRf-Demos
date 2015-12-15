package org.gearvrf.gvrmeshanimationsample;

import org.gearvrf.GVRActivity;

import android.os.Bundle;

public class MeshAnimationActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setScript(new MeshAnimationScript(this), "gvr_note4.xml");
    }
}
