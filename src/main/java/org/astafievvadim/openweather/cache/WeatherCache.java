package org.astafievvadim.openweather.cache;

import org.astafievvadim.openweather.model.CacheItem;
import org.astafievvadim.openweather.model.WeatherResponse;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeatherCache {
    private final int maxEntries;
    private final long freshnessMillis;

    private final LinkedHashMap<String, CacheItem> map;

    public WeatherCache(int maxEntries, long freshnessSeconds) {

        this.maxEntries = maxEntries;
        this.freshnessMillis = freshnessSeconds * 1000L;

        this.map = new LinkedHashMap<>(10, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheItem> eldest) {
                return size() > WeatherCache.this.maxEntries;
            }
        };
    }

    public synchronized void put(String cityKey, WeatherResponse resp) {
        map.put(cityKey.toLowerCase(),
                new CacheItem(resp, Instant.now().toEpochMilli()));
    }

    public synchronized WeatherResponse getIfFresh(String cityKey) {
        CacheItem item = map.get(cityKey.toLowerCase());
        if (item == null) return null;

        if (Instant.now().toEpochMilli() - item.timestampMillis <= freshnessMillis) {
            return item.response;
        } else {
            map.remove(cityKey.toLowerCase());
            return null;
        }
    }

    public synchronized Map<String, WeatherResponse> snapshotAll() {
        LinkedHashMap<String, WeatherResponse> snap = new LinkedHashMap<>();
        for (Map.Entry<String, CacheItem> en : map.entrySet()) {
            snap.put(en.getKey(), en.getValue().response);
        }
        return snap;
    }

    public synchronized void remove(String cityKey) {
        map.remove(cityKey.toLowerCase());
    }

    public synchronized int size() {
        return map.size();
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized java.util.Set<String> keys() {
        return new java.util.HashSet<>(map.keySet());
    }
}
