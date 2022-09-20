package ai.raondata.blues.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public enum EventType {

    BLUETOOTH_STATE_CHANGED("bluetoothStateChanged"),

    BLUETOOTH_ENABLED("bluetoothEnabled"),

    BLUETOOTH_DISABLED("bluetoothDisabled"),

    DEVICE_CONNECTED("deviceConnected"),

    DEVICE_DISCONNECTED("deviceDisconnected"),

    DEVICE_READ("deviceRead"),

    ERROR("error"),

    DEVICE_DISCOVERED("deviceDiscovered"),

    SCAN_STARTED("scanStarted"),

    SCAN_STOPPED("scanStopped")

;

    public final String code;
    EventType(String code) {
        this.code = code;
    }

    public static WritableMap eventNames() {
        WritableMap events = Arguments.createMap();
        for(EventType event : EventType.values()) {
            events.putString(event.name(), event.name());
        }
        return events;
    }
}
