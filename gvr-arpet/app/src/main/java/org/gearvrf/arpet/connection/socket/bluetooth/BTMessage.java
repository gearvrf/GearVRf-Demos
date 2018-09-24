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

package org.gearvrf.arpet.connection.socket.bluetooth;

import org.gearvrf.arpet.connection.Device;
import org.gearvrf.arpet.connection.Message;

import java.io.Serializable;

public class BTMessage implements Message {

    private Serializable mData;
    private static int sId;
    private int mId;

    public BTMessage(Serializable data) {
        mId = incrementId();
        mData = data;
    }

    private static int incrementId() {
        return ++sId;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public Device getDevice() {
        return LocalBluetoothDevice.getDefault();
    }

    @Override
    public Serializable getData() {
        return mData;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + getId() +
                ", device=" + getDevice() +
                ", data=" + getData() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BTMessage btMessage = (BTMessage) o;
        return mId == btMessage.mId;
    }

    @Override
    public int hashCode() {
        return mId;
    }
}
