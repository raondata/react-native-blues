import { Platform } from "react-native";
import RNBlues from "react-native-blues";

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

export const requestPermissions = () => {
  if (Platform.OS === 'android'){
    if (Platform.Version >= 23) {
      PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
        if (result) {
          console.log("Permission is OK");
        } else {
          PermissionsAndroid.requestPermission(PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION).then((result) => {
            if (result) {
              console.log("User accept");
            } else {
              console.log("User refuse");
            }
          });
        }
      });
    } else {
      console.log("android version is under 23.");
    }
  }
};