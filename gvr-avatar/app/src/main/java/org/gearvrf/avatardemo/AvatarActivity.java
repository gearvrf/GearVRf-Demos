package org.gearvrf.avatardemo;

import org.gearvrf.GVRActivity;

import android.os.Bundle;

public class AvatarActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMain(new AvatarMain(this), "gvr.xml");
    }
}
