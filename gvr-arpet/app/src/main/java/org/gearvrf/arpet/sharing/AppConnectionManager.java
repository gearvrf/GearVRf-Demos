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

package org.gearvrf.arpet.sharing;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.connection.Connection;
import org.gearvrf.arpet.connection.ManagerState;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.exception.ConnectionException;
import org.gearvrf.arpet.manager.connection.bluetooth.BTConnectionManager;
import org.gearvrf.arpet.manager.connection.bluetooth.BTDevice;
import org.gearvrf.arpet.manager.connection.bluetooth.BTMessage;
import org.gearvrf.arpet.manager.connection.bluetooth.BluetoothDeviceFinder;

import java.io.Serializable;
import java.util.Arrays;

public final class AppConnectionManager extends BTConnectionManager implements IAppConnectionManager {

    private static final int REQUEST_ENABLE_BT = 1000;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 1001;
    private static final int DISCOVERABLE_DURATION = 30; // in seconds

    private Activity mContext;
    private UiMessageHandler mUiMessageHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDeviceFinder mDeviceFinder;
    private OnEnableBluetoothCallback mEnableBTCallback;
    private OnEnableDiscoverableCallback mEnableDiscoverableCallback;

    private static volatile IAppConnectionManager sInstance;

    private AppConnectionManager(
            @NonNull Activity context,
            @NonNull UiMessageHandler mMessageHandler) {

        this.mContext = context;
        this.mUiMessageHandler = mMessageHandler;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mDeviceFinder = new BluetoothDeviceFinder(mContext);
    }

    public static IAppConnectionManager getInstance(
            @NonNull PetContext petContext,
            @NonNull UiMessageHandler mMessageHandler) {
        if (sInstance == null) {
            synchronized (IAppConnectionManager.class) {
                if (sInstance == null) {
                    sInstance = new AppConnectionManager(petContext.getActivity(), mMessageHandler);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void startUsersInvitation() {
        Log.d(TAG, "startUsersInvitation: ");
        if (stateIs(ManagerState.IDLE)) {
            Log.d(TAG, "startUsersInvitation: request enable BT");
            enableBluetooth(() -> {
                Log.d(TAG, "startUsersInvitation: OK, BT enabled. Request device discoverable");
                enableDiscoverable(() -> {
                    Log.d(TAG, "startUsersInvitation: OK, now this device is discoverable for "
                            + DISCOVERABLE_DURATION + " seconds. Wait for connections");
                    startConnectionListener(this::onMessageReceived);
                    sendMessageToUI(UiMessageType.CONNECTION_LISTENER_STARTED);
                });
            });
        }
    }

    @Override
    public void stopUsersInvitation() {
        this.stopConnectionListener();
    }

    @Override
    public void acceptInvitation() {
        Log.d(TAG, "acceptInvitation: ");
        if (stateIs(ManagerState.IDLE)) {
            enableBluetooth(() -> {
                Log.d(TAG, "acceptInvitation: finding devices...");
                // Broadcast all devices found and saves the first successfully connection

                mDeviceFinder.find(new PhoneTypeDeviceFilter(), false, this::onDevicesFound);
                // mDeviceFinder.find(this::onDevicesFound);
            });
        }
    }

    /**
     * Handle devices found by {@link BluetoothDeviceFinder}.
     *
     * @param devices Devices to handle.
     */
    private void onDevicesFound(BTDevice[] devices) {
        Log.d(TAG, "onDevicesFound: " + devices.length);
        if (devices.length > 0) {
            Log.d(TAG, "onDevicesFound: Trying connect to devices " + Arrays.toString(devices));
            connectToDevices(this::onMessageReceived, devices);
        } else {
            sendMessageToUI(UiMessageType.CONNECTION_NOT_FOUND);
        }
    }

    /**
     * Handle message received form remote device.
     *
     * @param message Message form remote device.
     */
    private void onMessageReceived(Message message) {
        Log.d(TAG, "onMessageReceived: " + message);
        sendMessageToUI(UiMessageType.MESSAGE_RECEIVED, message);
    }

    @Override
    public void onConnectionEstablished(Connection connection) {
        Log.d(TAG, "onConnectionEstablished: " + connection.getRemoteDevice());
        // Stop trying connection when first connection is successful
        if (stateIs(ManagerState.CONNECTING_TO_REMOTE)) {
            super.onConnectionEstablished(connection);
            cancelOutgoingConnectionsThreads();
            sendMessageToUI(UiMessageType.CONNECTION_ESTABLISHED);
        } else {
            super.onConnectionEstablished(connection);
        }
    }

    @Override
    public void onConnectionFailure(ConnectionException error) {
        Log.d(TAG, "onConnectionFailure: " + error.getMessage());
        if (stateIs(ManagerState.CONNECTING_TO_REMOTE)) {
            super.onConnectionFailure(error);
            if (stateIs(ManagerState.IDLE)) {
                Log.d(TAG, "onConnectionFailure: No connection found.");
                sendMessageToUI(UiMessageType.CONNECTION_NOT_FOUND);
            }
        } else {
            super.onConnectionFailure(error);
        }
    }

    @Override
    public void onConnectionLost(Connection connection, ConnectionException error) {
        super.onConnectionLost(connection, error);
        if (getTotalConnected() == 0) {
            sendMessageToUI(UiMessageType.CONNECTION_LOST);
        }
    }

    @Override
    public void stopConnectionListener() {
        if (stateIs(ManagerState.LISTENING_TO_CONNECTIONS)) {
            Log.d(TAG, "stopConnectionListener: force stop connection listener");
            super.stopConnectionListener();
            if (getTotalConnected() > 0) {
                sendMessageToUI(UiMessageType.CONNECTION_ESTABLISHED);
            } else {
                sendMessageToUI(UiMessageType.CONNECTION_NOT_FOUND);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                mEnableBTCallback.onEnabled();
            } else {
                sendMessageToUI(UiMessageType.ERROR_BLUETOOTH_NOT_ENABLED);
            }
        } else if (requestCode == REQUEST_ENABLE_DISCOVERABLE) {
            if (resultCode != Activity.RESULT_CANCELED) {
                mEnableDiscoverableCallback.onEnabled();
            } else {
                sendMessageToUI(UiMessageType.ERROR_DEVICE_NOT_DISCOVERABLE);
            }
        }
    }

    public static Message newMessage(Serializable data) {
        return new BTMessage(data);
    }

    private void sendMessageToUI(@UiMessageType int type) {
        sendMessageToUI(type, null);
    }

    private void sendMessageToUI(@UiMessageType int type, Serializable data) {
        if (mUiMessageHandler != null) {
            mUiMessageHandler.handleMessage(new UiMessage(type, data));
        }
    }

    private void enableBluetooth(OnEnableBluetoothCallback callback) {
        mEnableBTCallback = callback;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            callback.onEnabled();
        }
    }

    private void enableDiscoverable(OnEnableDiscoverableCallback callback) {
        mEnableDiscoverableCallback = callback;
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
            mContext.startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE);
        } else {
            mEnableDiscoverableCallback.onEnabled();
        }
    }

    @FunctionalInterface
    private interface OnEnableBluetoothCallback {
        void onEnabled();
    }

    @FunctionalInterface
    private interface OnEnableDiscoverableCallback {
        void onEnabled();
    }
}

