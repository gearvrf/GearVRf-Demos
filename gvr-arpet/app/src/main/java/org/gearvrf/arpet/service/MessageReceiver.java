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

package org.gearvrf.arpet.service;

import org.gearvrf.arpet.service.data.SharedObject;
import org.gearvrf.arpet.service.data.SharedScene;
import org.gearvrf.arpet.service.data.ViewCommand;

public interface MessageReceiver {
    /**
     * Resolve end load the objects represented by given shared scene.
     *
     * @param sharedScene Object to load.
     * @throws MessageException If an exception occurs.
     */
    void onReceiveSharedScene(SharedScene sharedScene) throws MessageException;

    /**
     * Execute the given command on this device
     *
     * @param command CommandType to execute.
     * @throws MessageException If an exception occurs
     */
    void onReceiveViewCommand(ViewCommand command) throws MessageException;

    /**
     * @param sharedObject Object to update
     * @throws MessageException If an exception occurs
     */
    void onReceiveUpdateSharedObject(SharedObject sharedObject) throws MessageException;
}
