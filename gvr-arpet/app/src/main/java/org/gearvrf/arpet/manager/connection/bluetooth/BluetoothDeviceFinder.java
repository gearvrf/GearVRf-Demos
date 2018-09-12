/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.arpet.manager.connection.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.arpet.connection.DeviceFilter;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceFinder extends BroadcastReceiver {

    private static final String TAG = BluetoothDeviceFinder.class.getSimpleName();

    private IntentFilter mIntentFilter;
    private Context mContext;
    private DeviceFilter mDeviceFilter;
    private OnFindCallback mOnFindCallback;
    private List<BTDevice> mDevicesFound;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mReturnOnFirstFound;

    public BluetoothDeviceFinder(@NonNull Context context) {

        this.mContext = context;
        this.mDevicesFound = new ArrayList<>();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        this.mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public void find(DeviceFilter filter, boolean returnOnFirstFound, @NonNull OnFindCallback callback) {
        mOnFindCallback = callback;
        mDeviceFilter = filter;
        mReturnOnFirstFound = returnOnFirstFound;
        mDevicesFound.clear();
        setReceiverEnabled(true);
        doDiscovery();
    }

    public void find(@NonNull OnFindCallback callback) {
        find(null, false, callback);
    }

    private void setReceiverEnabled(boolean enabled) {
        if (enabled) {
            mContext.registerReceiver(this, mIntentFilter);
        } else {
            mContext.unregisterReceiver(this);
        }
    }

    private void doDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            BluetoothDevice btDeviceFound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, String.format("Device found { name: %s, type: %s}",
                    btDeviceFound.getName(), btDeviceFound.getBluetoothClass().getMajorDeviceClass()));

            BTDevice device = new BTDevice(btDeviceFound);

            if (mDeviceFilter == null || mDeviceFilter.meet(device)) {
                mDevicesFound.add(device);
                if (mReturnOnFirstFound) {
                    setReceiverEnabled(false);
                    mOnFindCallback.onFound(mDevicesFound.toArray(new BTDevice[mDevicesFound.size()]));
                }
            }

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            setReceiverEnabled(false);
            mOnFindCallback.onFound(mDevicesFound.toArray(new BTDevice[mDevicesFound.size()]));
        }
    }

    @FunctionalInterface
    public interface OnFindCallback {
        void onFound(BTDevice[] devices);
    }
}
