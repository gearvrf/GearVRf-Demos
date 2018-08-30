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

import org.gearvrf.arpet.connection.exception.ConnectionException;

/**
 * Interface called when an connection event occurs.
 */
public interface OnConnectionListener {

    /**
     * Called when a connection from remote is established successful.
     *
     * @param connection A connection from remote.
     */
    void onConnectionEstablished(Connection connection);

    /**
     * Called when an error occurs trying connect to a remote.
     *
     * @param failure The failure info.
     */
    void onConnectionFailure(ConnectionException failure);

    /**
     * Called when a connection from remote has been lost.
     *
     * @param connection The info of lost connection.
     * @param error      If exists, the cause of lost connection.
     */
    void onConnectionLost(Connection connection, ConnectionException error);
}
