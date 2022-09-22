package ai.raondata.blues.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.raondata.blues.model.NativeDevice;

public class DiscoveryReceiver extends BroadcastReceiver {

    private final Callback mCallback;
    private final Map<String, NativeDevice> unpairedDevices;

    public DiscoveryReceiver(Callback callback) {
        this.mCallback = callback;
        this.unpairedDevices = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Log.d("DiscoveryReceiver", "onDeviceDiscovered: BluetoothDevice.ACTION_FOUND");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (!unpairedDevices.containsKey(device.getAddress())) {
                NativeDevice nativeDevice = new NativeDevice(device);
                nativeDevice.putExtra("rssi", (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                unpairedDevices.put(device.getAddress(), nativeDevice);
                mCallback.onDeviceDiscovered(nativeDevice);
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.d("DiscoveryReceiver", "onDiscoveryFinished: BluetoothAdapter.ACTION_DISCOVERY_FINISHED");
            mCallback.onDiscoveryFinished(unpairedDevices.values());
            context.unregisterReceiver(this);
        }
    }

    public static IntentFilter intentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        return intentFilter;
    }

    public interface Callback {

        /**
         * Alerts when {@link BluetoothDevice#ACTION_FOUND} is fired.  During discovery
         * devices will be found multiple times; differing values (such as RSSI) will be updated.
         *
         * @param device
         */
        void onDeviceDiscovered(NativeDevice device);

        /**
         * When discovery is completed a {@link BluetoothAdapter#ACTION_DISCOVERY_FINISHED}
         * a {@link List} of {@link NativeDevice}(s) is returned.
         *
         * @param devices
         */
        void onDiscoveryFinished(Collection<NativeDevice> devices);

        /**
         * If an {@link Exception} of any kind is thrown during the discovery process.
         *
         * @param e
         */
        void onDiscoveryFailed(Throwable e);

    }

}
