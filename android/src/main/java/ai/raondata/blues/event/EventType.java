package ai.raondata.blues.event;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public enum EventType {

    BLUETOOTH_STATE_CHANGING("bluetoothStateChanging"),

    BLUETOOTH_STATE_CHANGED("bluetoothStateChanged"),

    BLUETOOTH_ENABLED("bluetoothEnabled"),

    BLUETOOTH_DISABLED("bluetoothDisabled"),

    CONNECTION_STATE_CHANGED("connectionStateChanged"),

    DEVICE_CONNECTED("deviceConnected"),

    DEVICE_DISCONNECTED("deviceDisconnected"),

    DEVICE_READ("deviceRead"),

    ERROR("error"),

    DEVICE_DISCOVERED("deviceDiscovered"),

    SCAN_STARTED("scanStarted"),

    SCAN_STOPPED("scanStopped"),

    SCAN_ERROR("scanError")

;

    public final String name;
    EventType(String name) {
        this.name = name;
    }

    public static WritableMap eventNames() {
        WritableMap events = Arguments.createMap();
        for(EventType event : EventType.values()) {
            events.putString(event.name(), event.name());
        }
        return events;
    }
}
