package ru.sterkhovkv.openweathermap.config;

/**
 * OpenWeather API version.
 */
public enum ApiVersion {

    /**
     * Current Weather Data API 2.5.
     * Compatible with all API keys.
     * See also: <a href="https://openweathermap.org/current">...</a>
     */
    V2_5,

    /**
     * One Call API 3.0.
     * Requires "One Call by Call" subscription.
     * See also: <a href="https://openweathermap.org/api/one-call-3">...</a>
     */
    V3_0
}

