package ru.sterkhovkv.openweathermap.model;

import ru.sterkhovkv.openweathermap.config.ApiVersion;

/**
 * Cache entry for weather data.
 * Stores weather data with timestamp and coordinates for cache management.
 *
 * @param cityName    City name.
 * @param coordinates Geographic coordinates of the city.
 * @param weatherData Weather data (WeatherDataV2 or WeatherDataV3).
 * @param apiVersion  API version used to fetch this data.
 * @param timestamp   Timestamp when the data was fetched (Unix timestamp in milliseconds).
 */
public record CacheEntry(

    String cityName,

    Coordinates coordinates,

    Object weatherData,

    ApiVersion apiVersion,

    long timestamp

) {

    /**
     * Checks if the cache entry is still valid (not expired).
     *
     * @param currentTime current timestamp in milliseconds
     * @param ttlMillis   time-to-live in milliseconds
     * @return true if the entry is still valid, false otherwise
     */
    public boolean isValid(long currentTime, long ttlMillis) {
        return (currentTime - timestamp) < ttlMillis;
    }
}

