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

package org.gearvrf.videoplayer.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;

import org.gearvrf.GVRContext;

import java.util.ArrayList;

public class NetworkManager extends Handler implements Runnable {
    private static final String TAG = NetworkManager.class.getSimpleName();
    private static long TIMER = 5000;
    private ConnectivityManager mConectivtyManager;
    private ArrayList<NetworkListener> mListeners;
    private boolean mIsRunning = false;
    private boolean mIsConnected = false;

    public NetworkManager(GVRContext context) {
        mConectivtyManager = (ConnectivityManager) context.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mListeners = new ArrayList<>();
    }

    public void register(NetworkListener listener){
        if (!mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public void unregister(NetworkListener listener){
        if (mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }

    public boolean isConnected(){
        return  mConectivtyManager.getActiveNetworkInfo() != null
                && mConectivtyManager.getActiveNetworkInfo().isAvailable()
                && mConectivtyManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void run() {
        if (!mIsRunning)
            return;

        boolean connected = isConnected();

        if (mIsConnected != connected) {
            mIsConnected = connected;
            for (NetworkListener listener : mListeners) {
                listener.onConnected(connected);
            }
        }

        postDelayed(this, TIMER);
    }

    /**
     * Start checking network states
     */
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            postDelayed(this, TIMER);
        }
    }

    /**
     * Stops checking network states
     */
    public void stop() {
        if (mIsRunning)
            mIsRunning = false;
    }
}
