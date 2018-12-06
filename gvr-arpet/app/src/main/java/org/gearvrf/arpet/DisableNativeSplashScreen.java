package org.gearvrf.arpet;

import org.gearvrf.GVRMain;

public abstract class DisableNativeSplashScreen extends GVRMain {

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

}
