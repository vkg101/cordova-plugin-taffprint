package com.covle.cordova.plugin;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.covle.sdk.Command;
import com.covle.sdk.PrintPicture;
import com.covle.sdk.PrinterCommand;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Set;

public class TaffPrint extends CordovaPlugin {

    private final String LOGO = "logo";
    private final String TAG = "coo";
    private BluetoothAdapter mBtAdapter;
    private JSONArray mPairedDevices;
    private CallbackContext mBtConnectCallback;

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;
    public static final int MESSAGE_UNABLE_CONNECT = 7;
    public static final String DEVICE_NAME = "device_name";

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services
    private BluetoothService mService = null;



    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        init();

        if (action.equals("scan")){
            scan(callbackContext);
        } else if (action.equals("connect")){
            connect(data.getString(0), callbackContext);
        } else if (action.equals("status")){
            status(callbackContext);
        } else if (action.equals("disconnect")){
            mService.stop();
            callbackContext.success("disconnected");
        } else if (action.equals("printLogo")){
            printLogo(data.getString(0), callbackContext);
        } else if (action.equals("print")){
            sendDataString(data.getString(0), callbackContext);
        } else {
            return false;
        }

        return true;
    }

    public void status(CallbackContext callback){
        int state = mService.getState();
        switch (state) {
            case BluetoothService.STATE_LISTEN:
            case BluetoothService.STATE_NONE:
                callback.success("disconnected");
            case BluetoothService.STATE_CONNECTED:
                callback.success("connected");
            case BluetoothService.STATE_CONNECTING:
                callback.success("connecting");
        }

        callback.error("Unknown state");
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
            mService = new BluetoothService(mHandler);
        }
    }

    public void connect(String address, CallbackContext callback){
        //init();
        mBtConnectCallback = callback;
        PluginResult result;

        if (mBluetoothAdapter == null){
            result = new PluginResult(PluginResult.Status.ERROR, "No bluetooth adapter");
        } else if (BluetoothAdapter.checkBluetoothAddress(address)) {
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            // Attempt to connect to the device
            mService.connect(device);

            result = new PluginResult(PluginResult.Status.OK, "Connecting...");
            //Magically keep the callback.
            result.setKeepCallback(true);
        } else {
            result = new PluginResult(PluginResult.Status.ERROR, "Can't find Printer");
        }

        mBtConnectCallback.sendPluginResult(result);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PluginResult result;
            boolean persist = true;

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            result = new PluginResult(PluginResult.Status.OK, "connected");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            result = new PluginResult(PluginResult.Status.OK, "connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            result = new PluginResult(PluginResult.Status.OK, "disconnected");
                            persist = false;
                            break;
                        default:
                            result = new PluginResult(PluginResult.Status.ERROR, "unknown");
                            persist = false;
                    }
                    break;
                /*case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    result = new PluginResult(PluginResult.Status.OK, "Getting device name");
                    break;*/
                case MESSAGE_CONNECTION_LOST:
                    result = new PluginResult(PluginResult.Status.OK, "disconnected");
                    persist = false;
                    break;
                case MESSAGE_UNABLE_CONNECT:
                    result = new PluginResult(PluginResult.Status.OK, "cant-connect");
                    persist = false;
                    break;
                default:
                    result = new PluginResult(PluginResult.Status.NO_RESULT, "irrelevant");
            }

            result.setKeepCallback(persist);
            mBtConnectCallback.sendPluginResult(result);
        }
    };

    private void sendDataString(String data, CallbackContext callback) {

        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            callback.error("Not connected");
            return;
        }
        if (data.length() > 0) {
            try {
                mService.write(data.getBytes("GBK"));
                callback.success("Printed.");
            } catch (UnsupportedEncodingException e) {

            }
        } else {
            callback.error("Nothing to write...");
        }
    }

    private void sendDataByte(byte[] data) {

        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            //todo return not connected?
            return;
        }
        mService.write(data);
    }

    private void printLogo(String path, CallbackContext callback){

        Bitmap mBitmap;


        try {
            //Resources resources = cordova.getActivity().getResources();
            //String packageName = cordova.getActivity().getPackageName();
            //int id = resources.getIdentifier(LOGO, "drawable", packageName);

            //mBitmap = BitmapFactory.decodeResource(resources, id);
            mBitmap = BitmapFactory.decodeFile(path);
        } catch (Exception ex) {
            callback.error("Error getting bitmap");
            return;
        }


        int nMode = 0;
        int nPaperWidth = 384;
        if(mBitmap != null)
        {
            byte[] data = PrintPicture.POS_PrintBMP(mBitmap, nPaperWidth, nMode);
            sendDataByte(Command.ESC_Init);
            sendDataByte(Command.LF);
            sendDataByte(data);
            sendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(30));
            sendDataByte(PrinterCommand.POS_Set_Cut(1));
            sendDataByte(PrinterCommand.POS_Set_PrtInit());

            callback.success("Image printed.");
        } else {
            callback.error("Bitmap "+path+" is null");
        }
    }

}