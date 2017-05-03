package org.gearvrf.gvrcamera2renderscript;

import android.os.Bundle;

import org.gearvrf.GVRActivity;

public class Camera2RenderscriptActivity extends GVRActivity {
    private Camera2RenderscriptManager mManger;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mManger = new Camera2RenderscriptManager(this);
        setMain(mManger);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManger.onPause();
        finish();
    }
}
