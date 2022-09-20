package ai.raondata.blues.receiver;

import android.bluetooth.BluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class A2dpConnectionReceiver extends BroadcastReceiver {
    private final Callback mCallback;

    public A2dpConnectionReceiver(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            int currentState = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
            mCallback.onDeviceConnectionChanged();
            if (currentState == BluetoothA2dp.STATE_CONNECTED) {
                mCallback.onDeviceConnected();
            } else if (currentState == BluetoothA2dp.STATE_DISCONNECTED) {
                mCallback.onDeviceDisconnected();
            }
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        return intentFilter;
    }


    public interface Callback {
        void onDeviceConnectionChanged();

        void onDeviceConnected();

        void onDeviceDisconnected();
    }
}
