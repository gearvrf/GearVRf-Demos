package com.example.vnidamarthy.myapplication;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private String tag = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void discoverWifiDevices()
    {
        // TODO Auto-generated method stub
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Log.d(tag, "Discovered devices");

            }

            @Override
            public void onFailure(int reasonCode) {
                // TODO Auto-generated method stub
                Log.d(tag, "Could not discover any devices because "+ Integer.toString(reasonCode));
            }
        });
    }

    /**@brief Registrer to receiver, discover and connect to the
     *        wifi of the device
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver,intentFilter);
        discoverWifiDevices();
    }

    /**@brief Unregister the wifi receiver whent he app pauses or stops
     * */
    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}
