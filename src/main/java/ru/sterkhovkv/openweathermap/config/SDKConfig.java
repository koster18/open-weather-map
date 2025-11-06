package ru.sterkhovkv.openweathermap.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Configuration class for OpenWeatherMap SDK.
 * Provides default values and allows customization of SDK behavior.
 */
@Getter
@Builder
@AllArgsConstructor
public class SDKConfig {
    
    /**
     * Maximum number of API calls per day.
     * Default: 2000 (OpenWeather One Call API 3.0 default limit).
     */
    @Builder.Default
    private int maxCallsPerDay = 2000;
    
    /**
     * Maximum number of API calls per minute.
     * Default: 60 (reasonable limit to prevent rate limiting).
     */
    @Builder.Default
    private int maxCallsPerMinute = 60;
    
    /**
     * Request timeout in seconds.
     * Default: 30 seconds.
     */
    @Builder.Default
    private long requestTimeoutSeconds = 30;
    
    /**
     * Connection timeout in seconds.
     * Default: 10 seconds.
     */
    @Builder.Default
    private long connectTimeoutSeconds = 10;
    
    /**
     * Maximum cache size (number of cities).
     * Default: 10 (as per requirements).
     */
    @Builder.Default
    private int cacheSize = 10;
    
    /**
     * Cache time-to-live in minutes.
     * Weather data is considered up-to-date if less than this time has passed.
     * Default: 10 minutes (as per requirements).
     */
    @Builder.Default
    private long cacheTtlMinutes = 10;
    
    /**
     * Polling interval in minutes.
     * How often the SDK should update weather data in polling mode.
     * Default: 10 minutes (matches cache TTL).
     */
    @Builder.Default
    private long pollingIntervalMinutes = 10;
    
    /**
     * Polling strategy for background updates in POLLING mode.
     * Default: STRICT (update all cities each tick).
     */
    @Builder.Default
    private PollingStrategy pollingStrategy = PollingStrategy.STRICT;
    
    /**
     * Epsilon in minutes for PREEMPTIVE_EPSILON strategy. If remaining TTL is below this value,
     * the entry will be refreshed on the current tick. Ignored for other strategies.
     * Default: 1 minute.
     */
    @Builder.Default
    private long preemptiveEpsilonMinutes = 1;
    
    /**
     * OpenWeather API version to use.
     * Default: V3_0 (One Call API 3.0).
     * Use V2_5 if your API key doesn't support 3.0.
     */
    @Builder.Default
    private ApiVersion apiVersion = ApiVersion.V3_0;
    
    /**
     * Temperature units for weather data.
     * Default: STANDARD (Kelvin for temperature, meter/sec for wind speed).
     * Use METRIC for Celsius or IMPERIAL for Fahrenheit.
     */
    @Getter
    @Builder.Default
    private TemperatureUnits units = TemperatureUnits.STANDARD;
    
    /**
     * Language for weather descriptions.
     * Optional parameter. If not set, API returns English by default.
     * Supported languages: en, ru, es, etc. See OpenWeather API documentation.
     */
    @Getter
    private String lang;
    
    /**
     * Creates a default configuration.
     *
     * @return default SDK configuration
     */
    public static SDKConfig defaultConfig() {
        return SDKConfig.builder().build().validate();
    }
    
    /**
     * Validates configuration values.
     * Throws IllegalArgumentException if any value is invalid.
     *
     * @return this instance for method chaining
     * @throws IllegalArgumentException if any configuration value is invalid
     */
    public SDKConfig validate() {
        if (maxCallsPerDay <= 0) {
            throw new IllegalArgumentException("maxCallsPerDay must be positive");
        }
        if (maxCallsPerMinute <= 0) {
            throw new IllegalArgumentException("maxCallsPerMinute must be positive");
        }
        if (requestTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("requestTimeoutSeconds must be positive");
        }
        if (connectTimeoutSeconds <= 0) {
            throw new IllegalArgumentException("connectTimeoutSeconds must be positive");
        }
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must be positive");
        }
        if (cacheTtlMinutes <= 0) {
            throw new IllegalArgumentException("cacheTtlMinutes must be positive");
        }
        if (pollingIntervalMinutes <= 0) {
            throw new IllegalArgumentException("pollingIntervalMinutes must be positive");
        }
        if (pollingStrategy == null) {
            throw new IllegalArgumentException("pollingStrategy cannot be null");
        }
        if (preemptiveEpsilonMinutes < 0) {
            throw new IllegalArgumentException("preemptiveEpsilonMinutes cannot be negative");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("apiVersion cannot be null");
        }
        if (units == null) {
            throw new IllegalArgumentException("units cannot be null");
        }
        return this;
    }
}
