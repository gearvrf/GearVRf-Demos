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

package org.gearvrf.arpet.manager.connection.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

import org.gearvrf.arpet.connection.DeviceType;
import org.gearvrf.arpet.manager.connection.BaseDevice;

import java.io.IOException;
import java.util.UUID;

public class BTDevice extends BaseDevice {

    private BluetoothDevice mDevice;

    public BTDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    @Override
    public String getName() {
        return mDevice.getName();
    }

    @Override
    public String getAddress() {
        return mDevice.getAddress();
    }

    @Override
    public int getType() {
        int majorType = mDevice.getBluetoothClass().getMajorDeviceClass();
        switch (majorType) {
            case BluetoothClass.Device.Major.PHONE:
                return DeviceType.PHONE;
            default:
                return DeviceType.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return "BTDevice{" +
                "Name= " + getName() +
                ", Address= " + getAddress() +
                ", Type= " + getType() +
                '}';
    }

    public BTSocket createSocket(UUID uuid) throws IOException {
        return new BTSocket(mDevice.createRfcommSocketToServiceRecord(uuid));
    }
}
