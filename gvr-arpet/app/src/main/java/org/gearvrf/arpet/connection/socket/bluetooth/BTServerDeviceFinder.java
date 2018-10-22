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
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BTServerDeviceFinder {

    private static final String TAG = BTServerDeviceFinder.class.getSimpleName();

    private Context mContext;
    private OnFindCallback mOnFindCallback;
    private List<BTDevice> mServersFound;
    private List<BluetoothDevice> mPendingDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private LocalReceiver mReceiver;
    private boolean isFinding;

    public BTServerDeviceFinder(@NonNull Context context) {
        this.mContext = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void find(@NonNull OnFindCallback callback) {

        if (isFinding) {
            throw new IllegalStateException("Already is finding for servers");
        }

        Log.d(TAG, "Finding servers...");

        isFinding = true;
        mOnFindCallback = callback;
        mPendingDevices = Collections.synchronizedList(new ArrayList<>());
        mServersFound = Collections.synchronizedList(new ArrayList<>());

        findInPairedDevices();
        setReceiverEnabled(true);
        doDiscovery();
    }

    public void cancel() {
        if (isFinding) {
            setReceiverEnabled(false);
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mPendingDevices = null;
            mServersFound = null;
            mOnFindCallback = null;
            isFinding = false;
            Log.d(TAG, "Discovery canceled");
        }
    }

    private void findInPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice paired : pairedDevices) {
            if (isPhoneDevice(paired)) {
                if (paired.getUuids() == null) {
                    Log.d(TAG, "Server candidate found in paired list: " + deviceToString(paired));
                    mPendingDevices.add(paired);
                } else {
                    if (isServerDevice(paired)) {
                        Log.d(TAG, "Server found in paired list: " + deviceToString(paired));
                        mServersFound.add(new BTDevice(paired));
                    }
                }
            }
        }
    }

    private boolean isPhoneDevice(BluetoothDevice device) {
        return device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE;
    }

    private boolean isPaired(BluetoothDevice device) {
        return device.getBondState() == BluetoothDevice.BOND_BONDED;
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
        try {
            if (enabled) {
                if (mReceiver == null) {
                    mReceiver = new LocalReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_UUID);
                    filter.addAction(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    mContext.registerReceiver(mReceiver, filter);
                }
            } else {
                if (mReceiver != null) {
                    mContext.unregisterReceiver(mReceiver);
                    mReceiver = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
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

    private class LocalReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!isPaired(device) && isPhoneDevice(device)) {
                    // Check cached SDP records
                    if (isServerDevice(device)) {
                        Log.d(TAG, "Cached server found: " + deviceToString(device));
                        mServersFound.add(new BTDevice(device));
                    } else {
                        mPendingDevices.add(device);
                        Log.d(TAG, "New phone found and added to pending: " + deviceToString(device));
                    }
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (hasNextPendingToFetchUuid()) {
                    Log.d(TAG, "Discovery finished. Start UUIDs fetching for phones found");
                    fetchUuidForNextPending();
                } else {
                    Log.d(TAG, "Discovery finished. No phone found");
                    notifyFoundOrRetry();
                }

            } else if (BluetoothDevice.ACTION_UUID.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (isServerDevice(device)) {
                    Log.d(TAG, "New server found: " + deviceToString(device));
                    mServersFound.add(new BTDevice(device));
                }

                if (hasNextPendingToFetchUuid()) {
                    fetchUuidForNextPending();
                } else {
                    Log.d(TAG, "UUIDs fetching finished");
                    notifyFoundOrRetry();
                }
            }
        }
    }

    private void notifyFoundOrRetry() {
        if (!mServersFound.isEmpty()) {
            BTDevice[] result = mServersFound.toArray(new BTDevice[0]);
            setReceiverEnabled(false);
            mPendingDevices = null;
            mServersFound = null;
            mOnFindCallback.onResult(result);
            mOnFindCallback = null;
            isFinding = false;
        } else {
            isFinding = false;
            find(mOnFindCallback);
        }
    }

    @FunctionalInterface
    public interface OnFindCallback {
        void onResult(BTDevice[] devices);
    }
}
