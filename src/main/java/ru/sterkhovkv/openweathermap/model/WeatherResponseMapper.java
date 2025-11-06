package ru.sterkhovkv.openweathermap.model;

import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;
import ru.sterkhovkv.openweathermap.model.v3.WeatherDataV3;

/**
 * Mapper for converting WeatherData (V2 or V3) to WeatherResponse.
 */
public class WeatherResponseMapper {

    /**
     * Converts weather data (V2 or V3) and city name to WeatherResponse.
     *
     * @param weatherData weather data from API (WeatherDataV2 or WeatherDataV3)
     * @param apiVersion  API version used
     * @param cityName    city name
     * @return WeatherResponse for SDK users
     */
    public static WeatherResponse toResponse(Object weatherData, ApiVersion apiVersion, String cityName) {
        if (weatherData == null) {
            throw new IllegalArgumentException("Weather data cannot be null");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("API version cannot be null");
        }

        return switch (apiVersion) {
            case V3_0 -> toResponseV3((WeatherDataV3) weatherData, cityName);
            case V2_5 -> toResponseV2((WeatherDataV2) weatherData, cityName);
        };
    }

    /**
     * Converts WeatherDataV3 to WeatherResponse.
     */
    private static WeatherResponse toResponseV3(WeatherDataV3 weatherData, String cityName) {
        if (weatherData.getCurrent() == null) {
            throw new IllegalArgumentException("WeatherDataV3 current weather cannot be null");
        }

        WeatherDataV3.CurrentWeather current = weatherData.getCurrent();

        // Extract weather condition (first element from weather array)
        WeatherResponse.WeatherInfo weatherInfo = null;
        if (current.getWeather() != null && !current.getWeather().isEmpty()) {
            WeatherDataV3.WeatherCondition condition = current.getWeather().getFirst();
            weatherInfo = WeatherResponse.WeatherInfo.builder()
                .main(condition.getMain())
                .description(condition.getDescription())
                .build();
        }

        // Build temperature info
        WeatherResponse.TemperatureInfo temperatureInfo = WeatherResponse.TemperatureInfo.builder()
            .temp(current.getTemp())
            .feelsLike(current.getFeelsLike())
            .build();

        // Build wind info
        WeatherResponse.WindInfo windInfo = WeatherResponse.WindInfo.builder()
            .speed(current.getWindSpeed())
            .build();

        // Build system info
        WeatherResponse.SystemInfo systemInfo = WeatherResponse.SystemInfo.builder()
            .sunrise(current.getSunrise())
            .sunset(current.getSunset())
            .build();

        // Build and return response
        return WeatherResponse.builder()
            .weather(weatherInfo)
            .temperature(temperatureInfo)
            .visibility(current.getVisibility())
            .wind(windInfo)
            .datetime(current.getDatetime())
            .sys(systemInfo)
            .timezone(weatherData.getTimezoneOffset())
            .name(cityName)
            .build();
    }

    /**
     * Converts WeatherDataV2 to WeatherResponse.
     */
    private static WeatherResponse toResponseV2(WeatherDataV2 weatherData, String cityName) {
        if (weatherData.getMain() == null) {
            throw new IllegalArgumentException("WeatherDataV2 main data cannot be null");
        }

        WeatherDataV2.MainData main = weatherData.getMain();

        // Extract weather condition (first element from weather array)
        WeatherResponse.WeatherInfo weatherInfo = null;
        if (weatherData.getWeather() != null && !weatherData.getWeather().isEmpty()) {
            WeatherDataV2.WeatherCondition condition = weatherData.getWeather().getFirst();
            weatherInfo = WeatherResponse.WeatherInfo.builder()
                .main(condition.getMain())
                .description(condition.getDescription())
                .build();
        }

        // Build temperature info
        WeatherResponse.TemperatureInfo temperatureInfo = WeatherResponse.TemperatureInfo.builder()
            .temp(main.getTemp())
            .feelsLike(main.getFeelsLike())
            .build();

        // Build wind info
        WeatherResponse.WindInfo windInfo = null;
        if (weatherData.getWind() != null) {
            windInfo = WeatherResponse.WindInfo.builder()
                .speed(weatherData.getWind().getSpeed())
                .build();
        }

        // Build system info
        WeatherResponse.SystemInfo systemInfo = null;
        if (weatherData.getSys() != null) {
            systemInfo = WeatherResponse.SystemInfo.builder()
                .sunrise(weatherData.getSys().getSunrise())
                .sunset(weatherData.getSys().getSunset())
                .build();
        }

        // Use city name from response if available, otherwise use provided name
        String finalCityName = weatherData.getName() != null ? weatherData.getName() : cityName;

        // Build and return response
        return WeatherResponse.builder()
            .weather(weatherInfo)
            .temperature(temperatureInfo)
            .visibility(weatherData.getVisibility())
            .wind(windInfo)
            .datetime(weatherData.getDatetime())
            .sys(systemInfo)
            .timezone(weatherData.getTimezone())
            .name(finalCityName)
            .build();
    }
}
