import { PermissionsAndroid, Platform } from "react-native";
import { check, PERMISSIONS, request, RESULTS } from 'react-native-permissions';
import * as Blues from '../bluetooth';

export const requestStoragePermission = async () => {
  // dhpark: 0. storage permission
  return await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE);
};

export async function enablePermissions() {
  // dhpark: 1. check bluetooth available
  const available = await Blues.isBluetoothAvailable();
  if (!available) {
    throw new Error('bluetooth not available');
  }
  
  // dhpark: 2. check bluetooth enabled
  const enabled = await Blues.isBluetoothEnabled();
  if (!enabled) {
    const enabled2 = await Blues.requestBluetoothEnabled();
    if (!enabled2) {
      throw new Error('bluetooth not enabled');
    }
  }

  // dhpark: 3. check runtime permissions
  let granted;
  if (Platform.OS === 'android') {
    granted = await _requestAndroidPermissions();
  } else {
    throw `unsupported platform os :(`;
  }
  if (!granted) {
    throw new Error(`권한획득에 실패했습니다 : ${PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION}`);
  }
  return true;
}

async function _requestAndroidPermissions() {
  this.debug && console.log("Android version :", Platform.Version);
  let grantedCoarseLocation;
  let grantedFineLocation = RESULTS.GRANTED;
  let grantedScan = RESULTS.GRANTED;
  let grantedConnect = RESULTS.GRANTED;

  // dhpark: legacy permission check
  grantedCoarseLocation = await check(PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION);
  if (grantedCoarseLocation === RESULTS.DENIED) {
    grantedCoarseLocation = await request(PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION);
  }
  
  if (Platform.Version >= 31) { // dhpark: latest permission policy
    // dhpark: 1. fine location
    grantedFineLocation = await check(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);
    if (grantedFineLocation === RESULTS.DENIED) {
      grantedFineLocation = await request(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);
    }
    // dhpark: 2. bluetooth scan
    grantedScan = await check(PERMISSIONS.ANDROID.BLUETOOTH_SCAN);
    if (grantedScan === RESULTS.DENIED) {
      grantedScan = await request(PERMISSIONS.ANDROID.BLUETOOTH_SCAN);
    }
    // dhpark: 3. bluetooth connect
    grantedConnect = await check(PERMISSIONS.ANDROID.BLUETOOTH_CONNECT);
    if (grantedConnect === RESULTS.DENIED) {
      grantedConnect = await request(PERMISSIONS.ANDROID.BLUETOOTH_CONNECT);
    }
  }

  return grantedCoarseLocation === RESULTS.GRANTED
      && grantedFineLocation === RESULTS.GRANTED
      && grantedScan === RESULTS.GRANTED
      && grantedConnect === RESULTS.GRANTED
  ;
}