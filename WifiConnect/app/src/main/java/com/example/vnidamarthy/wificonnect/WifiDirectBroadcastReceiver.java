package com.example.vnidamarthy.wificonnect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by v.nidamarthy on 9/14/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.nidamarthy on 8/29/16.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver{
    protected static final String TAG = "wifiBroadcastReceiver";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private String listOfDevices;
    private String deviceaddress;
    protected String str;
    protected boolean flag = true;
    private Context androidContext;
    private List peers = new ArrayList();

    public WifiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiP2pManager.Channel channel,
                                       Context context)
    {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.androidContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            String message = " ";
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Log.d(TAG, "Wifi is enabled");
            }
            else
            {
                Log.d(TAG, "Wifi is not enabled");
            }
        }

        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            if(mManager != null)
            {
                mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList devicelist) {
                        Activity activity = (Activity)androidContext;
                        View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                        ListView listView = (ListView) rootView.findViewById(R.id.listView);
                        if(!peers.isEmpty())
                        {
                            peers.clear();
                        }
                        for(final WifiP2pDevice device : devicelist.getDeviceList())
                        {
                            InetAddress address = null;
                            peers.add(device.deviceName);
                        }
                        //peers.addAll(devicelist.getDeviceList());
                        ArrayAdapter adapter = new ArrayAdapter<String>(androidContext, android.R.layout.simple_list_item_1, peers);
                        listView.setAdapter(adapter);
                    }
                });
            }

        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            // Respond to new connection or disconnections
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            // Respond to this device's wifi state changing
        }
    }

    public void connect2Device(String s)
    {
        for(int i = 0; i < peers.size(); ++i)
        {
            if(peers.get(i) == s)
            {
                connectDevice(s);
            }
        }
    }

    protected void connectDevice(String deviceAddress2) {
        WifiP2pDevice device;
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress2;
        Log.d("shiyu", "in the method with: "+ config.deviceAddress);
        try {
            mManager.connect(mChannel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d("shiyu", "Device Pairing successful");
                    String msg = "Connected: " + config.deviceAddress;
                    CharSequence msgChar = msg;
                    Toast.makeText(androidContext.getApplicationContext(), msgChar, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("shiyu", "Device Pairing failed" + reason);
                }
            });
        }catch (Exception e)
        {
            CharSequence s = e.getMessage();
            Log.d(TAG, ":"+s);
        }
    }

    protected void setDeviceAddress(String deviceAddress2) {
        deviceaddress = deviceAddress2;
    }

    public String getDeviceAddress() {
        return deviceaddress;
    }

}
