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

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({ManagerState.IDLE,
        ManagerState.LISTENING_TO_CONNECTIONS,
        ManagerState.CONNECTING_TO_REMOTE,
        ManagerState.CONNECTED})
public @interface ManagerState {
    int IDLE = 0; // ready to connect to remote or listen to connections
    int LISTENING_TO_CONNECTIONS = 1;
    int CONNECTING_TO_REMOTE = 2;
    int CONNECTED = 3; // at least one connection established
}
