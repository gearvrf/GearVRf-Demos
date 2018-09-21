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
        PetConnectionEventType.CONNECTION_ESTABLISHED,
        PetConnectionEventType.CONNECTION_NOT_FOUND,
        PetConnectionEventType.CONNECTION_ALL_LOST,
        PetConnectionEventType.CONNECTION_ONE_LOST,
        PetConnectionEventType.CONNECTION_LISTENER_STARTED,
        PetConnectionEventType.MESSAGE_RECEIVED,
        PetConnectionEventType.ERROR_BLUETOOTH_NOT_ENABLED,
        PetConnectionEventType.ERROR_DEVICE_NOT_DISCOVERABLE})
public @interface PetConnectionEventType {

    // Connection status

    /**
     * At least one connection is active
     */
    int CONNECTION_ESTABLISHED = 10;

    /**
     * Connection timeout or no BT device discovered
     */
    int CONNECTION_NOT_FOUND = 11;

    /**
     * All connections lost
     */
    int CONNECTION_ALL_LOST = 12;

    /**
     * A connection was lost
     */
    int CONNECTION_ONE_LOST = 13;

    /**
     * Listener ready to accept connections
     */
    int CONNECTION_LISTENER_STARTED = 14;

    // Message exchange for ongoing connections

    /**
     * Message received from remote device
     */
    int MESSAGE_RECEIVED = 20; //

    // Bluetooth errors

    /**
     * User denied enable bluetooth
     */
    int ERROR_BLUETOOTH_NOT_ENABLED = 30;

    /**
     * User denied enable bluetooth discoverable
     */
    int ERROR_DEVICE_NOT_DISCOVERABLE = 31;
}
