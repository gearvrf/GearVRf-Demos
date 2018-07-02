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
