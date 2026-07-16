package com.example.reading.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阅读量去重缓存，避免同一会话短时间内重复统计
 */
@Component
public class ViewCountCache {

    private static final long EXPIRE_MILLIS = 30 * 60 * 1000L;
    private final Map<String, Long> cache = new ConcurrentHashMap<>();

    public boolean shouldCount(String key) {
        long now = System.currentTimeMillis();
        Long lastTime = cache.get(key);
        if (lastTime != null && now - lastTime < EXPIRE_MILLIS) {
            return false;
        }
        cache.put(key, now);
        return true;
    }
}
