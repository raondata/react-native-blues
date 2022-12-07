import { LogBox, NativeEventEmitter, NativeModules } from 'react-native';
import { NativeDevice } from './model';
import { hasValue, isObject, tryCall } from './util';
const { RNBlues } = NativeModules;
LogBox.ignoreLogs(['new NativeEventEmitter']);

const eventEmitter = new NativeEventEmitter(RNBlues);
const eventMap = {};
var debugMode = false;

const log = {
  d (...s) {
    debugMode && console.log('[BLUES]', ...s);
  },
  w (...s) {
    debugMode && console.warn('[BLUES]', ...s);
  },
  e (...s) {
    console.error('[BLUES]', ...s);
  },
};

export const setDebugMode = (d: boolean) => {
  debugMode = d;
};

export const getRegisteredEventNames = () => {
  return Object.keys(eventMap);
};

export const removeBluesEvent = (eventName: string) => {
  if (getRegisteredEventNames().includes(eventName)) {
    eventMap[eventName].remove();
    delete eventMap[eventName];
    log.d(`removeBluesEvent: event ${eventName} is removed.`);
  } else {
    throw new Error(`${eventName} 이벤트는 react-native-blues에 등록되지 않았습니다.`);
  }
};

export const getEventHandler = (eventName) => {
  return isObject(eventMap) && eventMap[eventName];
};

export const setEvent = (eventName: string, handler: (event: any) => void) => {
  if (getRegisteredEventNames().includes(eventName)) {
    log.d(`setEvent: event ${eventName} already registered.`);
    removeBluesEvent(eventName);
  }
  eventMap[eventName] = eventEmitter.addListener(eventName, handler);
  return eventMap[eventName];
};

export const setEvents = (events) => {
  Object.entries(events).forEach(([k, v]) => setEvent(k, v));
}

export const removeAllEvents = () => {
  Object.keys(eventMap).forEach((eventName) => {
    removeBluesEvent(eventName);
  });
};

export const isBluetoothAvailable = async (): Promise<boolean> => {
  return await RNBlues.isBluetoothAvailable();
};

export const isBluetoothEnabled = async (): Promise<boolean> => {
  return await RNBlues.isBluetoothEnabled();
};

export const enableBluetooth = async (onAlreadyEnabled?: Function): Promise<boolean> => {
  let enabled = true;
  try {
    enabled = await RNBlues.requestBluetoothEnabled();
    log.d('requestBluetoothEnabled(): ', enabled);
  } catch (e) {
    if (e.message.includes('already enabled')) {
      tryCall(onAlreadyEnabled);
    } else {
      throw e;
    }
  }
  if (!enabled) {
    throw new Error('failed to enable bluetooth');
  } else {
    return true;
  }
};

export const disableBluetooth = async (): Promise<boolean> => {
  return await RNBlues.disableBluetooth();
};

export const getPairedDeviceList = async (): Promise<NativeDevice[]> => {
  return await RNBlues.deviceList();
};

export const getConnectedDevice = async (): Promise<NativeDevice> => {
  const connectedDevice = await RNBlues.getConnectedA2dpDevice();
  log.d('getConnectedDevice:', connectedDevice);
  return connectedDevice;
};

export const isConnected = async () => {
  const device = await RNBlues.getConnectedA2dpDevice();
  return hasValue(device);
};

export const startScan = async () => {
  await stopScan();
  return RNBlues.startScan();
};

export const stopScan = async () => {
  return await RNBlues.stopScan();
};

export const connect = async (deviceId) => {
  return await RNBlues.connectA2dp(deviceId);
};

export const disconnect = async (removeBond: boolean) => {
  return await RNBlues.disconnectA2dp(removeBond);
};

export const close = () => {
  RNBlues.close();
};

export const emitBluesEvent = (eventName: string) => {
  eventEmitter.emit(eventName);
};

export default RNBlues;
