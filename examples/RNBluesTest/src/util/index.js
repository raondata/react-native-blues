export const tryCall = function (fn: Function, ...args) {
  return fn instanceof Function && fn.apply(this, args);
};

export const tryCallAsync = async function (fn: Function, ...args) {
  return fn instanceof Function && await fn.apply(this, args);
};