package com.myhome;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.UnsupportedEncodingException;

public class Activity_BTLE_Services extends AppCompatActivity{
    public static final String EXTRA_NAME = "com.myhome.Activity_BTLE_Services.NAME";
    public static final String EXTRA_ADDRESS = "com.myhome.Activity_BTLE_Services.ADDRESS";
    private final static String TAG = Activity_BTLE_Services.class.getSimpleName();

    //private String responseData;
    private Intent mBTLE_Service_Intent;
    private Service_BTLE_GATT mBTLE_Service;
    private boolean mBTLE_Service_Bound;
    private ProgressDialog progress;
    private String name;
    private String address;
    private String weightText;
    TextView weightTV;
    Boolean isGetWeight = false;
    //private static OnDataSendListener mOnDataSendListener;

    private ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Service_BTLE_GATT.BTLeServiceBinder binder = (Service_BTLE_GATT.BTLeServiceBinder) service;
            mBTLE_Service = binder.getService();
            mBTLE_Service_Bound = true;

            if (!mBTLE_Service.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mBTLE_Service.connect(address);
            mBTLE_Service.enableTXNotification();

            // Automatically connects to the device upon successful start-up initialization.
//            mBTLeService.connect(mBTLeDeviceAddress);

//            mBluetoothGatt = mBTLeService.getmBluetoothGatt();
//            mGattUpdateReceiver.setBluetoothGatt(mBluetoothGatt);
//            mGattUpdateReceiver.setBTLeService(mBTLeService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTLE_Service = null;
            mBTLE_Service_Bound = false;
            Log.d(TAG, "onServiceDisconnected");
//            mBluetoothGatt = null;
//            mGattUpdateReceiver.setBluetoothGatt(null);
//            mGattUpdateReceiver.setBTLeService(null);
        }
    };
    private BroadcastReceiver_BTLE_GATT mGattUpdateReceiver = new BroadcastReceiver_BTLE_GATT(this) {
        @Override
        protected void onDeviceConnected() {

            progress.dismiss();
            Log.d(TAG, "onDeviceConnected");
            //sendCommand("G");

            /*Log.d(TAG, "sending data to device.");

            String message = "0123";
            byte[] value;
            try {
                //send data to service
                value = message.getBytes("UTF-8");
                mBTLE_Service.writeRXCharacteristic(value);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        protected void getResponseDataFromDevice(byte responseData[]) {
            //responseData = text;
            //Log.d(TAG, "response : " + responseData);
            byte data[] = new byte[4];
            float weight = 0;
            Log.d(TAG, "getResponseDataFromDevice");
            if(((char) responseData[0]) == 'M')
            {
                weight = (((byte)responseData[1] & 0xFF) << 24) | (((byte)responseData[2] & 0xFF) << 16) | (((byte)responseData[3] & 0xFF) << 8) | ((byte)responseData[4] & 0xFF);
                //weight /= 1000; //b
                //Log.d("aa", "weight = " + weight);

                weightText = String.valueOf(weight);
                //weightText += " Kg";

                weightTV.setText(weightText);
            }
            if(((char) responseData[0]) == 'W')
            {
                weight = (((byte)responseData[1] & 0xFF) << 24) | (((byte)responseData[2] & 0xFF) << 16) | (((byte)responseData[3] & 0xFF) << 8) | ((byte)responseData[4] & 0xFF);
                weight /= 1000; //b
                //Log.d("aa", "weight = " + weight);

                weightText = String.valueOf(weight);
                //weightText += " Kg";

                weightTV.setText(weightText);
            }


        }

        @Override
        protected void onDeviceDisconnected() {
            Log.d(TAG, "onDeviceDisconnected");
            unregisterReceiver(mGattUpdateReceiver);
            unbindService(mBTLE_ServiceConnection);
            mBTLE_Service_Intent = null;
            Intent intent = new Intent(Activity_BTLE_Services.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("DISCONNECT");
        adb.setCancelable(true);
        adb.setMessage("Do you want to disconnect");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBTLE_Service.disconnect();
                dialog.dismiss();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    Button button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_page);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        name = intent.getStringExtra(Activity_BTLE_Services.EXTRA_NAME);
        address = intent.getStringExtra(Activity_BTLE_Services.EXTRA_ADDRESS);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);


        mBTLE_Service_Intent = new Intent(this, Service_BTLE_GATT.class);
        bindService(mBTLE_Service_Intent, mBTLE_ServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mBTLE_Service_Intent);

        registerReceiver(mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
        progress = new ProgressDialog(this);
        progress.setMessage("Connecting...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);
        progress.show();

        button1 = (Button) findViewById(R.id.Button1);

        weightTV = (TextView) findViewById(R.id.weightText);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText t=(EditText)findViewById(R.id.editText);
                String s=t.getText().toString();
                sendCommand(s);
                Log.d(TAG, s+" send");
                if(!isGetWeight)
                {
                    isGetWeight = true;
                   // button1.setText("STOP");
                }
                else
                {
                    isGetWeight = false;
                   // button1.setText("START");
                }
            }
        });

    }







    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mBTLE_Service.disconnect();
    }


    public void sendCommand(String cmd) {
        byte[] value;
        Log.d(TAG, "sendCommand()");
        try {
            //send data to service
            value = cmd.getBytes("UTF-8");
            mBTLE_Service.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "sending data to device.");
    }

}

