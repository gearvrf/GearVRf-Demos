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
import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.exception.ConnectionException;

import java.io.IOException;

public abstract class IncomingSocketConnectionThread<S extends Socket> extends SocketConnectionThread {

    private ServerSocket<S> mServerSocket;
    protected OnConnectionListener mOnConnectionListener;

    public IncomingSocketConnectionThread(@NonNull OnConnectionListener listener) {
        mOnConnectionListener = listener;
        try {
            mServerSocket = getServerSocket();
        } catch (IOException e) {
            mOnConnectionListener.onConnectionFailure(
                    new ConnectionException("Error starting connections listener", e));
        }
    }

    @Override
    public void run() {

        setName("IncomingSocketConnectionThread");

        try {
            while (true) {
                try {
                    Connection connection = createConnection(mServerSocket.accept());
                    mOnConnectionListener.onConnectionEstablished(connection);
                } catch (IOException e) {
                    mOnConnectionListener.onConnectionFailure(
                            new ConnectionException("Error accepting connections", e));
                    break;
                }
            }
        } finally {
            // Max connections reached or can't accept more connections for any reason
            cancel();
        }
    }

    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract ServerSocket<S> getServerSocket() throws IOException;

    protected abstract Connection createConnection(S socket);
}
