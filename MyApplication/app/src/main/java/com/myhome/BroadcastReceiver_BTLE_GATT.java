package com.myhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Amit Varshney on 12/15/2016
 */
public abstract class BroadcastReceiver_BTLE_GATT extends BroadcastReceiver {

    public boolean mConnected = false;


private  Activity_BTLE_Services activity2;

    public BroadcastReceiver_BTLE_GATT(Activity_BTLE_Services activity2) {
        this.activity2 = activity2;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BroadcastReceiver_BT", "onReceive BroadcastReceiver_BTLE_GATT");
        final String action = intent.getAction();

        if (Service_BTLE_GATT.ACTION_GATT_CONNECTED.equals(action)) {
            //Utils.toast(activity.getApplicationContext(), "Connected to device");
            mConnected = false;
        } else if (Service_BTLE_GATT.ACTION_GATT_DISCONNECTED.equals(action)) {
            mConnected = false;
            //Utils.toast(activity.getApplicationContext(), "Disconnected From Device");
            //activity.finish();
            onDeviceDisconnected();
        } else if (Service_BTLE_GATT.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            //Utils.toast(activity.getApplicationContext(), "Service discovered");
            mConnected = true;

            onDeviceConnected();

        } else if (Service_BTLE_GATT.ACTION_DATA_AVAILABLE.equals(action)) {

            final byte txValue[] = intent.getByteArrayExtra(Service_BTLE_GATT.EXTRA_DATA);
            try {
                //String text = new String(txValue, "UTF-8");
                getResponseDataFromDevice(txValue);

            } catch (Exception e) {
                Log.e("BroadcastReceiver_BT", ""+e.toString());
            }
        }
        return;
    }

    protected abstract void onDeviceConnected();
    protected abstract void getResponseDataFromDevice(byte text[]);
    protected abstract void onDeviceDisconnected();
}
