package com.covle.cordova.plugin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Set;

public class TaffPrint extends CordovaPlugin {

    private final String TAG = "coo";
    private BluetoothAdapter mBtAdapter;
    private ArrayList<String> mPairedDevices;



    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        Log.d(TAG, "Something is happening");

        if (action.equals("scan")){
            scan();


        } else if (action.equals("greet")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);

        } else {
            return false;
        }


        return true;
    }

    public void scan(){
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = new ArrayList<String>();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevices.add(device.getName() + "\n" + device.getAddress());
                Log.d(TAG, device.getName());
            }
        } else {
            String noDevices = "No devices";
            mPairedDevices.add(noDevices);
        }
    }


}