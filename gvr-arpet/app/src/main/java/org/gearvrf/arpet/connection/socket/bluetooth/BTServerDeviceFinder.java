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

package org.gearvrf.arpet.connection.socket.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BTServerDeviceFinder extends BroadcastReceiver {

    private static final String TAG = BTServerDeviceFinder.class.getSimpleName();

    private IntentFilter mIntentFilter;
    private Context mContext;
    private OnFindCallback mOnFindCallback;
    private List<BTDevice> mServersFound = new ArrayList<>();
    private List<BluetoothDevice> mPendingDevices = Collections.synchronizedList(new ArrayList<>());
    private BluetoothAdapter mBluetoothAdapter;

    public BTServerDeviceFinder(@NonNull Context context) {

        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(BluetoothDevice.ACTION_UUID);
        this.mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        this.mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public void find(@NonNull OnFindCallback callback) {

        mOnFindCallback = callback;
        mPendingDevices.clear();
        mServersFound.clear();

        findInPairedDevices();
        setReceiverEnabled(true);
        doDiscovery();
    }

    private void findInPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice paired : pairedDevices) {
            if (isPhoneDevice(paired)) {
                if (paired.getUuids() == null) {
                    Log.d(TAG, "Paired device added to pending " + deviceToString(paired));
                    mPendingDevices.add(paired);
                } else {
                    if (isServerDevice(paired)) {
                        Log.d(TAG, "Server found in paired " + deviceToString(paired));
                        mServersFound.add(new BTDevice(paired));
                    }
                }
            }
        }
    }

    private boolean isPhoneDevice(BluetoothDevice device) {
        return device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE;
    }

    private boolean isServerDevice(BluetoothDevice device) {
        ParcelUuid[] uuids = device.getUuids();
        if (uuids != null) {
            for (ParcelUuid parcelUuid : uuids) {
                if (parcelUuid.getUuid().equals(BTConstants.SOCKET_SERVER_UUID)) {
                    return true;
                }
            }
        }
        return false;
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

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (isPhoneDevice(device)) {
                // Check cached SDP records
                if (isServerDevice(device)) {
                    Log.d(TAG, "Cached server found" + deviceToString(device));
                    mServersFound.add(new BTDevice(device));
                } else {
                    mPendingDevices.add(device);
                    Log.d(TAG, "Candidate device found added to pending " + deviceToString(device));
                }
            }

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if (hasNextPendingToFetchUuid()) {
                fetchUuidForNextPending();
            } else {
                Log.d(TAG, "Notify result on discovery finished");
                setReceiverEnabled(false);
                notifyResult();
            }

        } else if (BluetoothDevice.ACTION_UUID.equals(action)) {

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (isServerDevice(device)) {
                Log.d(TAG, "Server found " + deviceToString(device));
                mServersFound.add(new BTDevice(device));
            }

            if (hasNextPendingToFetchUuid()) {
                fetchUuidForNextPending();
            } else {
                Log.d(TAG, "Notify result on all pending processed");
                setReceiverEnabled(false);
                notifyResult();
            }
        }
    }

    private boolean hasNextPendingToFetchUuid() {
        return !mPendingDevices.isEmpty();
    }

    private void fetchUuidForNextPending() {
        if (!mPendingDevices.isEmpty()) {
            BluetoothDevice device = mPendingDevices.remove(0);
            while (!isPhoneDevice(device) || !device.fetchUuidsWithSdp()) {
                device = mPendingDevices.remove(0);
            }
            Log.d(TAG, "Fetching UUID for " + deviceToString(device));
        }
    }

    private String deviceToString(BluetoothDevice device) {
        return String.format(Locale.getDefault(), "{ name: %s, address %s, type: %d, uuid: %s }",
                device.getName(), device.getAddress(), device.getBluetoothClass().getMajorDeviceClass(),
                Arrays.toString(device.getUuids()));
    }

    private void notifyResult() {
        new Handler().post(() -> mOnFindCallback.onResult(mServersFound.toArray(new BTDevice[mServersFound.size()])));
    }

    @FunctionalInterface
    public interface OnFindCallback {
        void onResult(BTDevice[] devices);
    }
}
