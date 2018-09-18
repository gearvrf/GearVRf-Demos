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
import org.gearvrf.arpet.manager.connection.bluetooth.BTServerDeviceFinder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class PetConnectionManager extends BTConnectionManager implements IPetConnectionManager {

    private static final int REQUEST_ENABLE_BT = 1000;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 1001;
    private static final int DISCOVERABLE_DURATION = 30; // in seconds

    private PetContext mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BTServerDeviceFinder mServerFinder;
    private OnEnableBluetoothCallback mEnableBTCallback;
    private OnEnableDiscoverableCallback mEnableDiscoverableCallback;
    private List<PetConnectionMessageHandler> mPetConnectionMessageHandlers = new ArrayList<>();

    private static volatile PetConnectionManager sInstance;

    private PetConnectionManager() {
    }

    public static IPetConnectionManager getInstance() {
        if (sInstance == null) {
            synchronized (IPetConnectionManager.class) {
                if (sInstance == null) {
                    sInstance = new PetConnectionManager();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void init(@NonNull PetContext context) {
        if (mContext == null) {
            mContext = context;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mServerFinder = new BTServerDeviceFinder(mContext.getActivity());
            mContext.addOnPetContextListener((requestCode, resultCode, data)
                    -> onActivityResult(requestCode, resultCode));
        }
    }

    @Override
    public void addMessageHandler(PetConnectionMessageHandler handler) {
        checkInitialization();
        mPetConnectionMessageHandlers.remove(handler);
        mPetConnectionMessageHandlers.add(handler);
    }

    @Override
    public void removeMessageHandlers(PetConnectionMessageHandler handler) {
        checkInitialization();
        mPetConnectionMessageHandlers.remove(handler);
    }

    @Override
    public void startUsersInvitation() {
        checkInitialization();
        Log.d(TAG, "startUsersInvitation: ");
        if (stateIs(ManagerState.IDLE)) {
            Log.d(TAG, "startUsersInvitation: request enable BT");
            enableBluetooth(() -> {
                Log.d(TAG, "startUsersInvitation: OK, BT enabled. Request device discoverable");
                enableDiscoverable(() -> {
                    Log.d(TAG, "startUsersInvitation: OK, now this device is discoverable for "
                            + DISCOVERABLE_DURATION + " seconds. Waiting for connections");
                    startConnectionListener(this::onMessageReceived);
                    sendMessageToUI(PetConnectionMessageType.CONNECTION_LISTENER_STARTED);
                });
            });
        }
    }

    @Override
    public void stopUsersInvitation() {
        checkInitialization();
        this.stopConnectionListener();
    }

    @Override
    public void acceptInvitation() {
        checkInitialization();
        Log.d(TAG, "acceptInvitation: ");
        if (stateIs(ManagerState.IDLE)) {
            enableBluetooth(() -> {
                Log.d(TAG, "acceptInvitation: finding server devices...");
                // Broadcast all devices found and saves the first successfully connection
                mServerFinder.find(this::onServerFinderResult);
            });
        }
    }

    @Override
    public boolean isConnectedAs(int mode) {
        checkInitialization();
        return super.isConnectedAs(mode);
    }

    @Override
    public int getConnectionMode() {
        checkInitialization();
        return super.getConnectionMode();
    }

    @Override
    public int getTotalConnected() {
        checkInitialization();
        return super.getTotalConnected();
    }

    @Override
    public synchronized void sendMessage(Message message) {
        checkInitialization();
        super.sendMessage(message);
    }

    private void checkInitialization() {
        if (mContext == null) {
            throw new IllegalStateException("The manager must be initialized with the init() method.");
        }
    }

    /**
     * Handle devices found by {@link BTServerDeviceFinder}.
     *
     * @param server Ser.
     */
    private void onServerFinderResult(BTDevice server) {
        Log.d(TAG, "onServerFinderResult: " + server);
        if (server != null) {
            Log.d(TAG, "onDevicesFound: Trying connect to server " + server);
            connectToDevices(this::onMessageReceived, server);
        } else {
            sendMessageToUI(PetConnectionMessageType.CONNECTION_NOT_FOUND);
        }
    }

    /**
     * Handle message received form remote device.
     *
     * @param message Message form remote device.
     */
    private void onMessageReceived(Message message) {
        Log.d(TAG, "onMessageReceived: " + message);
        sendMessageToUI(PetConnectionMessageType.MESSAGE_RECEIVED, message);
    }

    @Override
    public void onConnectionEstablished(Connection connection) {
        Log.d(TAG, "onConnectionEstablished: " + connection.getRemoteDevice());
        // Stop trying connection when first connection is successful
        if (stateIs(ManagerState.CONNECTING_TO_REMOTE)) {
            super.onConnectionEstablished(connection);
            cancelOutgoingConnectionsThreads();
            sendMessageToUI(PetConnectionMessageType.CONNECTION_ESTABLISHED);
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
                sendMessageToUI(PetConnectionMessageType.CONNECTION_NOT_FOUND);
            }
        } else {
            super.onConnectionFailure(error);
        }
    }

    @Override
    public void onConnectionLost(Connection connection, ConnectionException error) {
        super.onConnectionLost(connection, error);
        if (getTotalConnected() == 0) {
            Log.d(TAG, "onConnectionLost: " + connection.getRemoteDevice());
            sendMessageToUI(PetConnectionMessageType.CONNECTION_LOST);
        }
    }

    @Override
    public void stopConnectionListener() {
        if (stateIs(ManagerState.LISTENING_TO_CONNECTIONS)) {
            Log.d(TAG, "stopConnectionListener: force stop connection listener");
            super.stopConnectionListener();
            if (getTotalConnected() > 0) {
                sendMessageToUI(PetConnectionMessageType.CONNECTION_ESTABLISHED);
            } else {
                sendMessageToUI(PetConnectionMessageType.CONNECTION_NOT_FOUND);
            }
        }
    }

    private void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                mEnableBTCallback.onEnabled();
            } else {
                sendMessageToUI(PetConnectionMessageType.ERROR_BLUETOOTH_NOT_ENABLED);
            }
        } else if (requestCode == REQUEST_ENABLE_DISCOVERABLE) {
            if (resultCode != Activity.RESULT_CANCELED) {
                mEnableDiscoverableCallback.onEnabled();
            } else {
                sendMessageToUI(PetConnectionMessageType.ERROR_DEVICE_NOT_DISCOVERABLE);
            }
        }
    }

    public static Message newMessage(Serializable data) {
        return new BTMessage(data);
    }

    private void sendMessageToUI(@PetConnectionMessageType int type) {
        sendMessageToUI(type, null);
    }

    private void sendMessageToUI(@PetConnectionMessageType int type, Serializable data) {
        for (PetConnectionMessageHandler petConnectionMessageHandler : mPetConnectionMessageHandlers) {
            petConnectionMessageHandler.handleMessage(new PetConnectionMessage(type, data));
        }
    }

    private void enableBluetooth(OnEnableBluetoothCallback callback) {
        mEnableBTCallback = callback;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.getActivity().startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            callback.onEnabled();
        }
    }

    private void enableDiscoverable(OnEnableDiscoverableCallback callback) {
        mEnableDiscoverableCallback = callback;
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
            mContext.getActivity().startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE);
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

