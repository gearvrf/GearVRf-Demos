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

package org.gearvrf.arpet.manager.connection;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        PetConnectionEventType.CONN_CONNECTION_ESTABLISHED,
        PetConnectionEventType.CONN_NO_CONNECTION_FOUND,
        PetConnectionEventType.CONN_ALL_CONNECTIONS_LOST,
        PetConnectionEventType.CONN_ONE_CONNECTION_LOST,
        PetConnectionEventType.CONN_ON_LISTENING_TO_GUESTS,
        PetConnectionEventType.CONN_ON_REQUEST_CONNECTION_TO_HOST,
        PetConnectionEventType.CONN_GUEST_CONNECTION_ESTABLISHED,
        PetConnectionEventType.MSG_MESSAGE_RECEIVED,
        PetConnectionEventType.ERR_ENABLE_BLUETOOTH_DENIED,
        PetConnectionEventType.ERR_HOST_VISIBILITY_DENIED})
public @interface PetConnectionEventType {

    // Connection status

    /**
     * At least one connection is active
     */
    int CONN_CONNECTION_ESTABLISHED = 10;

    /**
     * Connection timeout or no BT device found
     */
    int CONN_NO_CONNECTION_FOUND = 11;

    /**
     * All connections lost
     */
    int CONN_ALL_CONNECTIONS_LOST = 12;

    /**
     * A connection was lost
     */
    int CONN_ONE_CONNECTION_LOST = 13;

    /**
     * Ready to accept connections from guests
     */
    int CONN_ON_LISTENING_TO_GUESTS = 14;

    /**
     * A connection to host was requested
     */
    int CONN_ON_REQUEST_CONNECTION_TO_HOST = 15;

    /**
     * Connection from guest established
     */
    int CONN_GUEST_CONNECTION_ESTABLISHED = 16;

    // Message exchange for ongoing connections

    /**
     * Message received from remote device
     */
    int MSG_MESSAGE_RECEIVED = 20; //

    // Bluetooth errors

    /**
     * User denied enable bluetooth
     */
    int ERR_ENABLE_BLUETOOTH_DENIED = 30;

    /**
     * User denied device visibility
     */
    int ERR_HOST_VISIBILITY_DENIED = 31;
}