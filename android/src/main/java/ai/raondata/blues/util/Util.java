package ai.raondata.blues.util;

import android.bluetooth.BluetoothDevice;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class Util {
    public static WritableMap deviceToWritableMap(BluetoothDevice device) {
        WritableMap params = Arguments.createMap();

        if (device != null) {
            params.putString("name", device.getName());
            params.putString("id", device.getAddress());

            if (device.getBluetoothClass() != null) {
                params.putInt("class", device.getBluetoothClass().getDeviceClass());
            }
        }

        return params;
    }


    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static DateFormat dateFormat() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
    }

    public static String formatDate(Date date) {
        return dateFormat().format(date);
    }

    public static Date parseDate(String date) throws ParseException {
        return dateFormat().parse(date);
    }

}