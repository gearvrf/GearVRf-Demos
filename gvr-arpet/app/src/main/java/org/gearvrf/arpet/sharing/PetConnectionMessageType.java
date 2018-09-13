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

package org.gearvrf.arpet.sharing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
        PetConnectionMessageType.CONNECTION_ESTABLISHED,
        PetConnectionMessageType.CONNECTION_NOT_FOUND,
        PetConnectionMessageType.CONNECTION_LOST,
        PetConnectionMessageType.CONNECTION_LISTENER_STARTED,
        PetConnectionMessageType.MESSAGE_RECEIVED,
        PetConnectionMessageType.ERROR_BLUETOOTH_NOT_ENABLED,
        PetConnectionMessageType.ERROR_DEVICE_NOT_DISCOVERABLE})
public @interface PetConnectionMessageType {

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
    int CONNECTION_LOST = 12;

    /**
     * Listener ready to accept connections
     */
    int CONNECTION_LISTENER_STARTED = 13;

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
