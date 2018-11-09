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
public @interface EventType {
}
