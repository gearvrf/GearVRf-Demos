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

package org.gearvrf.arpet.connection;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.List;

public interface ConnectionManager {

    /**
     * Connect to given devices.
     *
     * @param messageListener Listener to listen messages from remotes.
     * @param devices         Devices to to connect to.
     */
    void connectToDevices(@NonNull OnMessageListener messageListener, @NonNull Device... devices);

    /**
     * Listen to remote connections.
     *
     * @param messageListener Listener to listen messages from remote.
     * @param mMaxConnections Max connections allowed.
     */
    void startConnectionListener(@NonNull OnMessageListener messageListener, @IntRange() int mMaxConnections);

    /**
     * Stop wait for remote connections.
     */
    void stopConnectionListener();

    /**
     * Send message to all connected devices.
     *
     * @param message A message to send.
     */
    void sendMessage(Message message);

    /**
     * Get a list of current connected devices.
     *
     * @return A list of connected devices.
     */
    List<Device> getConnectedDevices();

    /**
     * Return total of connected devices.
     *
     * @return Total of connected devices.
     */
    int getTotalConnected();

    /**
     * Get current manager state.
     *
     * @return One of states indicated in {@link ManagerState}
     */
    @ManagerState
    int getState();

    /**
     * Disconnect from all connected devices.
     */
    void disconnect();
}
