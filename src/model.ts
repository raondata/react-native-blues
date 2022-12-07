import { NativeModules } from 'react-native';
import { BluesConnectedState } from './types';
const { RNBlues } = NativeModules;

export interface INativeDevice {
  id: string,
  name?: string,
  address: string,
  bonded: boolean,
  extra: any,
  isConnected: () => Promise<BluesConnectedState>;
}

export class NativeDevice implements INativeDevice {
  id: string;
  name?: string | undefined;
  address: string;
  bonded: boolean;
  extra: any;

  constructor({
    id,
    name,
    address,
    bonded,
    extra
  }) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.bonded = bonded;
    this.extra = extra
  }

  async isConnected(): Promise<BluesConnectedState> {
    return await RNBlues.getConnectionState(this.address);
  };
}