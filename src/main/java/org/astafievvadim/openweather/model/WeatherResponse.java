package org.astafievvadim.openweather.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WeatherResponse {
    public final String weatherMain;
    public final String weatherDescription;
    public final double temp;
    public final double feelsLike;
    public final Integer visibility;
    public final Double windSpeed;
    public final long datetime;
    public final long sunrise;
    public final long sunset;
    public final int timezoneOffset;
    public final String name;

    public WeatherResponse(String weatherMain, String weatherDescription,
                           double temp, double feelsLike,
                           Integer visibility, Double windSpeed,
                           long datetime, long sunrise, long sunset,
                           int timezoneOffset, String name) {
        this.weatherMain = weatherMain;
        this.weatherDescription = weatherDescription;
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.visibility = visibility;
        this.windSpeed = windSpeed;
        this.datetime = datetime;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.timezoneOffset = timezoneOffset;
        this.name = name;
    }

    public String toJson() {
        try {
            ObjectMapper om = new ObjectMapper();
            ObjectNode root = om.createObjectNode();

            ObjectNode weather = om.createObjectNode();
            weather.put("main", weatherMain);
            weather.put("description", weatherDescription);
            root.set("weather", weather);

            ObjectNode temperature = om.createObjectNode();
            temperature.put("temp", temp);
            temperature.put("feels_like", feelsLike);
            root.set("temperature", temperature);

            if (visibility != null) root.put("visibility", visibility);
            ObjectNode wind = om.createObjectNode();
            if (windSpeed != null) wind.put("speed", windSpeed);
            root.set("wind", wind);

            root.put("datetime", datetime);

            ObjectNode sys = om.createObjectNode();
            sys.put("sunrise", sunrise);
            sys.put("sunset", sunset);
            root.set("sys", sys);

            root.put("timezone", timezoneOffset);
            root.put("name", name);

            return om.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception ex) {

            return "{}";
        }
    }

    @Override
    public String toString() {
        return toJson();
    }
}