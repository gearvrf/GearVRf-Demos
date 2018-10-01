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

import android.support.annotation.NonNull;

import org.gearvrf.arpet.service.message.Command;

import java.io.Serializable;

public interface IMessageService {

    /**
     * @param objects  Scene objects to load on remote side.
     * @param callback Returns nothing.
     */
    void shareScene(@NonNull Serializable[] objects, @NonNull MessageServiceCallback<Void> callback);

    /**
     * @param command  Command to execute on remote side.
     * @param callback Returns nothing.
     */
    void sendCommand(@NonNull @Command String command, @NonNull MessageServiceCallback<Void> callback);

    void addMessageReceiver(MessageServiceReceiver receiver);
}