package ai.raondata.blues.exception;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public enum BluesException {
    BLUES_ERROR("Blues is not initialized"),
    BLUETOOTH_NOT_AVAILABLE("Bluetooth is not available for this device"),
    BLUETOOTH_NOT_ENABLED("Bluetooth mAdapter is not enabled"),
    BLUETOOTH_ENABLE_FAILED("Failed to enable Bluetooth mAdapter"),
    BLUETOOTH_IN_DISCOVERY("Bluetooth already in discovery mode"),
    BLUETOOTH_IN_ACCEPTING("Bluetooth already in accepting state"),
    BLUETOOTH_NOT_ACCEPTING("Bluetooth is not currently accepting"),
    BLUETOOTH_DEVICE_NOT_FOUND("Bluetooth device is not found"),
    ALREADY_ENABLED("Bluetooth is already enabled"),
    ALREADY_CONNECTING("Already attempting connection to device"),
    ALREADY_CONNECTED("Already connected to device"),
    NOT_CURRENTLY_CONNECTED("Not connected to device"),
    BONDING_UNAVAILABLE_API("Bluetooth bonding is unavailable in this version of Android"),
    DISCOVERY_FAILED("Attempt to discover failed:"),
    WRITE_FAILED("Unable to write to device"),
    READ_FAILED("Unable to read from device"),
    ACCEPTING_CANCELLED("Accept was cancelled"),
    CONNECTION_FAILED("Connection to device failed."),
    DISCONNECTION_FAILED("Disconnection to device failed."),
    REMOVE_BOND_FAILED("Removing bond failed."),
    NO_DEVICE_CONNECTION("There is no Blues device connection."),
    CONNECTION_LOST("Connection was lost"),
    PAIRING_FAILED("Unable to complete pairing"),
    INVALID_CONNECTOR_TYPE("Invalid connector type"),
    INVALID_ACCEPTOR_TYPE("Invalid acceptor type"),
    INVALID_CONNECTION_TYPE("Invalid connection type"),
    ;

    private final String message;
    BluesException(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
    public String message(String args) {
        return String.join(message, args);
    }
    
    public WritableMap map() {
        WritableMap map = Arguments.createMap();
        map.putString("name", this.name());
        map.putString("message", this.message());
        return map;
    }
}

