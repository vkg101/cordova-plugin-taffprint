package com.covle.cordova.plugin;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class TaffPrint extends CordovaPlugin {

    private final String TAG = "coo";
    private BluetoothAdapter mBtAdapter;
    private JSONArray mPairedDevices;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services
    private BluetoothService mService = null;



    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("scan")){
            scan(callbackContext);
        } else if (action.equals("connect")){
            connect(data.getString(0), callbackContext);
        } else if (action.equals("greet")) {
            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);
        } else {
            return false;
        }


        callbackContext.error("Unknown action");
        return true;
    }

    public void scan(CallbackContext callbackContext){
        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = new JSONArray();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                JSONObject d = new JSONObject();

                try{
                    d.put("name", device.getName());
                    d.put("address", device.getAddress());
                    mPairedDevices.put(d);
                } catch (Exception ex) {
                    //cry
                }
            }
        } else {
            callbackContext.error("No paired devices");
        }


        JSONObject json = new JSONObject();
        try {
            json.put("devices", mPairedDevices);
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
        } catch (Exception ex){
            callbackContext.error("Json error...");
        }
    }

    private void init(){
        if (mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (mService == null) {
            mService = new BluetoothService();
        }
    }

    public void connect(String address, CallbackContext callback){
        init();

        if (mBluetoothAdapter == null){
            callback.error("No bluetooth devide");
        }

        // Get the BluetoothDevice object
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            BluetoothDevice device = mBluetoothAdapter
                    .getRemoteDevice(address);
            // Attempt to connect to the device
            mService.connect(device);

            callback.success("Connecting to printer");
        } else {
            callback.error("Can't find printer");
        }
    }

}