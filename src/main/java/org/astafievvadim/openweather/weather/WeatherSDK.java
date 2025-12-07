package org.astafievvadim.openweather.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.astafievvadim.openweather.cache.WeatherCache;
import org.astafievvadim.openweather.model.WeatherResponse;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private static final String GEOCODING_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String CURRENT_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final String apiKey;
    private final Mode mode;
    private final HttpClient http;
    private final ObjectMapper om = new ObjectMapper();

    // cache: up to 10 cities, freshness 10 minutes (600 seconds)

    private final WeatherCache cache = new WeatherCache(10, 600);

    // Polling
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "WeatherSDK-Poller");
        t.setDaemon(true);
        return t;
    });

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    // poll interval in seconds (when in POLLING mode). We pick 5 minutes.
    private static final long POLL_INTERVAL_SECONDS = 300L;

    // private constructor; use manager or factory methods
    WeatherSDK(String apiKey, Mode mode) throws WeatherException {
        if (apiKey == null || apiKey.isBlank()) throw new WeatherException("API key cannot be null or empty");
        this.apiKey = apiKey;
        this.mode = mode == null ? Mode.ON_DEMAND : mode;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        if (this.mode == Mode.POLLING) {
            // start polling task
            poller.scheduleAtFixedRate(this::pollAll, 10, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    public String getWeatherByCity(String cityName) throws WeatherException {
        if (cityName == null || cityName.isBlank()) throw new WeatherException("City name is required");

        WeatherResponse cached = cache.getIfFresh(cityName);
        if (cached != null) {
            System.out.println("USED CACHE");
            return cached.toJson();
        }

        try {
            WeatherResponse resp = fetchWeatherForCity(cityName);
            cache.put(cityName, resp);
            System.out.println("DID NOT USE CACHE");
            return resp.toJson();
        } catch (WeatherException e) {
            throw e;
        } catch (Exception ex) {
            throw new WeatherException("Failed to get weather for city: " + cityName, ex);
        }
    }

    private WeatherResponse fetchWeatherForCity(String cityName) throws WeatherException {
        try {
            // 1) Geocoding: limit=1
            String q = URLEncoder.encode(cityName, StandardCharsets.UTF_8);
            String geoUrl = String.format("%s?q=%s&limit=1&appid=%s", GEOCODING_URL, q, apiKey.trim());

            HttpRequest reqGeo = HttpRequest.newBuilder()
                    .uri(URI.create(geoUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> geoResp = http.send(reqGeo, HttpResponse.BodyHandlers.ofString());
            if (geoResp.statusCode() != 200) {
                throw new WeatherException("Geocoding API returned status " + geoResp.statusCode() + ": " + geoResp.body());
            }
            JsonNode geoNode = om.readTree(geoResp.body());
            if (!geoNode.isArray() || geoNode.size() == 0) {
                throw new WeatherException("City not found by geocoding: " + cityName);
            }
            JsonNode first = geoNode.get(0);
            double lat = first.get("lat").asDouble();
            double lon = first.get("lon").asDouble();

            // 2) Current weather by coordinates
            String currentUrl = String.format("%s?lat=%s&lon=%s&appid=%s", CURRENT_WEATHER_URL, lat, lon, apiKey);
            HttpRequest reqCurrent = HttpRequest.newBuilder()
                    .uri(URI.create(currentUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> curResp = http.send(reqCurrent, HttpResponse.BodyHandlers.ofString());
            if (curResp.statusCode() != 200) {
                throw new WeatherException("Current weather API returned status " + curResp.statusCode() + ": " + curResp.body());
            }
            JsonNode root = om.readTree(curResp.body());

            // map to WeatherResponse
            String weatherMain = "";
            String weatherDesc = "";
            JsonNode weatherArr = root.path("weather");
            if (weatherArr.isArray() && weatherArr.size() > 0) {
                JsonNode w0 = weatherArr.get(0);
                weatherMain = w0.path("main").asText("");
                weatherDesc = w0.path("description").asText("");
            }
            double temp = root.path("main").path("temp").asDouble(Double.NaN);
            double feelsLike = root.path("main").path("feels_like").asDouble(Double.NaN);

            Integer visibility = root.has("visibility") && !root.get("visibility").isNull() ? root.get("visibility").asInt() : null;
            Double windSpeed = root.has("wind") && root.get("wind").has("speed") ? root.get("wind").get("speed").asDouble() : null;

            long datetime = root.path("dt").asLong(0);
            long sunrise = root.path("sys").path("sunrise").asLong(0);
            long sunset = root.path("sys").path("sunset").asLong(0);
            int timezone = root.path("timezone").asInt(0);
            String name = root.path("name").asText(cityName);

            return new WeatherResponse(weatherMain, weatherDesc, temp, feelsLike,
                    visibility, windSpeed, datetime, sunrise, sunset, timezone, name);

        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new WeatherException("Network error while fetching weather", ex);
        }
    }

    private void pollAll() {
        try {
            for (String city : cache.keys()) {
                final String cityName = city;
                executor.submit(() -> {
                    try {
                        WeatherResponse r = fetchWeatherForCity(cityName);
                        cache.put(cityName, r);
                    } catch (Exception ex) {
                        System.err.println("Polling update failed for " + cityName + ": " + ex.getMessage());
                    }
                });
            }
        } catch (Exception ex) {
            System.err.println("Poller error: " + ex.getMessage());
        }
    }

    public void addCityToStore(String cityName) throws WeatherException {
        WeatherResponse r = fetchWeatherForCity(cityName);
        cache.put(cityName, r);
    }

    public void removeCityFromStore(String cityName) {
        cache.remove(cityName);
    }

    public void clearCache() {
        cache.clear();
    }


    public void shutdown() {
        try {
            poller.shutdownNow();
        } catch (Exception ignored) {}
        try {
            executor.shutdownNow();
        } catch (Exception ignored) {}
    }

    public Map<String, WeatherResponse> snapshotStored() {
        return cache.snapshotAll();
    }

    public Mode getMode() {
        return mode;
    }

    public String getApiKey() {
        return apiKey;
    }
}
