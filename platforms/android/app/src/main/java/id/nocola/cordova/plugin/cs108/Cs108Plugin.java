package id.nocola.cordova.plugin.cs108;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Build;
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
import org.apache.cordova.myApp.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import id.nocola.cordova.plugin.cs108.adapters.ReaderListAdapter;
import id.nocola.cordova.plugin.cs108.tasks.DeviceConnectTask;
import id.nocola.cordova.plugin.cs108.tasks.DeviceScanTask;
import id.nocola.cordova.plugin.cs108.tasks.TaskListenerItf;

/**
 * This class echoes a string called from JavaScript.
 */
public class Cs108Plugin extends CordovaPlugin implements TaskListenerItf {
    private static final String TAG = "Cs108Plugin";
    final boolean DEBUG = false;
    private DeviceScanTask deviceScanTask;
    private DeviceConnectTask deviceConnectTask;
    private ReaderListAdapter readerListAdapter;
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
        final TaskListenerItf itf = (TaskListenerItf) this;

        if (action.equals("showToast")) {
            showToast(args);
        } else if (action.equals("scan")) {
            findCs108Devices(callbackContext, itf);
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

    private void findCs108Devices(CallbackContext context, TaskListenerItf taskListenerItf) {
        //        cordova.getActivity().runOnUiThread(new Runnable() {
////            @Override
////            public void run() {
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

                    String json = new Gson().toJson(readersList);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                    Log.i("Test", "Sampai disini");
                    discoverCallback.sendPluginResult(result);
                } else {
//                    discoverCallback.error("operating true");
//                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "here comes the callback");
//                    discoverCallback.sendPluginResult(pluginResult);
                    String json = new Gson().toJson(readersList);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                    Log.i("Test", "Sampai disini");
                    discoverCallback.sendPluginResult(result);
                }
//                    mHandler.postDelayed(checkRunnable, 5000);
//            }
//        });
    }

    public void connect(final JSONArray args, CallbackContext callbackContext, String macAddress) {
        try {
            if (bConnecting) return;

            int positionId = args.getInt(0);
            readerListAdapter = new ReaderListAdapter(cordova.getActivity(), R.layout.readers_list_item, readersList, true, true);
            ReaderDevice readerDevice = readerListAdapter.getItem(positionId);
            mCs108Library4a.appendToLog("bConnecting = " + bConnecting + ", postion = " + positionId);
            boolean bSelectOld = readerDevice.getSelected();

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        deviceConnectTask = new DeviceConnectTask(cordova.getActivity(), positionId, readerDevice, "Connecting with " + readerDevice.getName());
                        deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        deviceConnectTask = new DeviceConnectTask(cordova.getActivity(), positionId, readerDevice, "Connecting with " + readerDevice.getName());
                        deviceConnectTask.execute();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskDone(JSONArray data) {
        Log.d("callback", callbackContext.toString());
        if (this.callbackContext != null) {
            Log.d("datawas=", data.toString());
            PluginResult result = new PluginResult(PluginResult.Status.OK, data);
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        }
    }
}
