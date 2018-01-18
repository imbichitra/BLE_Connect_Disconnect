package com.myhome;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Activity_BTLE_Services extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTRA_NAME = "com.myhome.Activity_BTLE_Services.NAME";
    public static final String EXTRA_ADDRESS = "com.myhome.Activity_BTLE_Services.ADDRESS";
    private final static String TAG = Activity_BTLE_Services.class.getSimpleName();
    List<Button_Row_Items> rowItems = new ArrayList<Button_Row_Items>();
    ListView mylistview;
    //private String responseData;
    private Intent mBTLE_Service_Intent;
    private Service_BTLE_GATT mBTLE_Service;
    private boolean mBTLE_Service_Bound;
    private ProgressDialog progress;
    private String name;
    private String address;
    private String newButtonName = new String();
    private int switchCount = 0, switchState = 1;
    private ServiceConnection mBTLE_ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

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

//            mBluetoothGatt = null;
//            mGattUpdateReceiver.setBluetoothGatt(null);
//            mGattUpdateReceiver.setBTLeService(null);
        }
    };
    private BroadcastReceiver_BTLE_GATT mGattUpdateReceiver = new BroadcastReceiver_BTLE_GATT(this) {
        @Override
        protected void onDeviceConnected() {

            progress.dismiss();

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
        protected void getResponseDataFromDevice(String responseData) {
            //responseData = text;
            Log.d(TAG, "response : " + responseData + "length : " + responseData.length());

            if(responseData.charAt(0) == 'G'){
                int strLength;
                for(strLength = 0; strLength < 20; strLength++) {
                    if(responseData.charAt(strLength) == 0xFF) {
                        Log.d(TAG, "responseData length : " + strLength);
                        break;
                    }
                }
                String buttonName = responseData.substring(4, ((int) responseData.charAt(1) + 4) );
                boolean buttonState = false;
                if(responseData.charAt(3) == '1') {
                    buttonState = true;
                }
                switchCount = responseData.charAt(2);
                addRow(buttonName, switchCount, buttonState);
            }

            //if(responseData.toString().get == 'G')

        }

        @Override
        protected void onDeviceDisconnected() {
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

    Button button1, button2, button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btle_services);

        Intent intent = getIntent();
        name = intent.getStringExtra(Activity_BTLE_Services.EXTRA_NAME);
        address = intent.getStringExtra(Activity_BTLE_Services.EXTRA_ADDRESS);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(name);
        mylistview = (ListView) findViewById(R.id.listView2);


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

        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte value[] = {0x30, 0x56, 0x0a, 0x7e, 0x54, 0x54, 0x63, (byte) 0x87, 0x4b, 0x12};
                mBTLE_Service.writeRXCharacteristic(value);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte value[] = {0x33, 0x4f, 0x11, 0x4c, 0x7e, 0x54, 0x54, 0x63, (byte) 0x87, 0x4b, 0x1b, 0x02, 0x14, 0x11, 0x0b, 0x07, 0x6e};
                mBTLE_Service.writeRXCharacteristic(value);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte value[] = {0x33, 0x4f, 0x11, 0x55, 0x7e, 0x54, 0x54, 0x63, (byte) 0x87, 0x4b, 0x1b, 0x02, 0x14, 0x11, 0x0b, 0x09, 0x63};
                mBTLE_Service.writeRXCharacteristic(value);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_btle_service, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            OpenAddButtonDialogBox();
            return true;
        } else if (id == R.id.action_disconnect) {
            mBTLE_Service.disconnect();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        /*unregisterReceiver(mGattUpdateReceiver);
        unbindService(mBTLE_ServiceConnection);
        mBTLE_Service_Intent = null;*/
    }

    @Override
    public void onClick(View v) {
        String message = "12334";
        byte[] value;
        try {
            //send data to service
            value = message.getBytes("UTF-8");
            mBTLE_Service.writeRXCharacteristic(value);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void OpenAddButtonDialogBox() {

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.add_button_dialog, null);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add New Button");
        alert.setView(promptView);

        final EditText input = (EditText) promptView
                .findViewById(R.id.etButtonName);

        input.requestFocus();
        input.setHint("Enter Button Name");
        input.setTextColor(Color.BLACK);
        newButtonName = null;

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                newButtonName = input.getText().toString();
                addRow(newButtonName, switchCount, true);

                String message = "A";
                message += switchCount;
                message += switchState;
                message += newButtonName;
                sendCommand(message);

                switchCount++;
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

        // create an alert dialog
        final AlertDialog alertDialog = alert.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(input.getText().toString().isEmpty()) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void addRow(String buttonName, int buttonID, boolean buttonState) {
        Button_Row_Items item = new Button_Row_Items(buttonName, buttonID, buttonState);
        rowItems.add(item);

        ListAdapter_Add_Button adapter = new ListAdapter_Add_Button(this, rowItems);
        mylistview.setAdapter(adapter);

    }

    public void sendCommand(String cmd){
        byte[] value;
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

