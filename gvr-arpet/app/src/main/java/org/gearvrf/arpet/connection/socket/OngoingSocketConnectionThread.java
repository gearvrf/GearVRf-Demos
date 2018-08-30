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
import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.OnMessageListener;
import org.gearvrf.arpet.connection.exception.ConnectionException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OngoingSocketConnectionThread extends SocketConnectionThread implements Connection {

    private Socket mSocket;
    private ObjectInputStream mInStream;
    private ObjectOutputStream mOutStream;
    private OnMessageListener mMessageListener;
    private OnConnectionListener mOnConnectionListener;

    public OngoingSocketConnectionThread(
            @NonNull Socket socket,
            @NonNull OnMessageListener messageListener,
            @NonNull OnConnectionListener listener) {

        mSocket = socket;
        mMessageListener = messageListener;
        mOnConnectionListener = listener;

        try {
            mInStream = new ObjectInputStream(socket.getInputStream());
            mOutStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            mOnConnectionListener.onConnectionFailure(
                    new ConnectionException("Error opening connection to remote " + getRemoteDevice(), e));
        }
    }

    @Override
    public void run() {

        while (true) {
            try {
                Message message = (Message) mInStream.readObject();
                mMessageListener.onMessageReceived(message);
            } catch (IOException | ClassNotFoundException e) {
                if (!mSocket.isConnected()) {
                    mOnConnectionListener.onConnectionLost(this, null);
                } else {
                    mOnConnectionListener.onConnectionLost(this,
                            new ConnectionException("Error reading from remote " + getRemoteDevice(), e));
                }
                break;
            }
        }
    }

    @Override
    public void write(Message message) {
        try {
            mOutStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Device getRemoteDevice() {
        return mSocket.getRemoteDevice();
    }

    @Override
    public void close() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
