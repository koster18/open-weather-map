package ru.sterkhovkv.openweathermap.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Temperature units for weather data.
 * Used in OpenWeather API requests to specify measurement units.
 */
@Getter
@AllArgsConstructor
public enum TemperatureUnits {

    /**
     * Standard units: Kelvin for temperature, meter/sec for wind speed.
     * Default OpenWeather API units.
     */
    STANDARD("standard"),

    /**
     * Metric units: Celsius for temperature, meter/sec for wind speed.
     */
    METRIC("metric"),

    /**
     * Imperial units: Fahrenheit for temperature, miles/hour for wind speed.
     */
    IMPERIAL("imperial");

    /**
     * -- GETTER --
     * Gets the API value for this unit.
     */
    private final String apiValue;
}
