export type bluesEventHandler = (event: any) => void;
export type bluesEventKeyValue = [string, bluesEventHandler];

export enum BluesConnectedState {
  STATE_DISCONNECTED,
  STATE_CONNECTING,
  STATE_CONNECTED,
  STATE_DISCONNECTING
}