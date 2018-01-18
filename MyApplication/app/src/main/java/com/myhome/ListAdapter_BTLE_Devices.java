package com.myhome;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/*
 * Created by Amit Varshney on 12/15/2016
 */
public class ListAdapter_BTLE_Devices extends ArrayAdapter<BTLE_Device> {

    Activity activity;
    int layoutResourceID;
    ArrayList<BTLE_Device> devices;
    customButtonListener customListner;

    public interface customButtonListener {
        public void onButtonClickListner(int position);
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }

    public ListAdapter_BTLE_Devices(Activity activity, int resource, ArrayList<BTLE_Device> objects) {
        super(activity.getApplicationContext(), resource, objects);

        this.activity = activity;
        layoutResourceID = resource;
        devices = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        BTLE_Device device = devices.get(position);
        String name = device.getName();
        String address = device.getAddress();

        TextView tv = null;

        tv = (TextView) convertView.findViewById(R.id.tv_name);
        if (name != null && name.length() > 0) {
            tv.setText(device.getName());
        }
        else {
            tv.setText("No Name");
        }


        tv = (TextView) convertView.findViewById(R.id.tv_macaddr);
        if (address != null && address.length() > 0) {
            tv.setText(device.getAddress());
        }
        else {
            tv.setText("No Address");
        }

        RelativeLayout btn_Connect = (RelativeLayout) convertView.findViewById(R.id.btn_Connect);
        btn_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = null;
                //Utils.toast(activity, "Connect button clicked");
                if (customListner != null) {
                    customListner.onButtonClickListner(position);
                }
            }
        });


        return convertView;

    }
}
