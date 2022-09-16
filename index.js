import { NativeEventEmitter, NativeModules } from 'react-native';

const { RNBlues } = NativeModules;

const eventEmitter = new NativeEventEmitter(RNBlues);
const eventMap = {};

RNBlues.removeListener = (eventName: String) => {
  console.log('[RNBlues.removeListener]');
  if (Object.keys(eventMap).includes(eventName)) {
    eventMap[eventName].remove();
    delete eventMap[eventName];
  } else {
    throw new Error(`${eventName} 이벤트는 react-native-blues에 등록되지 않았습니다.`);
  }
};

RNBlues.on = (eventName: String, handler: Function) => {
  console.log('[RNBlues.on]');
  if (Object.keys(eventMap).includes(eventName)) {
    console.log(`[RNBlues.on] event ${eventName} already registered.`);
    eventMap[eventName].remove();
  }
  eventMap[eventName] = eventEmitter.addListener(eventName, handler);
  return eventMap[eventName];
};


RNBlues.removeAllEvents = () => {
  Object.entries(eventMap).forEach(([eventName, e]) => {
    e.remove();
    delete eventMap[eventName];
  });
};

export default RNBlues;
