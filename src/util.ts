import { bluesEventHandler } from "./types";

export const hasValue = (o) => {
  return o !== null && o !== undefined;
};

export const isEmpty = (o) => {
  return !hasValue(o);
};

export const tryCall = function (fn?: Function, ...args) {
  return fn instanceof Function && fn.apply(this, args);
};

export const isObject = (o) => {
  return typeof o === "object";
}

export const entries = (o: {[s: string]: bluesEventHandler;}): Array<([string, bluesEventHandler])> => {
  return Object.entries(o);
}