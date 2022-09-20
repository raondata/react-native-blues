package ai.raondata.blues.rn;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import ai.raondata.blues.core.Blues;
import ai.raondata.blues.exception.BluesException;
import ai.raondata.blues.state.BluetoothRequest;
import ai.raondata.blues.event.EventType;

public class RNBluesModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static ReactApplicationContext reactContext;
    public Blues blues = null;

    private Promise mEnabledPromise;

//    private AudioManager mAudioManager = null;

    public RNBluesModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
//        mAudioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        blues = new Blues(reactContext);
        reactContext.addActivityEventListener((requestCode, resultCode, data) -> {
            if (requestCode == BluetoothRequest.ENABLE_BLUETOOTH.code) {
                if (resultCode == Activity.RESULT_OK) {
                    if (mEnabledPromise != null) {
                        sendEvent(EventType.BLUETOOTH_ENABLED, null);
                        mEnabledPromise.resolve(true);
                    }
                } else {
                    if (mEnabledPromise != null) {
                        mEnabledPromise.reject(BluesException.BLUETOOTH_ENABLE_FAILED.name(), BluesException.BLUETOOTH_ENABLE_FAILED.message());
                    }
                }
                mEnabledPromise = null;
            }
        });
    }

    @Override
    public String getName() {
        return "RNBlues";
    }

    @ReactMethod
    public void isBluetoothAvailable(Promise promise) {
        promise.resolve(blues.isBluetoothAvailable());
    }

    @ReactMethod
    public void isBluetoothEnabled(Promise promise) {
        promise.resolve(blues.checkBluetoothAdapter());
    }

    @ReactMethod
    public void requestBluetoothEnabled(Promise promise) {
        blues.requestBluetoothEnabled(getCurrentActivity(), promise);
    }

    @ReactMethod
    public void deviceList(Promise promise) {
        blues.list(promise);
    }

    @ReactMethod
    public void startScan(Promise promise) {
        blues.startDiscovery(promise);
    }

    @ReactMethod
    public void getConnectedA2dpDevice(Promise promise) {
        blues.getConnectedA2dpDevice(promise);
    }

    @ReactMethod
    public void stopScan(Promise promise) {
        boolean success = blues.stopDiscovery();
        promise.resolve(success);
    }

    @ReactMethod
    public void connectA2dp(String id, Promise promise) {
        if (blues != null) {
            blues.connectA2dp(id, promise);
        } else {
            promise.reject(BluesException.BLUES_ERROR.name(), BluesException.BLUES_ERROR.message());
        }
    }

    @ReactMethod
    public void disconnectA2dp(Promise promise) {
        if (blues != null) {
            blues.disconnectA2dp(promise);
        } else {
            promise.reject(BluesException.BLUES_ERROR.name(), BluesException.BLUES_ERROR.message());
        }
    }

//    @ReactMethod
//    public void startBluetoothSco(Promise promise) {
//        mAudioManager.setBluetoothScoOn(true);
//        mAudioManager.startBluetoothSco();
//        promise.resolve(true);
//    }
//
//    @ReactMethod
//    public void stopBluetoothSco(Promise promise) {
//        mAudioManager.setBluetoothScoOn(false);
//        mAudioManager.stopBluetoothSco();
//        promise.resolve(true);
//    }

    @ReactMethod
    public void close() {
        blues.close();
    }

    synchronized public static void sendEvent(EventType event, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(event.name(), params);
    }

    @Override
    public void onHostResume() {
        blues.registerBluetoothReceivers();
    }

    @Override
    public void onHostPause() {
        blues.unregisterBluetoothReceivers();
    }

    @Override
    public void onHostDestroy() {
        blues.stopDiscovery();
        blues.close();
    }
}