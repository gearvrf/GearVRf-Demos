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

package org.gearvrf.arpet.connection.socket;

import android.support.annotation.NonNull;

import org.gearvrf.arpet.connection.Connection;
import org.gearvrf.arpet.connection.ConnectionManager;
import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.ManagerState;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.OnMessageListener;
import org.gearvrf.arpet.connection.exception.ConnectionException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseSocketConnectionManager implements ConnectionManager, OnConnectionListener {

    protected final String TAG = getClass().getSimpleName();

    @ManagerState
    private int mState = ManagerState.IDLE;
    private List<Connection> mOngoingConnections = new ArrayList<>();
    private IncomingSocketConnectionThread mIncomingSocketConnection;
    private int mTotalConnectionsDesired, mTotalConnectionsFailed;
    private List<OutgoingSocketConnectionThread> mOutgoingSocketConnections;

    @ConnectionMode
    private int mConnectionMode;

    public BaseSocketConnectionManager() {
        mOutgoingSocketConnections = new ArrayList<>();
        mConnectionMode = ConnectionMode.NONE;
    }

    @Override
    public synchronized void connectToDevices(
            @NonNull OnMessageListener messageListener,
            @NonNull Device... devices) {

        if (stateIs(ManagerState.IDLE) && devices.length > 0) {

            setState(ManagerState.CONNECTING_TO_REMOTE);
            mTotalConnectionsDesired = devices.length;
            mTotalConnectionsFailed = 0;
            mOngoingConnections.clear();
            mOutgoingSocketConnections.clear();

            for (Device device : devices) {
                OutgoingSocketConnectionThread connectionThread =
                        getSocketConnectionThreadFactory().createOutgoingSocketConnectionThread(
                                device, messageListener, this);
                mOutgoingSocketConnections.add(connectionThread);
                connectionThread.start();
            }
        }
    }

    @Override
    public synchronized void startConnectionListener(
            @NonNull OnMessageListener messageListener) {

        if (stateIs(ManagerState.IDLE)) {
            setState(ManagerState.LISTENING_TO_CONNECTIONS);
            mIncomingSocketConnection = getSocketConnectionThreadFactory()
                    .createIncomingSocketConnectionThread(messageListener, this);
            mIncomingSocketConnection.start();
        }
    }

    @Override
    public synchronized void stopConnectionListener() {
        if (stateIs(ManagerState.LISTENING_TO_CONNECTIONS)) {
            mIncomingSocketConnection.cancel();
            mIncomingSocketConnection = null;
            if (getTotalConnected() == 0) {
                setState(ManagerState.IDLE);
                mConnectionMode = ConnectionMode.NONE;
            } else {
                setState(ManagerState.CONNECTED);
                mConnectionMode = ConnectionMode.SERVER;
            }
        }
    }

    @Override
    public synchronized void sendMessage(Message message) {
        if (stateIs(ManagerState.CONNECTED)) {
            for (Connection connection : mOngoingConnections) {
                connection.write(message);
            }
        }
    }

    @Override
    public synchronized List<Device> getConnectedDevices() {
        List<Device> devices = new ArrayList<>(mOngoingConnections.size());
        for (Connection connection : mOngoingConnections) {
            devices.add(connection.getRemoteDevice());
        }
        return devices;
    }

    @ManagerState
    @Override
    public synchronized int getState() {
        return mState;
    }

    protected synchronized boolean stateIs(@ManagerState int state) {
        return getState() == state;
    }

    private synchronized void setState(@ManagerState int mState) {
        this.mState = mState;
    }

    protected synchronized void cancelOutgoingConnectionsThreads() {
        Iterator<OutgoingSocketConnectionThread> iterator = mOutgoingSocketConnections.iterator();
        while (iterator.hasNext()) {
            OutgoingSocketConnectionThread thread = iterator.next();
            if (!thread.isConnected()) {
                thread.cancel();
                iterator.remove();
            }
        }
        setState(getTotalConnected() > 0 ? ManagerState.CONNECTED : ManagerState.IDLE);
        mConnectionMode = getTotalConnected() > 0 ? ConnectionMode.CLIENT : ConnectionMode.NONE;
    }

    @Override
    public synchronized int getTotalConnected() {
        return mOngoingConnections.size();
    }

    public boolean isConnectedAs(@ConnectionMode int mode) {
        return mConnectionMode == mode;
    }

    @ConnectionMode
    public int getConnectionMode() {
        return mConnectionMode;
    }

    @Override
    public synchronized void disconnect() {
        if (stateIs(ManagerState.CONNECTED)) {
            try {
                for (Connection connection : mOngoingConnections) {
                    connection.close();
                }
                mOngoingConnections.clear();
                setState(ManagerState.IDLE);
                mConnectionMode = ConnectionMode.NONE;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract SocketConnectionThreadFactory getSocketConnectionThreadFactory();

    // CONNECTION LISTENER METHODS

    @Override
    public synchronized void onConnectionEstablished(Connection connection) {
        mOngoingConnections.add(connection);
        ((OngoingSocketConnectionThread) connection).start();
        if (checkOutgoingConnectionThreadsFinished()) {
            setState(ManagerState.CONNECTED);
            mConnectionMode = ConnectionMode.CLIENT;
        }
    }

    @Override
    public synchronized void onConnectionFailure(ConnectionException error) {
        mTotalConnectionsFailed++;
        if (checkOutgoingConnectionThreadsFinished()) {
            if (getTotalConnected() == 0) {
                setState(ManagerState.IDLE);
                mConnectionMode = ConnectionMode.NONE;
            }
        } else if (stateIs(ManagerState.LISTENING_TO_CONNECTIONS)) {
            // When failed while accepting the first connection then set final state IDLE
            // since the listener thread is stopped when any error occurs
            if (getTotalConnected() == 0) {
                setState(ManagerState.IDLE);
                mConnectionMode = ConnectionMode.NONE;
            }

        }
    }

    @Override
    public synchronized void onConnectionLost(Connection connection, ConnectionException error) {
        mOngoingConnections.remove(connection);
        if (mOngoingConnections.size() == 0) {
            setState(ManagerState.IDLE);
            mConnectionMode = ConnectionMode.NONE;
        }
    }

    private synchronized boolean checkOutgoingConnectionThreadsFinished() {
        int connectionsSuccessful = mOngoingConnections.size();
        return stateIs(ManagerState.CONNECTING_TO_REMOTE)
                && (connectionsSuccessful + mTotalConnectionsFailed) == mTotalConnectionsDesired;
    }
}
