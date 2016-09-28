package com.example.vnidamarthy.wificonnect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    ;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private String tag = "MainActivity";
    private ListView listView;
    private static String deviceName;

    public void setDeviceName(String dName)
    {
        deviceName = dName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

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

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = listView.getItemAtPosition(position);
                setDeviceName(obj.toString());
            }
        });
    }

        public void connect2device(View view)
        {
            if(deviceName != null)
            {
                Log.d("shiyu", "before the call with " + deviceName);
                mReceiver.connect2Device(deviceName);
            }
            else
            {
                CharSequence s = "Choose a device";
                Toast.makeText(this.getApplicationContext(), s, Toast.LENGTH_LONG).show();
            }
        }


    private void discoverWifiDevices() {
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
                Log.d(tag, "Could not discover any devices because " + Integer.toString(reasonCode));
            }
        });
    }

    /**
     * @brief Registrer to receiver, discover and connect to the
     * wifi of the device
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
        discoverWifiDevices();
    }

    /**
     * @brief Unregister the wifi receiver whent he app pauses or stops
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class ConnectThisDevice extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            mReceiver.connectDevice(params[0]);
            return null;
        }
    }
}
