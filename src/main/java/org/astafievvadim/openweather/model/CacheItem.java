package org.astafievvadim.openweather.model;

public class CacheItem {
    public final WeatherResponse response;
    public final long timestampMillis;

    public CacheItem(WeatherResponse response, long timestampMillis) {
        this.response = response;
        this.timestampMillis = timestampMillis;
    }
}
