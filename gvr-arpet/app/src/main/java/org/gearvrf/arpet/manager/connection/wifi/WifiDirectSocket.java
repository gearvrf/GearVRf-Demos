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

package org.gearvrf.arpet.manager.connection.wifi;

import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.socket.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WifiDirectSocket implements Socket {

    @Override
    public void connect() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Device getRemoteDevice() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
