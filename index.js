import { LogBox, NativeEventEmitter, NativeModules } from 'react-native';
const { RNBlues } = NativeModules;
LogBox.ignoreLogs(['new NativeEventEmitter']);

const eventEmitter = new NativeEventEmitter(RNBlues);
const eventMap = {};

export const removeBluesEvent = (eventName: String) => {
  if (Object.keys(eventMap).includes(eventName)) {
    eventMap[eventName].remove();
    delete eventMap[eventName];
    console.log(`[RNBlues.removeBluesEvent] event ${eventName} is removed.`);
  } else {
    throw new Error(`${eventName} 이벤트는 react-native-blues에 등록되지 않았습니다.`);
  }
};

export const getEventHandler = (eventName) => {
  return eventMap[eventName];
};

export const setEvent = (eventName: String, handler: Function) => {
  if (Object.keys(eventMap).includes(eventName)) {
    console.log(`[RNBlues.on] event ${eventName} already registered.`);
    removeBluesEvent(eventName);
  }
  eventMap[eventName] = eventEmitter.addListener(eventName, handler);
  // console.log('registered events:', Object.keys(eventMap));
  return eventMap[eventName];
};


export const removeAllEvents = () => {
  Object.keys(eventMap).forEach((eventName) => {
    removeBluesEvent(eventName);
  });
};

export const isBluetoothAvailable = async () => {
  return await RNBlues.isBluetoothAvailable();
}


export const isBluetoothEnabled = async () => {
  return await RNBlues.isBluetoothEnabled();
}

export const enableBluetooth = async (onBluetoothAlreadyEnabled) => {
  let enabled = true;
  try {
    enabled = await RNBlues.requestBluetoothEnabled();
    console.log('RNBlues.requestBluetoothEnabled(): ', enabled);
  } catch (e) {
    if (e.toString().includes('already enabled')) {
      onBluetoothAlreadyEnabled instanceof Function && onBluetoothAlreadyEnabled();
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

export const disableBluetooth = async () => {
  return await RNBlues.disableBluetooth();
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

export default RNBlues;
