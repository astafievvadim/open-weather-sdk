package org.astafievvadim.openweather.weather;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WeatherSDKManager {
    private static final ConcurrentHashMap<String, WeatherSDK> instances = new ConcurrentHashMap<>();

    public static WeatherSDK createOrGet(String apiKey, Mode mode) throws WeatherException {
        Objects.requireNonNull(apiKey, "apiKey is required");

        // computeIfAbsent: ensures that there is at time only one single sdk instance per key

        return instances.computeIfAbsent(apiKey, k -> {
            try {
                return new WeatherSDK(k, mode);
            } catch (WeatherException e) {

                return null;
            }
        });
    }

    public static WeatherSDK get(String apiKey) {
        return instances.get(apiKey);
    }

    public static void delete(String apiKey) {
        WeatherSDK sdk = instances.remove(apiKey);
        if (sdk != null) {
            sdk.shutdown();
        }
    }
}
