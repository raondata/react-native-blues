package ai.raondata.blues.exception;

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
    ALREADY_CONNECTING("Already attempting connection to device %s"),
    ALREADY_CONNECTED("Already connected to device %s"),
    NOT_CURRENTLY_CONNECTED("Not connected to %s"),
    BONDING_UNAVAILABLE_API("Bluetooth bonding is unavailable in this version of Android"),
    DISCOVERY_FAILED("Attempt to discover failed due to: %s"),
    WRITE_FAILED("Unable to write to device, due to: %s"),
    READ_FAILED("Unable to read from device, due to: %s"),
    ACCEPTING_CANCELLED("Accept was cancelled"),
    CONNECTION_FAILED("Connection to %s failed."),
    DISCONNECTION_FAILED("Disconnection to %s failed."),
    REMOVE_BOND_FAILED("Removing bond failed."),
    CONNECTION_LOST("Connection to %s was lost"),
    PAIRING_FAILED("Unable to complete pairing with %s"),
    INVALID_CONNECTOR_TYPE("Invalid connector type: %s"),
    INVALID_ACCEPTOR_TYPE("Invalid acceptor type: %s"),
    INVALID_CONNECTION_TYPE("Invalid connection type: %s"),
    ;

    private final String message;
    BluesException(String message) {
        this.message = message;
    }

    public String message(Object... args) {
        return String.format(message, args);
    }
}

