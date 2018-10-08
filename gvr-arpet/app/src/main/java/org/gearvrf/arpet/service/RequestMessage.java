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

import org.gearvrf.arpet.connection.socket.bluetooth.BTMessage;

public class RequestMessage<Data extends IMessageData> extends BTMessage<Data> {

    private String mActionName;

    public RequestMessage(String mActionName, Data data) {
        super(data);
        this.mActionName = mActionName;
    }

    public String getActionName() {
        return mActionName;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "mActionName='" + mActionName + '\'' +
                "} " + super.toString();
    }
}
