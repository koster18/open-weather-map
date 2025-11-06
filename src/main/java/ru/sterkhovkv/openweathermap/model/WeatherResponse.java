package ru.sterkhovkv.openweathermap.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * SDK API response model.
 * This is the structure returned to SDK users as specified in requirements.
 */
@Getter
@Builder
@AllArgsConstructor
public class WeatherResponse {

    /**
     * Weather condition information.
     */
    private WeatherInfo weather;

    /**
     * Temperature information.
     */
    private TemperatureInfo temperature;

    /**
     * Visibility in meters.
     */
    private Integer visibility;

    /**
     * Wind information.
     */
    private WindInfo wind;

    /**
     * Current datetime as Unix timestamp (UTC).
     */
    private Long datetime;

    /**
     * System information (sunrise, sunset).
     */
    private SystemInfo sys;

    /**
     * Timezone offset in seconds from UTC.
     */
    private Integer timezone;

    /**
     * City name.
     */
    private String name;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WeatherInfo {
        private String main;
        private String description;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TemperatureInfo {
        private Double temp;
        private Double feelsLike;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WindInfo {
        private Double speed;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SystemInfo {
        private Long sunrise;
        private Long sunset;
    }
}
