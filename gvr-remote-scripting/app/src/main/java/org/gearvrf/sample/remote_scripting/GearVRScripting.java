/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.sample.remote_scripting;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import org.gearvrf.GVRActivity;
import android.graphics.ImageFormat;
import android.net.wifi.WifiManager;
import java.nio.ByteOrder;
import java.lang.Integer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;

public class GearVRScripting extends GVRActivity
{
    private final String TAG = "GearVRScripting";
    String ipAddress;
    private Camera camera;
    private Handler handler = new Handler();
    private GearVRScriptingMain main;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ipAddress = getWifiIpAddress(this);
        createCameraView();
        main = new GearVRScriptingMain();
        setMain(main, "gvr.xml");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(camera != null) {
            camera.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCameraView();
    }


    public Handler getHandler() {
        return handler;
    }

    /* 
     * The following getWifiIpAddress() method is taken from:
     * http://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
     */
    protected String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            android.util.Log.e(TAG, "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    private void createCameraView() {
        if (!checkCameraHardware(this)) {
            android.util.Log.d(TAG, "Camera hardware not available.");
            return;
        }

        camera = null;
        try {
            camera = Camera.open();
        } catch (Exception exception) {
            android.util.Log.d(TAG, "Camera not available or is in use");
        }
    }

    Camera getCamera() {
        return camera;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        main.stop();
    }

}
