import { NativeEventEmitter, NativeModules } from 'react-native';

const { RNBlues } = NativeModules;

const eventEmitter = new NativeEventEmitter(RNBlues);
const eventMap = {};

RNBlues.removeListener = (eventName: String) => {
  if (Object.keys(eventMap).includes(eventName)) {
    eventMap[eventName].remove();
    delete eventMap[eventName];
    console.log(`[RNBlues.removeListener] event ${eventName} is removed.`);
  } else {
    throw new Error(`${eventName} 이벤트는 react-native-blues에 등록되지 않았습니다.`);
  }
};

RNBlues.on = (eventName: String, handler: Function) => {
  if (Object.keys(eventMap).includes(eventName)) {
    console.log(`[RNBlues.on] event ${eventName} already registered.`);
    RNBlues.removeListener(eventName);
  }
  eventMap[eventName] = eventEmitter.addListener(eventName, handler);
  return eventMap[eventName];
};


RNBlues.removeAllEvents = () => {
  Object.keys(eventMap).forEach((eventName) => {
    RNBlues.removeListener(eventName);
  });
};

export default RNBlues;
