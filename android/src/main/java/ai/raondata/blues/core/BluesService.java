package ai.raondata.blues.core;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;

import java.lang.reflect.Method;

class BluesService {
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothA2dp mA2dp;
    BluetoothDevice mmDevice = null;

    public BluesService(ReactApplicationContext reactContext, BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
        bluetoothAdapter.getProfileProxy(reactContext, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    mA2dp = (BluetoothA2dp) proxy;
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.A2DP) {
                    mA2dp = null;
                }
            }
        }, BluetoothProfile.A2DP);

    }

    public void connectA2dp(BluetoothDevice device, Promise promise) {
        mmDevice = device;
        try {
            Method connectMethod = BluetoothA2dp.class.getMethod("connect", BluetoothDevice.class);
            connectMethod.invoke(mA2dp, mmDevice);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("BLUES_CONNECTION_ERROR", "Bluetooth A2dp connection error");
        }
    }

    public void disconnectA2dp() {
        try {
            Method connectMethod = BluetoothA2dp.class.getMethod("disconnect", BluetoothDevice.class);
            connectMethod.invoke(mA2dp, mmDevice);
            mmDevice = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createBond(BluetoothDevice device) {
        try {
            Method bond = device.getClass().getMethod("createBond");
            return (boolean) bond.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBond() {
        try {
            Method bond = mmDevice.getClass().getMethod("removeBond");
            return (boolean) bond.invoke(mmDevice, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
