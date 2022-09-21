import { Platform } from 'react-native';
import { check, PERMISSIONS, request, RESULTS } from 'react-native-permissions';

const requiredPermissions = (() => {
  const permissions = [
    PERMISSIONS.ANDROID.ACCESS_COARSE_LOCATION,
    PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION,
    PERMISSIONS.ANDROID.ACCESS_BACKGROUND_LOCATION,
  ];
  if (Platform.Version >= 31) {
    permissions.concat(
      PERMISSIONS.ANDROID.BLUETOOTH_SCAN,
      PERMISSIONS.ANDROID.BLUETOOTH_CONNECT
    );
  }
  return permissions;
})();

async function _request(permission) {
  console.log('check permission:', permission);
  if (await check(permission) !== RESULTS.GRANTED) {
    console.log('request permission:', permission);
    return await request(permission);
  } else {
    return RESULTS.GRANTED;
  }
}

export async function requestPermissions() {
  for (const p of requiredPermissions) {
    let res;
    try {
      res = await _request(p);
    } catch (e) {
      console.error(`권한획득에 실패했습니다 : result=${res}, permission=${p}, error=${e}`);
    }
    if (res !== RESULTS.GRANTED) {
      throw new Error(`권한획득에 실패했습니다 : result=${res}, permission=${p}`);
    }
  }
  return true;
}
