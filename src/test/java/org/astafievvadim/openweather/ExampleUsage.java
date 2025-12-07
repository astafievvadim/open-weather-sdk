package org.astafievvadim.openweather;

import org.astafievvadim.openweather.weather.Mode;
import org.astafievvadim.openweather.weather.WeatherException;
import org.astafievvadim.openweather.weather.WeatherSDK;
import org.astafievvadim.openweather.weather.WeatherSDKManager;

public class ExampleUsage {

    public static void main(String[] args) {

        try {

            String key = "438b7481aa7d08a631115d758ae10956";

            WeatherSDK sdk = WeatherSDKManager.createOrGet(key, Mode.ON_DEMAND);

            String result = sdk.getWeatherByCity("some_nonexistent_name");

            System.out.println(result);

        } catch (WeatherException e) {

            e.printStackTrace();

        }
    }
}

