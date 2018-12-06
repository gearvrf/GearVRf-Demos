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
import android.support.annotation.NonNull;

import org.gearvrf.arpet.connection.Connection;
import org.gearvrf.arpet.connection.OnConnectionListener;
import org.gearvrf.arpet.connection.OnMessageListener;
import org.gearvrf.arpet.connection.socket.IncomingSocketConnectionThread;
import org.gearvrf.arpet.connection.socket.ServerSocket;

import java.io.IOException;

public final class BTIncomingSocketConnectionThread extends IncomingSocketConnectionThread<BTSocket> {

    private OnMessageListener mMessageListener;

    BTIncomingSocketConnectionThread(
            @NonNull OnMessageListener messageListener,
            @NonNull OnConnectionListener connectionListener) {

        super(connectionListener);
        mMessageListener = messageListener;
    }

    @Override
    protected ServerSocket<BTSocket> getServerSocket() throws IOException {
        return new BTServerSocket(BluetoothAdapter.getDefaultAdapter().
                listenUsingInsecureRfcommWithServiceRecord(BTConstants.SOCKET_SERVER_NAME, BTConstants.SOCKET_SERVER_UUID));
    }

    @Override
    protected Connection createConnection(BTSocket socket) {
        return new BTOngoingSocketConnectionThread(socket, mMessageListener, mOnConnectionListener);
    }

}
