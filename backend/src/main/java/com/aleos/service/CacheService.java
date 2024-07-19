package com.aleos.service;

import lombok.NonNull;

import java.util.concurrent.ConcurrentHashMap;

public class CacheService {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public record CacheEntry(Integer status, Object content) {
    }

    public void put(@NonNull String key, @NonNull CacheEntry cacheEntry) {
        cache.put(key, cacheEntry);
    }

    public CacheEntry get(String key) {
        return cache.get(key);
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public void remove(String cacheKey) {
        if (cacheKey.contains("currency")) {
            cache.entrySet().removeIf(entry -> entry.getKey().matches(".*(currency|currencies).*"));
        } else if (cacheKey.contains("exchangeRate")) {
            cache.entrySet().removeIf(entry -> entry.getKey().matches(".*(exchangeRate|exchangeRates).*"));
        } else {
            cache.remove(cacheKey);
        }
    }
}
