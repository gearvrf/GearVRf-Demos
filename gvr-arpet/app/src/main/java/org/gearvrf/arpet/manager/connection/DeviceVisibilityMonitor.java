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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.IntDef;

import org.gearvrf.arpet.PetContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class DeviceVisibilityMonitor {

    static final int VISIBILITY_ON = 10;
    static final int VISIBILITY_OFF = 11;

    private PetContext mPetContext;
    private OnVisibilityChangeListener mVisibilityListener;
    private BluetoothAdapter mBluetoothAdapter;
    private LocalReceiver mReceiver;

    public DeviceVisibilityMonitor(PetContext context, OnVisibilityChangeListener listener) {
        this.mPetContext = context;
        this.mVisibilityListener = listener;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void setEnabled(boolean enabled) {
        try {
            if (enabled) {
                if (mReceiver == null) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                    mReceiver = new LocalReceiver();
                    mPetContext.getActivity().registerReceiver(mReceiver, filter);
                }
            } else {
                if (mReceiver != null) {
                    mPetContext.getActivity().unregisterReceiver(mReceiver);
                    mReceiver = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    mVisibilityListener.onChange(VISIBILITY_ON);
                } else {
                    mVisibilityListener.onChange(VISIBILITY_OFF);
                }
            }
        }
    }

    @FunctionalInterface
    public interface OnVisibilityChangeListener {
        void onChange(@State int scanMode);
    }

    @IntDef({VISIBILITY_ON, VISIBILITY_OFF})
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
    }
}
