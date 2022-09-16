package ai.raondata.blues.core;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.Set;

import ai.raondata.blues.rn.RNBluesModule;
import ai.raondata.blues.util.Util;

public class Blues {
    public final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Promise connectPromise = null;
    private ReactApplicationContext mReactContext;
    private final BluesService bluesService;
    private BluetoothReceiver bluetoothReceiver;
    private boolean isScanning = false;
    private boolean isConnecting = false;

    public boolean isScanning() {
        return isScanning;
    }

    public void setScanning(boolean scanning) {
        isScanning = scanning;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean connecting) {
        isConnecting = connecting;
    }

    public Blues(ReactApplicationContext reactContext) {
        mReactContext = reactContext;
        bluesService = new BluesService(reactContext, bluetoothAdapter);
        this.registerBluetoothDeviceReceiver();
    }

    public void list(Promise promise) {
        WritableArray deviceList = Arguments.createArray();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice rawDevice : bondedDevices) {
                WritableMap device = Util.deviceToWritableMap(rawDevice);
                deviceList.pushMap(device);
            }
        }
        promise.resolve(deviceList);
    }

    public void startDiscovery() {
        isScanning = true;
        bluetoothAdapter.startDiscovery();
    }

    public void stopDiscovery() {
        bluetoothAdapter.cancelDiscovery();
        isScanning = false;
    }

    public void connectA2dp(String id, Promise promise) {
        bluetoothAdapter.cancelDiscovery();
        connectPromise = promise;
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(id);
        if (device != null) {
            boolean isok = bluesService.createBond(device);
            bluesService.connectA2dp(device, promise);
        } else {
            promise.reject("BLUES_CONNECTION_ERROR", "Bluetooth device not found.");
        }
    }

    public void disconnectA2dp(Promise promise) {
        bluesService.disconnectA2dp();
        boolean success = bluesService.removeBond();
        if (success) {
            promise.resolve(success);
        } else {
            promise.reject("BLUES_DISCONNECT_ERROR", "Bluetooth device disconnection error");
        }
    }

    private void registerBluetoothDeviceReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        bluetoothReceiver = new BluetoothReceiver();
        mReactContext.registerReceiver(bluetoothReceiver, intentFilter);
    }

    public class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                WritableMap wDevice = Util.deviceToWritableMap(device);
                RNBluesModule.sendEvent("onScan", wDevice);
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                RNBluesModule.sendEvent("scanStop", "");
            }

            if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int currentState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
                if (currentState == BluetoothA2dp.STATE_CONNECTED) {
                    connectPromise.resolve("A2dp connection succeeded.");
                    RNBluesModule.sendEvent("connectSucceeded", "");
                }
                if (currentState == BluetoothA2dp.STATE_DISCONNECTED) {
                    RNBluesModule.sendEvent("connectDisconnect", "");
                    connectPromise.reject("BLUES_CONNECTION_ERROR", "A2dp connection failed.");
                }
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    connectPromise.reject("BLUES_BOND_ERROR", "A2dp pairing failed.");
                }
            }
        }
    }
}
