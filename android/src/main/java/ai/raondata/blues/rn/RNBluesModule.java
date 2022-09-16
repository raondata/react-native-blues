package ai.raondata.blues.rn;

import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import ai.raondata.blues.core.Blues;

public class RNBluesModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    public Blues blues = null;
    private AudioManager mAudioManager = null;

    public RNBluesModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        mAudioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
        blues = new Blues(this.reactContext);
    }

    @Override
    public String getName() {
        return "RNBlues";
    }

    @ReactMethod
    public void deviceList(Promise promise) {
        blues.list(promise);
    }

    @ReactMethod
    public void startScan(Promise promise) {
        boolean success = blues.startDiscovery();
        promise.resolve(success);
    }

    @ReactMethod
    public void getConnectedA2dpDevice(Promise promise) {
        promise.resolve(blues.getConnectedA2dpDevice());
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
            promise.reject("BLUES_ERROR", "Blues initiation failed.");
        }
    }

    @ReactMethod
    public void disconnectA2dp(Promise promise) {
        if (blues != null) {
            blues.disconnectA2dp(promise);
        } else {
            promise.reject("BLUES_ERROR", "Blues initiation failed.");
        }
    }

    @ReactMethod
    public void startBluetoothSco(Promise promise) {
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.startBluetoothSco();
        promise.resolve(true);
    }

    @ReactMethod
    public void stopBluetoothSco(Promise promise) {
        mAudioManager.setBluetoothScoOn(false);
        mAudioManager.stopBluetoothSco();
        promise.resolve(true);
    }

    public static void sendEvent(String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    public static void sendEvent(String eventName, String params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
}