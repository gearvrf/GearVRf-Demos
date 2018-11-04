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
import org.gearvrf.arpet.connection.WriteErrorCallback;
import org.gearvrf.arpet.connection.WriteSuccessCallback;
import org.gearvrf.arpet.connection.exception.ClosedConnectionException;
import org.gearvrf.arpet.connection.exception.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class OngoingSocketConnectionThread extends SocketConnectionThread implements Connection {

    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
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
            mInStream = socket.getInputStream();
            mOutStream = socket.getOutputStream();
        } catch (IOException e) {
            mOnConnectionListener.onConnectionFailure(
                    new ConnectionException("Error opening connection to remote " +
                            getRemoteDevice() + ": " + e.getMessage(), e));
        }
    }

    @Override
    public void run() {

        while (true) {
            try {
                Message message = readMessage();
                if (message != null) {
                    mMessageListener.onMessageReceived(message);
                }
            } catch (IOException e) {
                handleIOException(e);
                break;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(@NonNull Message message, WriteSuccessCallback successCallback, WriteErrorCallback errorCallback) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(mOutStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            if (successCallback != null) {
                successCallback.onSuccess();
            }
        } catch (Exception e) {
            if (!mSocket.isConnected()) {
                ClosedConnectionException exc = new ClosedConnectionException("Connection closed", e);
                if (errorCallback != null) {
                    errorCallback.onError(exc);
                }
                mOnConnectionListener.onConnectionLost(this, exc);
            } else {
                if (errorCallback != null) {
                    errorCallback.onError(e);
                }
            }
        }
    }

    private Message readMessage() throws IOException, ClassNotFoundException {
        return (Message) new ObjectInputStream(mInStream).readObject();
    }

    private void handleIOException(IOException e) {
        if (!mSocket.isConnected()) {
            mOnConnectionListener.onConnectionLost(this, new ClosedConnectionException("Connection closed", e));
        } else {
            mOnConnectionListener.onConnectionLost(this, new ConnectionException(e));
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
