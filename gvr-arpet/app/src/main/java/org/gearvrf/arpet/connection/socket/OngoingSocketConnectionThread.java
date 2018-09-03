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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.gearvrf.arpet.connection.Connection;
import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.OnMessageListener;
import org.gearvrf.arpet.connection.exception.ConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OngoingSocketConnectionThread extends SocketConnectionThread implements Connection {

    private Socket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private OnMessageListener mMessageListener;
    private OnConnectionListener mOnConnectionListener;
    private Gson mGson;

    public OngoingSocketConnectionThread(
            @NonNull Socket socket,
            @NonNull OnMessageListener messageListener,
            @NonNull OnConnectionListener listener) {

        mSocket = socket;
        mMessageListener = messageListener;
        mOnConnectionListener = listener;
        mGson = new GsonBuilder().create();

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

                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;

                while ((length = mInStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                    if (mInStream.available() == 0) {
                        break;
                    }
                }

                Message message = mGson.fromJson(result.toString("UTF-8"), Message.class);
                mMessageListener.onMessageReceived(message);

            } catch (IOException e) {
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
            String m = mGson.toJson(message);
            mOutStream.write(mGson.toJson(message).getBytes());
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
