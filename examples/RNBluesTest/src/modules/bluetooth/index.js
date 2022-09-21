import RNBlues from "react-native-blues";
import { tryCall } from "../../util";

export const setEvent = (eventName, listener) => {
  console.log('[bluetooth] set event to RNBlues:', eventName);
  RNBlues.on(eventName, listener);
};

export const removeBluesEvent = (eventName) => {
  RNBlues.removeListener(eventName);
};

export const removeAllEvents = () => {
  console.log('[bluetooth] removing all events of RNBlues..');
  RNBlues.removeAllEvents();
};

export const isBluetoothAvailable = async () => {
  return await RNBlues.isBluetoothAvailable();
};

export const isBluetoothEnabled = async () => {
  return await RNBlues.isBluetoothEnabled();
}

export const enableBluetooth = async (onBluetoothAlreadyEnabled) => {
  let enabled;
  try {
    enabled = await RNBlues.requestBluetoothEnabled();
  } catch (e) {
    if (e.toString().includes('already enabled')) {
      tryCall(onBluetoothAlreadyEnabled);
    } else {
      throw e;
    }
  }
  if (!enabled) {
    throw new Error('failed to enable bluetooth');
  } else {
    return true;
  }
}

export const getPairedDeviceList = async () => {
  return await RNBlues.deviceList();
};

export const getConnectedDevice = async () => {
  return await RNBlues.getConnectedA2dpDevice();
}

export const isConnected = async () => {
  const device = await RNBlues.getConnectedA2dpDevice();
  return device !== null && device !== undefined;
};

export const startScan = async () => {
  return await RNBlues.startScan();
};

export const stopScan = async () => {
  return await RNBlues.stopScan();
};

export const connect = async (deviceId) => {
  return await RNBlues.connectA2dp(deviceId);
};

export const disconnect = async () => {
  await RNBlues.disconnectA2dp();
};

export const close = () => {
  RNBlues.close();
}
