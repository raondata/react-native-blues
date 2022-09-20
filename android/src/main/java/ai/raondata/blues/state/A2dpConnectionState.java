package ai.raondata.blues.state;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;

public enum A2dpConnectionState {
    DISCONNECTED(BluetoothA2dp.STATE_DISCONNECTED),
    CONNECTED(BluetoothAdapter.STATE_CONNECTED);

    public final int code;
    private A2dpConnectionState(int code) {
        this.code = code;
    }
}
