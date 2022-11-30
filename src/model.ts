export interface NativeDevice {
  id: string,
  name?: string,
  address: string,
  bonded: boolean,
  extra: any
}