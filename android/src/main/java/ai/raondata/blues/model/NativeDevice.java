package ai.raondata.blues.model;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;


/**
 * Provides wrapping of {@link BluetoothDevice} details and communication.
 * Primarily used for providing the {@link Mappable#map()} method.
 *
 * @author kendavidson
 */
@SuppressLint("MissingPermission")
public class NativeDevice implements Mappable {

    private BluetoothDevice mDevice;
    private Map<String,Object> mExtra;

    public NativeDevice(BluetoothDevice device) {
        this.mDevice = device;
        this.mExtra = new HashMap<>();
    }

    public BluetoothDevice getDevice() { return mDevice; }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public String getName() {
        return mDevice.getName();
    }

    public int getBondState() {
        return mDevice.getBondState();
    }

    public BluetoothClass getBluetoothClass() {
        return mDevice.getBluetoothClass();
    }

    public ParcelUuid[] getUuids() {
        return mDevice.getUuids();
    }

    public <T> T getExtra(String key) {
        return (T) mExtra.get(key);
    }

    public <T> T putExtra(String key, T value) {
        return (T) mExtra.put(key, value);
    }

    @Override
    public WritableMap map() {
        WritableMap mapped = Arguments.createMap();

        mapped.putString("name", mDevice.getName() != null ? mDevice.getName() : mDevice.getAddress());
        mapped.putString("address", mDevice.getAddress());
        mapped.putString("id", mDevice.getAddress());
        mapped.putBoolean("bonded", mDevice.getBondState() == BluetoothDevice.BOND_BONDED);

        if (mDevice.getBluetoothClass() != null) {
            WritableMap deviceClass = Arguments.createMap();
            deviceClass.putInt("deviceClass", mDevice.getBluetoothClass().getDeviceClass());
            deviceClass.putInt("majorClass", mDevice.getBluetoothClass().getMajorDeviceClass());
        }

        WritableMap extra = Arguments.createMap();

        for (Map.Entry<String, Object> e : mExtra.entrySet()) {
            if (e.getValue() instanceof Integer) {
                extra.putInt(e.getKey(), (int) e.getValue());
            } else {
                extra.putString(e.getKey(), (String) e.getValue());
            }
        }
        mapped.putMap("extra", extra);

        return mapped;
    }
}
