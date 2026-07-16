const EXPIRE_MILLIS = 30 * 60 * 1000;
const cache = new Map();

export function shouldCount(key) {
  const now = Date.now();
  const lastTime = cache.get(key);
  if (lastTime != null && now - lastTime < EXPIRE_MILLIS) {
    return false;
  }
  cache.set(key, now);
  return true;
}
