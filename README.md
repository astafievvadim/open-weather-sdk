# OpenWeather SDK

## English

### OpenWeather SDK

A Java SDK for easy access to the OpenWeather API. Provides weather information in a standard JSON format and supports caching and polling modes.

### Features

- Query current weather by city name  
- Cache up to 10 cities, freshness 10 minutes  
- On-demand and polling modes  
- Singleton-like management per API key  
- Returns standardized JSON with weather, temperature, visibility, wind, sunrise/sunset, timezone, and city name  
- Exception handling with descriptive messages  

### Installation

Add the dependency to your Maven project:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/astafievvadim/open-weather-sdk</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.astafievvadim</groupId>
        <artifactId>open-weather-sdk</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

### Usage example

```java
import org.astafievvadim.openweather.weather.WeatherSDK;
import org.astafievvadim.openweather.weather.WeatherSDKManager;
import org.astafievvadim.openweather.weather.Mode;
import org.astafievvadim.openweather.weather.WeatherException;

public class Main {
    public static void main(String[] args) {
        try {
            WeatherSDK sdk = WeatherSDKManager.createOrGet("YOUR_API_KEY", Mode.ON_DEMAND);
            String json = sdk.getWeatherByCity("London");
            System.out.println(json);
            sdk.shutdown();
        } catch (WeatherException e) {
            e.printStackTrace();
        }
    }
}
```

### JSON Response Example
```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feels_like": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "Zocca"
}
```

### API Notes

WeatherSDKManager.createOrGet(apiKey, mode)  -  create or get SDK instance

WeatherSDK.getWeatherByCity(cityName)  -  get cached or fresh weather

WeatherSDK.addCityToStore(cityName)  -  add city for polling mode

WeatherSDK.removeCityFromStore(cityName)  -  remove city from cache

WeatherSDK.clearCache()  -  clear all cached cities

WeatherSDK.shutdown()  -  stop background threads

## Русский
### OpenWeather SDK

Java SDK для удобного доступа к API OpenWeather. Возвращает данные о погоде в формате JSON и поддерживает кэширование и режим опроса.

### Возможности

Получение текущей погоды по названию города

Кэширование до 10 городов, актуальность 10 минут

Режимы работы: по запросу (on-demand) и с опросом (polling)

Управление объектами SDK по ключу API (один объект на ключ)

Возвращает стандартизированный JSON с погодой, температурой, видимостью, ветром, восходом/закатом, часовым поясом и названием города

Обработка ошибок с описанием причины

### Установка

Добавьте зависимость в ваш Maven проект:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/astafievvadim/open-weather-sdk</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.astafievvadim</groupId>
        <artifactId>open-weather-sdk</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

### Пример использования
```java
import org.astafievvadim.openweather.weather.WeatherSDK;
import org.astafievvadim.openweather.weather.WeatherSDKManager;
import org.astafievvadim.openweather.weather.Mode;
import org.astafievvadim.openweather.weather.WeatherException;

public class Main {
    public static void main(String[] args) {
        try {
            WeatherSDK sdk = WeatherSDKManager.createOrGet("ВАШ_API_KEY", Mode.ON_DEMAND);
            String json = sdk.getWeatherByCity("Москва");
            System.out.println(json);
            sdk.shutdown();
        } catch (WeatherException e) {
            e.printStackTrace();
        }
    }
}
```

Пример JSON ответа
```json
{
  "weather": {
    "main": "Clouds",
    "description": "scattered clouds"
  },
  "temperature": {
    "temp": 269.6,
    "feels_like": 267.57
  },
  "visibility": 10000,
  "wind": {
    "speed": 1.38
  },
  "datetime": 1675744800,
  "sys": {
    "sunrise": 1675751262,
    "sunset": 1675787560
  },
  "timezone": 3600,
  "name": "Zocca"
}
```

### Особенности API

WeatherSDKManager.createOrGet(apiKey, mode)  -  создать или получить экземпляр SDK

WeatherSDK.getWeatherByCity(cityName)  -  получить актуальную или кэшированную погоду

WeatherSDK.addCityToStore(cityName)  -  добавить город для режима опроса

WeatherSDK.removeCityFromStore(cityName)  -  удалить город из кэша

WeatherSDK.clearCache()  -  очистить все сохранённые города

WeatherSDK.shutdown()  -  остановить фоновые потоки
