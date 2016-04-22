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

package org.gearvrf.remotescripting;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import org.gearvrf.GVRActivity;
import android.net.wifi.WifiManager;
import java.nio.ByteOrder;
import java.lang.Integer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class GearVRScripting extends GVRActivity
{
    private final String TAG = "GearVRScripting";
    String ipAddress;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ipAddress = getWifiIpAddress(this);

        setScript(new GearVRScriptingManager(), "gvr.xml");
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
}
