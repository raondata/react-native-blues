package ai.raondata.blues.rn;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
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
import java.util.Timer;

import ai.raondata.blues.event.EventType;
import ai.raondata.blues.exception.BluesException;
import ai.raondata.blues.model.NativeDevice;
import ai.raondata.blues.receiver.A2dpConnectionReceiver;
import ai.raondata.blues.receiver.BluetoothStateChangeReceiver;
import ai.raondata.blues.receiver.DiscoveryReceiver;
import ai.raondata.blues.state.BluetoothState;


@SuppressLint("MissingPermission")
public class RNBluesModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String TAG = "RNBluesModule";

    public final BluetoothAdapter mAdapter;
    private BluetoothA2dp mA2dp;
    private NativeDevice mDevice;

    private DiscoveryReceiver mDiscoveryReceiver;
    private BluetoothStateChangeReceiver mBluetoothStateReceiver;
    private A2dpConnectionReceiver mConnectionStateReceiver;

    private Promise mConnectPromise;

    private Timer scanTimer;

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

    private void sendRNEvent(EventType event, @Nullable WritableMap params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event.name, params);
    }

    private boolean isBluetoothAvailable() {return mAdapter != null;}
    private boolean isBluetoothEnabled() {return mAdapter.isEnabled();}
    private boolean checkBluetoothAdapter() {return isBluetoothAvailable() && isBluetoothEnabled();}

    private void registerDiscoveryReceiver(final Promise promise) {
        Log.d(TAG, "registerDiscoveryReceiver()");
        if (mDiscoveryReceiver == null) {
            mDiscoveryReceiver = new DiscoveryReceiver(new DiscoveryReceiver.Callback() {
                @Override
                public void onDeviceDiscovered(NativeDevice device) {
                    Log.d(TAG, "onDeviceDiscovered(): " + device.getAddress());
//                    onScan.invoke(device.map());
                    sendRNEvent(EventType.DEVICE_DISCOVERED, device.map());
                }

                public synchronized void onDiscoveryFinished(Collection<NativeDevice> devices) {
                    Log.d(TAG, "onDiscoveryFinished()");
                    WritableMap result = Arguments.createMap();
                    WritableArray array = Arguments.createArray();
                    for (NativeDevice device : devices) {
                        array.pushMap(device.map());
                    }
                    result.putArray("result", array);
                    promise.resolve(result);
                    unregisterDiscoveryReceiver();
                }

                @Override
                public void onDiscoveryFailed(Throwable e) {
                    Log.d(TAG, "onDiscoveryFailed()");
                    promise.reject(BluesException.DISCOVERY_FAILED.name(), BluesException.DISCOVERY_FAILED.message(e.getMessage()));
                    sendRNEvent(EventType.ERROR, BluesException.DISCOVERY_FAILED.map());
                    unregisterDiscoveryReceiver();
                }
            });
            getReactApplicationContext().registerReceiver(mDiscoveryReceiver, DiscoveryReceiver.intentFilter());
        } else {
            Log.w(TAG, "DiscoveryReceiver already registered.");
        }
    }

    private void registerBluetoothStateReceiver() {
        if (mBluetoothStateReceiver == null) {
            mBluetoothStateReceiver = new BluetoothStateChangeReceiver(new BluetoothStateChangeReceiver.Callback() {
                @Override
                public void onStateChange(BluetoothState newState, BluetoothState oldState) {
                    Log.d(TAG, "onStateChange(): bluetooth state changed");
                    WritableMap map = Arguments.createMap();
                    map.putInt("state", newState.code);
                    sendRNEvent(EventType.BLUETOOTH_STATE_CHANGED, map);
                }

                @Override
                public void onBluetoothEnabled() {
                    Log.d(TAG, "onStateChange(): bluetooth enabled");
                    sendRNEvent(EventType.BLUETOOTH_ENABLED, null);
                }

                @Override
                public void onBluetoothDisabled() {
                    Log.d(TAG, "onStateChange(): bluetooth disabled");
                    sendRNEvent(EventType.BLUETOOTH_DISABLED, null);
                }
            });
            getReactApplicationContext().registerReceiver(mBluetoothStateReceiver, BluetoothStateChangeReceiver.intentFilter());
        } else {
            Log.w(TAG, "BluetoothStateReceiver already registered.");
        }
    }

    private void registerConnectionStateReceiver() {
        if (mConnectionStateReceiver == null) {
            mConnectionStateReceiver = new A2dpConnectionReceiver(new A2dpConnectionReceiver.Callback() {
                @Override
                public void onDeviceConnectionChanged() {
                    Log.d(TAG, "onDeviceConnectionChanged: A2dpConnectionReceiver");
                    sendRNEvent(EventType.CONNECTION_STATE_CHANGED, null);
                }

                @Override
                public void onDeviceConnected() {
                    Log.d(TAG, "onDeviceConnected: A2dpConnectionReceiver");
                    sendRNEvent(EventType.DEVICE_CONNECTED, null);
                    unregisterDiscoveryReceiver();
                }

                @Override
                public void onDeviceDisconnected() {
                    Log.d(TAG, "onDeviceDisconnected: A2dpConnectionReceiver");
                    sendRNEvent(EventType.DEVICE_DISCONNECTED, null);
                    if (mConnectPromise != null) {
                        mConnectPromise.reject(BluesException.ALREADY_CONNECTING.name(), BluesException.ALREADY_CONNECTING.message(mDevice.getName()));
                        mConnectPromise = null;
                    } else {
                        sendRNEvent(EventType.DEVICE_DISCONNECTED, null);
                    }
                }
            });
            getReactApplicationContext().registerReceiver(mConnectionStateReceiver, A2dpConnectionReceiver.intentFilter());
        } else {
            Log.w(TAG, "ConnectionStateReceiver already registered.");
        }
    }

    private void unregisterDiscoveryReceiver() {
        Log.d(TAG, "unregisterDiscoveryReceiver()");
        try {
            getReactApplicationContext().unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        } catch (IllegalArgumentException iae) {
            Log.w(TAG, iae.getMessage());
        }
    }

    private void unregisterBluetoothStateReceiver() {
        try {
            getReactApplicationContext().unregisterReceiver(mBluetoothStateReceiver);
            mBluetoothStateReceiver = null;
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "IllegalArgumentException: " + iae.getMessage());
        }
    }

    private void unregisterConnectionStateReceiver() {
        try {
            getReactApplicationContext().unregisterReceiver(mConnectionStateReceiver);
            mConnectionStateReceiver = null;
        } catch (IllegalArgumentException iae) {
            Log.d(TAG, "IllegalArgumentException: " + iae.getMessage());
        }
    }

    private void close() {
        unregisterBluetoothStateReceiver();
        unregisterConnectionStateReceiver();
        mAdapter.cancelDiscovery();
        mAdapter.closeProfileProxy(BluetoothA2dp.A2DP, mA2dp);
    }

    /* ============================= React methods ============================= */

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
        sendRNEvent(EventType.BLUETOOTH_STATE_CHANGING, null);
        if (!isBluetoothAvailable()) {
            promise.reject(BluesException.BLUETOOTH_NOT_AVAILABLE.name(), BluesException.BLUETOOTH_NOT_AVAILABLE.message());
        } else if (isBluetoothEnabled()) {
            promise.reject(BluesException.ALREADY_ENABLED.name(), BluesException.ALREADY_ENABLED.message());
        } else {
            boolean enabled = mAdapter.enable();
            if (enabled) {
                Log.d(TAG, "Bluetooth enabled");
                sendRNEvent(EventType.BLUETOOTH_ENABLED, null);
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
        }
    }

    @ReactMethod
    public void disableBluetooth(Promise promise) {
        if (!isBluetoothAvailable()) {
            promise.reject(BluesException.BLUETOOTH_NOT_AVAILABLE.name(), BluesException.BLUETOOTH_NOT_AVAILABLE.message());
        } else {
            promise.resolve(mAdapter.disable());
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

    @ReactMethod
    public void startScan(Promise promise) {
        // dhpark: FATAL ISSUES
        // 1. promise와 callback으로 처리하려 했는데 Java Native 단에서 RuntimeException 발생
        // => react native method의 경우 Promise와 callback 둘다 사용할 수 없음
        // 2. DiscoveryReceiver 에서 BluetoothDevice.ACTION_FOUND intent를 받을 때마다 callback을 호출하려
        //    했는데 오류 발생
        // => react native callback은 1회 호출만 가능

        Log.d(TAG, "::::: startScan :::::");
        if (!checkBluetoothAdapter()) {
            Log.e(TAG, "bluetooth adapter not available");
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
//            return false;
        } else if (mDiscoveryReceiver != null) {
            Log.e(TAG, "bluetooth is scanning");
            promise.reject(BluesException.BLUETOOTH_IN_DISCOVERY.name(), BluesException.BLUETOOTH_IN_DISCOVERY.message());
//            return false;
        } else {
            registerDiscoveryReceiver(promise);
            mAdapter.startDiscovery();
            sendRNEvent(EventType.SCAN_STARTED, null);
//            return true;
        }
    }

    @ReactMethod
    public void stopScan() {
        Log.d(TAG, ">>>>>> RNBluesModule.stopScan()");
        mAdapter.cancelDiscovery();
        sendRNEvent(EventType.SCAN_STOPPED, null);
    }

    @ReactMethod
    public void connectA2dp(String address, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else {
            unregisterDiscoveryReceiver();
            mConnectPromise = promise;

            // dhpark: Bluetooth device MAC address => get Device instance
            BluetoothDevice device = mAdapter.getRemoteDevice(address);
            if (device != null) {
                mDevice = new NativeDevice(device);

                // bond device
                try {
                    Method mtdBond = device.getClass().getMethod("createBond");
                    mtdBond.invoke(device);
                } catch (Exception e) {
                    e.printStackTrace();
                    mConnectPromise.reject(BluesException.BONDING_UNAVAILABLE_API.name(), BluesException.BONDING_UNAVAILABLE_API.message());
                    mConnectPromise = null;
                }
                // connect device
                try {
                    Method connectMethod = BluetoothA2dp.class.getMethod("connect", BluetoothDevice.class);
                    connectMethod.invoke(mA2dp, device);
                    mConnectPromise.resolve(mDevice.map());
                } catch (Exception e) {
                    e.printStackTrace();
                    mConnectPromise.reject(BluesException.CONNECTION_FAILED.name(), BluesException.CONNECTION_FAILED.message(device.getName()));
                }
                mConnectPromise = null;
            } else {
                mConnectPromise.reject(BluesException.BLUETOOTH_DEVICE_NOT_FOUND.name(), BluesException.BLUETOOTH_DEVICE_NOT_FOUND.message());
                mConnectPromise = null;
            }
        }
    }

    @ReactMethod
    public void getConnectedA2dpDevice(Promise promise) {
        promise.resolve(mDevice != null ? mDevice.map() : null);
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
        sendRNEvent(EventType.DEVICE_DISCONNECTED, null);
        promise.resolve(true);
    }

    @ReactMethod
    public void close(Promise promise) {
        close();
        promise.resolve(true);
    }
    /* ============================= React methods ============================= */



    /* ============================= Lifecycle Events ============================= */

    @Override
    public void initialize() {
        super.initialize();
        registerBluetoothStateReceiver();
        registerConnectionStateReceiver();
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "************LifecycleEventListener************ : onHostResume()");
//        registerBluetoothStateReceiver();
//        registerConnectionStateReceiver();
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "************LifecycleEventListener************ : onHostPause()");
//        unregisterBluetoothStateReceiver();
//        unregisterConnectionStateReceiver();
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "************LifecycleEventListener************ : onHostDestroy()");
        close();
    }
}
