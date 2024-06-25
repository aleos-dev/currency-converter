package com.aleos.services;

import java.util.concurrent.ConcurrentHashMap;

public class CacheService {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public record CacheEntry(Integer status, Object content) {
    }

    public void put(String key, CacheEntry cacheEntry) {
        cache.put(key, cacheEntry);
    }

    public CacheEntry get(String key) {
        return cache.get(key);
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }
}
