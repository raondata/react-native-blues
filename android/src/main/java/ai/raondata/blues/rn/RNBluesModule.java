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
    private static final String TAG = RNBluesModule.class.getName();
    private final ReactApplicationContext mContext;

    public final BluetoothAdapter mAdapter;
    private BluetoothA2dp mA2dp;
    private NativeDevice mDevice;

    private DiscoveryReceiver mDiscoveryReceiver;
    private BluetoothStateChangeReceiver mStateChangeReceiver;
    private A2dpConnectionReceiver mConnectReceiver;

    private Promise mConnectPromise;

    public RNBluesModule(ReactApplicationContext context) {
        super(context);
        this.mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
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
//        blues = new Blues(reactContext);
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
        if (!isBluetoothAvailable()) {
            promise.reject(BluesException.BLUETOOTH_NOT_AVAILABLE.name(), BluesException.BLUETOOTH_NOT_AVAILABLE.message());
        } else if (!isBluetoothEnabled()) {
            promise.reject(BluesException.ALREADY_ENABLED.name(), BluesException.ALREADY_ENABLED.message());
        } else {
            boolean enabled = mAdapter.enable();
            if (enabled) {
                sendEvent(EventType.BLUETOOTH_ENABLED, null);
                promise.resolve(true);
            } else {
                promise.reject(BluesException.BLUETOOTH_ENABLE_FAILED.name(), BluesException.BLUETOOTH_ENABLE_FAILED.message());
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

    @ReactMethod
    public void startScan(Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else if (mDiscoveryReceiver != null) {
            promise.reject(BluesException.BLUETOOTH_IN_DISCOVERY.name(), BluesException.BLUETOOTH_IN_DISCOVERY.message());
        } else {
            mDiscoveryReceiver = new DiscoveryReceiver(new DiscoveryReceiver.Callback() {
                @Override
                public void onDeviceDiscovered(NativeDevice device) {
                    Log.d(TAG, "onDeviceDiscovered(): " + device.getAddress());
                    sendEvent(EventType.SCAN_STARTED, device.map());
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

            mContext.registerReceiver(mDiscoveryReceiver, DiscoveryReceiver.intentFilter());
            mAdapter.startDiscovery();
        }
    }

    @ReactMethod
    public void stopScan(Promise promise) {
        if (mDiscoveryReceiver != null) {
            mContext.unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        }
        promise.resolve(mAdapter.cancelDiscovery());
    }

    @ReactMethod
    public void connectA2dp(String id, Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else {
            mContext.unregisterReceiver(mDiscoveryReceiver);
            mAdapter.cancelDiscovery();
            mConnectPromise = promise;

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
                    promise.resolve(device);
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject(BluesException.CONNECTION_FAILED.name(), BluesException.CONNECTION_FAILED.message());
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
            disconnectMethod.invoke(mA2dp, mDevice);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(BluesException.DISCONNECTION_FAILED.name(), BluesException.DISCONNECTION_FAILED.message(mDevice.getName() + ", " + mDevice.getAddress()));
        }

        try {
            Method mtdRemoveBond = mDevice.getClass().getMethod("removeBond");
            mtdRemoveBond.invoke(mDevice);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(BluesException.REMOVE_BOND_FAILED.name(), BluesException.REMOVE_BOND_FAILED.message());
        }
        sendEvent(EventType.DEVICE_DISCONNECTED, null);
        promise.resolve(true);
    }


    synchronized private void sendEvent(EventType event, @Nullable WritableMap params) {
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(event.code, params);
    }

    @Override
    public void onHostResume() {
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
            mContext.registerReceiver(mStateChangeReceiver, BluetoothStateChangeReceiver.intentFilter());
        }

        if (mConnectReceiver == null) {
            mConnectReceiver = new A2dpConnectionReceiver(new A2dpConnectionReceiver.Callback() {
                @Override
                public void onDeviceConnectionChanged() {
                }

                @Override
                public void onDeviceConnected() {
                    sendEvent(EventType.DEVICE_CONNECTED, null);
                }

                @Override
                public void onDeviceDisconnected() {
                    sendEvent(EventType.DEVICE_DISCONNECTED, null);
                    if (mConnectPromise != null) {
                        mConnectPromise.reject(BluesException.ALREADY_CONNECTING.name(), BluesException.ALREADY_CONNECTING.message());
                        mConnectPromise = null;
                    } else {
                        sendEvent(EventType.DEVICE_DISCONNECTED, null);
                    }
                }
            });
            mContext.registerReceiver(mConnectReceiver, A2dpConnectionReceiver.intentFilter());
        }
    }

    @Override
    public void onHostPause() {
        mContext.unregisterReceiver(mStateChangeReceiver);
        mContext.unregisterReceiver(mConnectReceiver);
    }

    @Override
    public void onHostDestroy() {
        mAdapter.cancelDiscovery();
        mAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dp);
    }
}