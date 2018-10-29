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
import android.support.annotation.NonNull;

import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.Message;
import org.gearvrf.arpet.connection.SendMessageCallback;
import org.gearvrf.arpet.connection.socket.ConnectionMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface IPetConnectionManager {

    // Connection status

    /**
     * At least one connection is active
     */
    int EVENT_CONNECTION_ESTABLISHED = 10;

    /**
     * Connection timeout or no BT device found
     */
    int EVENT_NO_CONNECTION_FOUND = 11;

    /**
     * All connections lost
     */
    int EVENT_ALL_CONNECTIONS_LOST = 12;

    /**
     * A connection was lost
     */
    int EVENT_ONE_CONNECTION_LOST = 13;

    /**
     * Ready to accept connections from guests
     */
    int EVENT_ON_LISTENING_TO_GUESTS = 14;

    /**
     * A connection to host was requested
     */
    int EVENT_ON_REQUEST_CONNECTION_TO_HOST = 15;

    /**
     * Connection from guest established
     */
    int EVENT_GUEST_CONNECTION_ESTABLISHED = 16;

    // Message exchange for ongoing connections

    /**
     * Message received from remote device
     */
    int EVENT_MESSAGE_RECEIVED = 20; //

    // Bluetooth errors

    /**
     * User denied enable bluetooth
     */
    int EVENT_ENABLE_BLUETOOTH_DENIED = 30;

    /**
     * User denied device visibility
     */
    int EVENT_HOST_VISIBILITY_DENIED = 31;

    @IntDef({
            IPetConnectionManager.EVENT_CONNECTION_ESTABLISHED,
            IPetConnectionManager.EVENT_NO_CONNECTION_FOUND,
            IPetConnectionManager.EVENT_ALL_CONNECTIONS_LOST,
            IPetConnectionManager.EVENT_ONE_CONNECTION_LOST,
            IPetConnectionManager.EVENT_ON_LISTENING_TO_GUESTS,
            IPetConnectionManager.EVENT_ON_REQUEST_CONNECTION_TO_HOST,
            IPetConnectionManager.EVENT_GUEST_CONNECTION_ESTABLISHED,
            IPetConnectionManager.EVENT_MESSAGE_RECEIVED,
            IPetConnectionManager.EVENT_ENABLE_BLUETOOTH_DENIED,
            IPetConnectionManager.EVENT_HOST_VISIBILITY_DENIED})
    @Retention(RetentionPolicy.SOURCE)
    @interface EventType {
    }

    /**
     * This method must be called before to use the manager,
     * otherwise an {@link IllegalStateException} will be thrown
     *
     * @param context A context
     */
    void init(@NonNull PetContext context);

    /**
     * Starts connection listener.
     * <br/>If bluetooth requirements are satisfied, the manager notifies the event {@link #EVENT_ON_LISTENING_TO_GUESTS}.
     * Otherwise, notifies the event {@link #EVENT_ENABLE_BLUETOOTH_DENIED} or {@link #EVENT_HOST_VISIBILITY_DENIED}.
     */
    void startInvitation();

    /**
     * Stop listening to connections
     */
    void stopInvitation();

    /**
     * Stop listening to new connections and close all
     * devices previously connected.
     */
    void stopInvitationAndDisconnect();

    /**
     * Find for a server then connect to it immediately.<br/>
     * If bluetooth requirement is satisfied, the manager notifies the event {@link #EVENT_ON_REQUEST_CONNECTION_TO_HOST}.
     * Otherwise, notifies the event {@link #EVENT_ENABLE_BLUETOOTH_DENIED}.
     */
    void findInvitationThenConnect();

    /**
     * Cancels the process of finding a server.
     */
    void stopFindInvitation();

    List<Device> getConnectedDevices();

    void disconnect();

    @ConnectionMode
    int getConnectionMode();

    int getTotalConnected();

    void sendMessage(Message message, @NonNull SendMessageCallback callback);

    void addEventHandler(PetConnectionEventHandler handler);

    void removeEventHandler(PetConnectionEventHandler handler);

    PetContext getContext();
}
