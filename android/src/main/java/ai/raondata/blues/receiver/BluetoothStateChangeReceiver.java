package ai.raondata.blues.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import ai.raondata.blues.state.BluetoothState;

public class BluetoothStateChangeReceiver extends BroadcastReceiver {

    private final Callback mCallback;

    public BluetoothStateChangeReceiver(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d("BlueStateChangeReceiver", "onStateChange, onBluetoothDisabled: BluetoothAdapter.STATE_OFF");
                    mCallback.onStateChange(BluetoothState.DISABLED, BluetoothState.ENABLED);
                    mCallback.onBluetoothDisabled();
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d("BlueStateChangeReceiver", "onStateChange, onBluetoothEnabled: BluetoothAdapter.STATE_ON");
                    mCallback.onStateChange(BluetoothState.ENABLED, BluetoothState.DISABLED);
                    mCallback.onBluetoothEnabled();
                    break;
            }
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        return intent;
    }

    public interface Callback {

        void onStateChange(BluetoothState newState, BluetoothState oldState);

        void onBluetoothEnabled();

        void onBluetoothDisabled();

    }
}

