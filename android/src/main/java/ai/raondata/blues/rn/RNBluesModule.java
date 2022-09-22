package ai.raondata.blues.rn;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.reflect.Method;
import java.util.Collection;

import ai.raondata.blues.exception.BluesException;
import ai.raondata.blues.model.NativeDevice;
import ai.raondata.blues.receiver.A2dpConnectionReceiver;
import ai.raondata.blues.receiver.BluetoothStateChangeReceiver;
import ai.raondata.blues.receiver.DiscoveryReceiver;
import ai.raondata.blues.event.EventType;
import ai.raondata.blues.state.BluetoothState;


@SuppressLint("MissingPermission")
public class RNBluesModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "RNBluesModule";

    public final BluetoothAdapter mAdapter;
    private BluetoothA2dp mA2dp;
    private NativeDevice mDevice;

    private DiscoveryReceiver mDiscoveryReceiver;
    private BluetoothStateChangeReceiver mStateChangeReceiver;
    private A2dpConnectionReceiver mConnectReceiver;

    private Promise mConnectPromise;

    public RNBluesModule(ReactApplicationContext context) {
        super(context);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        context.addLifecycleEventListener(this);
        mAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
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

    @Override
    public String getName() {
        return "RNBlues";
    }

    private boolean isBluetoothAvailable() {return mAdapter != null;}
    private boolean isBluetoothEnabled() {return mAdapter.isEnabled();}
    private boolean checkBluetoothAdapter() {return isBluetoothAvailable() && isBluetoothEnabled();}

    @ReactMethod
    public void isBluetoothAvailable(Promise promise) {
        promise.resolve(isBluetoothAvailable());
    }

    @ReactMethod
    public void isBluetoothEnabled(Promise promise) {
        promise.resolve(isBluetoothEnabled());
    }

    @ReactMethod
    public void checkBluetoothAdapter(Promise promise) {
        promise.resolve(checkBluetoothAdapter());
    }

    @ReactMethod
    public void requestBluetoothEnabled(Promise promise) {
        sendEvent(EventType.BLUETOOTH_STATE_CHANGING, null);
        if (!isBluetoothAvailable()) {
            promise.reject(BluesException.BLUETOOTH_NOT_AVAILABLE.name(), BluesException.BLUETOOTH_NOT_AVAILABLE.message());
        } else if (isBluetoothEnabled()) {
            promise.reject(BluesException.ALREADY_ENABLED.name(), BluesException.ALREADY_ENABLED.message());
        } else {
            boolean enabled = mAdapter.enable();
            if (enabled) {
                Log.d(TAG, "Bluetooth enabled");
                sendEvent(EventType.BLUETOOTH_ENABLED, null);
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
        }
    }

    @ReactMethod
    public void deviceList(Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else {
            WritableArray bonded = Arguments.createArray();
            for (BluetoothDevice device : mAdapter.getBondedDevices()) {
                NativeDevice nativeDevice = new NativeDevice(device);
                bonded.pushMap(nativeDevice.map());
            }
            promise.resolve(bonded);
        }
    }


    private void registerDiscoveryReceiver(final Promise promise) {
        mDiscoveryReceiver = new DiscoveryReceiver(new DiscoveryReceiver.Callback() {
            @Override
            public void onDeviceDiscovered(NativeDevice device) {
                Log.d(TAG, "onDeviceDiscovered(): " + device.getAddress());
                sendEvent(EventType.DEVICE_DISCOVERED, device.map());
            }

            public void onDiscoveryFinished(Collection<NativeDevice> devices) {
                Log.d(TAG, "onDiscoveryFinished()");
                WritableMap result = Arguments.createMap();
                WritableArray array = Arguments.createArray();
                for (NativeDevice device : devices) {
                    array.pushMap(device.map());
                }
                result.putArray("result", array);
                sendEvent(EventType.SCAN_STOPPED, result);
                promise.resolve(result);
                mDiscoveryReceiver = null;
            }

            @Override
            public void onDiscoveryFailed(Throwable e) {
                Log.d(TAG, "onDiscoveryFailed()");
                promise.reject(BluesException.DISCOVERY_FAILED.name(), BluesException.DISCOVERY_FAILED.message(e.getMessage()));
                mDiscoveryReceiver = null;
            }
        });

        getReactApplicationContext().registerReceiver(mDiscoveryReceiver, DiscoveryReceiver.intentFilter());
    }

    private void unregisterDiscoveryReceiver() {
        try {
            getReactApplicationContext().unregisterReceiver(mDiscoveryReceiver);
        } catch (IllegalArgumentException iae) {
            Log.w(TAG, iae.getMessage());
        }
    }


    @ReactMethod
    public void startScan(Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else if (mDiscoveryReceiver != null) {
            promise.reject(BluesException.BLUETOOTH_IN_DISCOVERY.name(), BluesException.BLUETOOTH_IN_DISCOVERY.message());
        } else {
            registerDiscoveryReceiver(promise);
            sendEvent(EventType.SCAN_STARTED, null);
            mAdapter.startDiscovery();
        }
    }

    @ReactMethod
    public void stopScan(Promise promise) {
        if (mDiscoveryReceiver != null) {
            getReactApplicationContext().unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        }
        promise.resolve(mAdapter.cancelDiscovery());
    }

    @ReactMethod
    public void connectA2dp(String id, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else {
            unregisterDiscoveryReceiver();
            mConnectPromise = promise;

            // dhpark: Bluetooth device MAC address => get Device instance
            BluetoothDevice device = mAdapter.getRemoteDevice(id);
            if (device != null) {
                mDevice = new NativeDevice(device);

                // bond device
                try {
                    Method mtdBond = device.getClass().getMethod("createBond");
                    mtdBond.invoke(device);
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject(BluesException.BONDING_UNAVAILABLE_API.name(), BluesException.BONDING_UNAVAILABLE_API.message());
                }
                // connect device
                try {
                    Method connectMethod = BluetoothA2dp.class.getMethod("connect", BluetoothDevice.class);
                    connectMethod.invoke(mA2dp, device);
                    promise.resolve(mDevice.map());
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject(BluesException.CONNECTION_FAILED.name(), BluesException.CONNECTION_FAILED.message(device.getName()));
                }
            } else {
                promise.reject(BluesException.BLUETOOTH_DEVICE_NOT_FOUND.name(), BluesException.BLUETOOTH_DEVICE_NOT_FOUND.message());
            }
        }
    }

    @ReactMethod
    public void getConnectedA2dpDevice(Promise promise) {
        promise.resolve(mDevice.map());
    }

    @ReactMethod
    public void disconnectA2dp(Promise promise) {
        try {
            Method disconnectMethod = BluetoothA2dp.class.getMethod("disconnect", BluetoothDevice.class);
            disconnectMethod.invoke(mA2dp, mDevice.getDevice());
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(BluesException.DISCONNECTION_FAILED.name(), BluesException.DISCONNECTION_FAILED.message(mDevice.getName() + ", " + mDevice.getAddress()));
        }

        try {
            Method mtdRemoveBond = mDevice.getDevice().getClass().getMethod("removeBond");
            mtdRemoveBond.invoke(mDevice.getDevice());
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(BluesException.REMOVE_BOND_FAILED.name(), BluesException.REMOVE_BOND_FAILED.message());
        }
        sendEvent(EventType.DEVICE_DISCONNECTED, null);
        promise.resolve(true);
    }


    private void sendEvent(EventType event, @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event.name, params);
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume()");
        if (mStateChangeReceiver == null) {
            mStateChangeReceiver = new BluetoothStateChangeReceiver(new BluetoothStateChangeReceiver.Callback() {
                @Override
                public void onStateChange(BluetoothState newState, BluetoothState oldState) {
                    Log.d(TAG, "onStateChange(): bluetooth state changed");
                    sendEvent(EventType.BLUETOOTH_STATE_CHANGED, null);
                }

                @Override
                public void onBluetoothEnabled() {
                    Log.d(TAG, "onStateChange(): bluetooth enabled");
                    sendEvent(EventType.BLUETOOTH_ENABLED, null);
                }

                @Override
                public void onBluetoothDisabled() {
                    Log.d(TAG, "onStateChange(): bluetooth disabled");
                    sendEvent(EventType.BLUETOOTH_DISABLED, null);
                }
            });
            getReactApplicationContext().registerReceiver(mStateChangeReceiver, BluetoothStateChangeReceiver.intentFilter());
        }

        if (mConnectReceiver == null) {
            mConnectReceiver = new A2dpConnectionReceiver(new A2dpConnectionReceiver.Callback() {
                @Override
                public void onDeviceConnectionChanged() {
                    Log.d(TAG, "onDeviceConnectionChanged: A2dpConnectionReceiver");
                }

                @Override
                public void onDeviceConnected() {
                    Log.d(TAG, "onDeviceConnected: A2dpConnectionReceiver");
                    sendEvent(EventType.DEVICE_CONNECTED, null);
                }

                @Override
                public void onDeviceDisconnected() {
                    Log.d(TAG, "onDeviceDisconnected: A2dpConnectionReceiver");
                    sendEvent(EventType.DEVICE_DISCONNECTED, null);
                    if (mConnectPromise != null) {
                        mConnectPromise.reject(BluesException.ALREADY_CONNECTING.name(), BluesException.ALREADY_CONNECTING.message(mDevice.getName()));
                        mConnectPromise = null;
                    } else {
                        sendEvent(EventType.DEVICE_DISCONNECTED, null);
                    }
                }
            });
            getReactApplicationContext().registerReceiver(mConnectReceiver, A2dpConnectionReceiver.intentFilter());
        }
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause()");
        try {
            getReactApplicationContext().unregisterReceiver(mStateChangeReceiver);
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "IllegalArgumentException: " + iae.getMessage());
        }
        try {
            getReactApplicationContext().unregisterReceiver(mConnectReceiver);
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "IllegalArgumentException: " + iae.getMessage());
        }
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy()");
        mAdapter.cancelDiscovery();
//        mAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dp);
    }
}