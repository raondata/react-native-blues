package ai.raondata.blues.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.lang.reflect.Method;
import java.util.Collection;

import ai.raondata.blues.exception.BluesException;
import ai.raondata.blues.state.BluetoothState;
import ai.raondata.blues.event.EventType;
import ai.raondata.blues.receiver.A2dpConnectionReceiver;
import ai.raondata.blues.receiver.DiscoveryReceiver;
import ai.raondata.blues.receiver.BluetoothStateChangeReceiver;
import ai.raondata.blues.state.BluetoothRequest;
import ai.raondata.blues.model.NativeDevice;
import ai.raondata.blues.rn.RNBluesModule;
import ai.raondata.blues.util.Util;

@SuppressLint("MissingPermission")
public class Blues {
    private static final String TAG = Blues.class.getName();
    private final Context mContext;

    public final BluetoothAdapter mAdapter;
    private BluetoothA2dp mA2dp;
    private BluetoothDevice mDevice;

    private DiscoveryReceiver mDiscoveryReceiver;
    private BluetoothStateChangeReceiver mStateChangeReceiver;
    private A2dpConnectionReceiver mConnectReceiver;

    private Promise mEnabledPromise;
    private Promise mConnectPromise;

    public Blues(ReactApplicationContext reactContext) {
        mContext = reactContext;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.getProfileProxy(reactContext, new BluetoothProfile.ServiceListener() {
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

    public boolean isBluetoothAvailable() {
        return mAdapter != null;
    }

    public boolean checkBluetoothAdapter() {
        return isBluetoothAvailable() && mAdapter.isEnabled();
    }

    public void requestBluetoothEnabled(Activity activity, Promise promise) {
        if (checkBluetoothAdapter()) {
            promise.resolve(true);
        } else {
            if (activity != null) {
                mEnabledPromise = promise;

                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(intent, BluetoothRequest.ENABLE_BLUETOOTH.code);
            } else {
                ActivityNotFoundException e = new ActivityNotFoundException();
                mEnabledPromise.reject(e);
                mEnabledPromise = null;
            }
        }
    }


    public void list(Promise promise) {
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

    public void startDiscovery(final Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else if (mDiscoveryReceiver != null) {
            promise.reject(BluesException.BLUETOOTH_IN_DISCOVERY.name(), BluesException.BLUETOOTH_IN_DISCOVERY.message());
        } else {
            mDiscoveryReceiver = new DiscoveryReceiver(new DiscoveryReceiver.Callback() {
                @Override
                public void onDeviceDiscovered(NativeDevice device) {
                    Log.d(TAG, "onDeviceDiscovered(): " + device.getAddress());
                    RNBluesModule.sendEvent(EventType.SCAN_STARTED, device.map());
                }

                public void onDiscoveryFinished(Collection<NativeDevice> devices) {
                    WritableMap result = Arguments.createMap();
                    WritableArray array = Arguments.createArray();
                    for (NativeDevice device : devices) {
                        array.pushMap(device.map());
                    }
                    result.putArray("result", array);
                    RNBluesModule.sendEvent(EventType.SCAN_STOPPED, result);
                    promise.resolve(result);
                    mDiscoveryReceiver = null;
                }

                @Override
                public void onDiscoveryFailed(Throwable e) {
                    promise.reject(BluesException.DISCOVERY_FAILED.name(), BluesException.DISCOVERY_FAILED.message(e.getMessage()));
                    mDiscoveryReceiver = null;
                }
            });
            mContext.registerReceiver(mDiscoveryReceiver, DiscoveryReceiver.intentFilter());
            mAdapter.startDiscovery();
        }
    }

    public boolean isDiscovering() {
        return mDiscoveryReceiver != null && mAdapter.isDiscovering();
    }

    public boolean stopDiscovery() {
        if (mDiscoveryReceiver != null) {
            mContext.unregisterReceiver(mDiscoveryReceiver);
            mDiscoveryReceiver = null;
        }
        return mAdapter.cancelDiscovery();
    }

    public void connectA2dp(String id, final Promise promise) {
        if (!checkBluetoothAdapter()) {
            promise.reject(BluesException.BLUETOOTH_NOT_ENABLED.name(), BluesException.BLUETOOTH_NOT_ENABLED.message());
        } else {
            stopDiscovery();
            mConnectPromise = promise;

            BluetoothDevice device = mAdapter.getRemoteDevice(id);
            if (device != null) {
                mDevice = device;
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
        promise.resolve(true);
    }

    public void getConnectedA2dpDevice(Promise promise) {
        promise.resolve(Util.deviceToWritableMap(mDevice));
    }

    public void close() {
        mAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dp);
    }

    public void registerBluetoothReceivers() {
        if (mStateChangeReceiver == null) {
            mStateChangeReceiver = new BluetoothStateChangeReceiver(new BluetoothStateChangeReceiver.Callback() {
                @Override
                public void onStateChange(BluetoothState newState, BluetoothState oldState) {
                    RNBluesModule.sendEvent(EventType.BLUETOOTH_STATE_CHANGED, null);
                }

                @Override
                public void onBluetoothEnabled() {
                    RNBluesModule.sendEvent(EventType.BLUETOOTH_ENABLED, null);
                }

                @Override
                public void onBluetoothDisabled() {
                    RNBluesModule.sendEvent(EventType.BLUETOOTH_DISABLED, null);
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
                    RNBluesModule.sendEvent(EventType.DEVICE_CONNECTED, null);
                }

                @Override
                public void onDeviceDisconnected() {
                    RNBluesModule.sendEvent(EventType.DEVICE_DISCONNECTED, null);
                    if (mConnectPromise != null) {
                        mConnectPromise.reject(BluesException.ALREADY_CONNECTING.name(), BluesException.ALREADY_CONNECTING.message());
                        mConnectPromise = null;
                    } else {
                        RNBluesModule.sendEvent(EventType.DEVICE_DISCONNECTED, null);
                    }
                }
            });
        }
    }

    public void unregisterBluetoothReceivers() {
        mContext.unregisterReceiver(mStateChangeReceiver);
    }
}
