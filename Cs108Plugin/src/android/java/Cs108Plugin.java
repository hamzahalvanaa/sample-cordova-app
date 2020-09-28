package id.nocola.cordova.plugin.cs108;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.apache.cordova.myApp.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import id.nocola.cordova.plugin.cs108.tasks.DeviceConnectTask;

/**
 * This class echoes a string called from JavaScript.
 */
public class Cs108Plugin extends CordovaPlugin {
    private static final String TAG = "Cs108Plugin";
    final boolean DEBUG = false;
    private DeviceScanTask deviceScanTask;
    private DeviceConnectTask deviceConnectTask;
    @SuppressLint("StaticFieldLeak")
    public static Cs108Library4A mCs108Library4a = MainActivity.mCs108Library4a;
    private Handler mHandler = new Handler();

    private ArrayList<Cs108Connector.Cs108ScanData> mScanResultList = new ArrayList<>();
    private ArrayList<ReaderDevice> readersList = MainActivity.sharedObjects.readersList;
    private CallbackContext callbackContext;
    CallbackContext discoverCallback;

    boolean bConnecting = false;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(TAG, "action%s", action);
        this.callbackContext = callbackContext;

        if (action.equals("showToast")) {
            showToast(args);
        } else if (action.equals("scan")) {
            findCs108Devices(callbackContext);
        } else if (action.equals("connect")) {
            String macAddress = args.getString(0);
            connect(callbackContext, macAddress);
        }
        return false;
    }

    public void showToast(final JSONArray args) {
        String message = "";
        String duration = "";
        try {
            JSONObject options = args.getJSONObject(0);
            message = options.getString("message");
            duration = options.getString("duration");
        } catch (JSONException e) {
            callbackContext.error("Error encountered: " + e.getMessage());
        }
        // Create the toast
        Toast toast = Toast.makeText(cordova.getActivity(), message,
                "long".equals(duration) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        // Display toast
        toast.show();
        // Send a positive result to the callbackContext
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "here comes the callback");
        callbackContext.sendPluginResult(pluginResult);
    }

    private void findCs108Devices(CallbackContext context) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                discoverCallback = context;
                boolean operating = false;
                if (mCs108Library4a.isBleConnected()) operating = true;
                if (!operating && deviceScanTask != null) {
                    if (!deviceScanTask.isCancelled()) operating = true;
                }
                if (!operating && deviceConnectTask != null) {
                    if (!deviceConnectTask.isCancelled()) operating = true;
                }
                if (!operating) {
                    deviceScanTask = new DeviceScanTask(context);
                    deviceScanTask.execute();
                    mCs108Library4a.appendToLog("Started DeviceScanTask");
                    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                    result.setKeepCallback(true);
                    discoverCallback.sendPluginResult(result);
                } else {
                    discoverCallback.error("operating true");
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "here comes the callback");
                    discoverCallback.sendPluginResult(pluginResult);
                }
//                    mHandler.postDelayed(checkRunnable, 5000);
            }
        });
    }

    public void connect(CallbackContext callbackContext, String macAddress) {
        if (mCs108Library4a.isBleConnected()) {
            mCs108Library4a.disconnect(false);
        } else if (!mCs108Library4a.isBleConnected()) {
            boolean validStart = false;
            if (deviceConnectTask == null) {
                validStart = true;
            } else if (deviceConnectTask.getStatus() == AsyncTask.Status.FINISHED) {
                validStart = true;
            }
            if (validStart) {
                bConnecting = true;
                if (deviceScanTask != null) deviceScanTask.cancel(true);
                MainActivity.mCs108Library4a.appendToLog("Connecting");
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                        deviceConnectTask = new DeviceConnectTask(cordova.getActivity())
//                    }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeviceScanTask extends AsyncTask<Void, String, String> {
        private long timeMillisUpdate = System.currentTimeMillis();
        ArrayList<ReaderDevice> readersListOld = new ArrayList<ReaderDevice>();
        boolean wait4process = false;
        boolean scanning = false;

        public DeviceScanTask(CallbackContext context) {
            discoverCallback = context;
        }

        @Override
        protected String doInBackground(Void... a) {
            while (isCancelled() == false) {
                if (wait4process == false) {
                    Cs108Connector.Cs108ScanData cs108ScanData = mCs108Library4a.getNewDeviceScanned();
                    if (cs108ScanData != null) mScanResultList.add(cs108ScanData);
                    if (scanning == false || mScanResultList.size() != 0 || System.currentTimeMillis() - timeMillisUpdate > 10000) {
                        wait4process = true;
                        publishProgress("");
                    }
                }
            }
            return "End of Asynctask()";
        }

        @Override
        protected void onProgressUpdate(String... output) {
            if (scanning == false) {
                scanning = true;
                if (mCs108Library4a.scanLeDevice(true) == false) cancel(true);
                else cordova.getActivity().invalidateOptionsMenu();
            }
            boolean listUpdated = false;
            while (mScanResultList.size() != 0) {
                Cs108Connector.Cs108ScanData scanResultA = mScanResultList.get(0);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", scanResultA.device.getName());
                    jsonObject.put("address", scanResultA.device.getAddress());
                    jsonObject.put("rssi", scanResultA.rssi);
                } catch (JSONException e) {
                    discoverCallback.error("Error encountered: " + e.getMessage());
                }
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
                Log.d("scanResult", jsonArray.toString());
                if (discoverCallback != null) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, jsonArray.toString());
                    result.setKeepCallback(true);
                    discoverCallback.sendPluginResult(result);
                }
                mScanResultList.remove(0);
                if (false)
                    mCs108Library4a.appendToLog("scanResultA.device.getType() = " + scanResultA.device.getType() + ". scanResultA.rssi = " + scanResultA.rssi);
                if (scanResultA.device.getType() == BluetoothDevice.DEVICE_TYPE_LE && (true || scanResultA.rssi < 0)) {
                    boolean match = false;
                    for (int i = 0; i < readersList.size(); i++) {
                        if (readersList.get(i).getAddress().matches(scanResultA.device.getAddress())) {
                            ReaderDevice readerDevice1 = readersList.get(i);
                            int count = readerDevice1.getCount();
                            count++;
                            readerDevice1.setCount(count);
                            readerDevice1.setRssi(scanResultA.rssi);
                            readersList.set(i, readerDevice1);
                            listUpdated = true;
                            match = true;
                            break;
                        }
                    }
                    if (match == false) {
                        ReaderDevice readerDevice = new ReaderDevice(scanResultA.device, scanResultA.device.getName(), scanResultA.device.getAddress(), false, "", 1, scanResultA.rssi);
                        String strInfo = "";
                        if (scanResultA.device.getBondState() == 12) {
                            strInfo += "BOND_BONDED\n";
                        }
                        readerDevice.setDetails(strInfo + "scanRecord=" + mCs108Library4a.byteArrayToString(scanResultA.scanRecord));
                        readersList.add(readerDevice);
                        listUpdated = true;
                    }
                } else {
                    if (true)
                        mCs108Library4a.appendToLog("deviceScanTask: rssi=" + scanResultA.rssi + ", error type=" + scanResultA.device.getType());
                }
            }
            if (System.currentTimeMillis() - timeMillisUpdate > 10000) {
                timeMillisUpdate = System.currentTimeMillis();
                for (int i = 0; i < readersList.size(); i++) {
                    ReaderDevice readerDeviceNew = readersList.get(i);
                    boolean matched = false;
                    for (int k = 0; k < readersListOld.size(); k++) {
                        ReaderDevice readerDeviceOld = readersListOld.get(k);
                        if (readerDeviceOld.getAddress().matches(readerDeviceNew.getAddress())) {
                            matched = true;
                            if (readerDeviceOld.getCount() >= readerDeviceNew.getCount()) {
                                readersList.remove(i);
                                listUpdated = true;
                                readersListOld.remove(k);
                            } else readerDeviceOld.setCount(readerDeviceNew.getCount());
                            break;
                        }
                    }
                    if (matched == false) {
                        ReaderDevice readerDevice1 = new ReaderDevice(null, null, readerDeviceNew.getAddress(), false, null, readerDeviceNew.getCount(), 0);
                        readersListOld.add(readerDevice1);
                    }
                }
                if (DEBUG)
                    mCs108Library4a.appendToLog("Matched. Updated readerListOld with size = " + readersListOld.size());
                mCs108Library4a.scanLeDevice(false);
                cordova.getActivity().invalidateOptionsMenu();
                scanning = false;
            }
//        if (listUpdated) readerListAdapter.notifyDataSetChanged();
            wait4process = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mCs108Library4a.appendToLog("Stop Scanning 1A");
            deviceScanEnding();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("result", result);
            mCs108Library4a.appendToLog("Stop Scanning 1B");
            deviceScanEnding();
        }

        void deviceScanEnding() {
            mCs108Library4a.scanLeDevice(false);
        }

    }

//    @Override
//    public void onTaskDone(JSONArray data) {
//        Log.d("callback", callbackContext.toString());
//        if (this.callbackContext != null) {
//            Log.d("datawas=", data.toString());
//            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
//            result.setKeepCallback(false);
//            callbackContext.sendPluginResult(result);
//        }
//    }
}
