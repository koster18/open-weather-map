package ru.sterkhovkv.openweathermap.cache;

import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.model.CacheEntry;
import ru.sterkhovkv.openweathermap.model.Coordinates;

import java.util.List;

/**
 * Interface for weather data cache.
 */
public interface WeatherCache {

    /**
     * Gets weather data from cache if it exists and is still valid.
     *
     * @param cityName city name
     * @return cached weather data, or null if not found or expired
     */
    CacheEntry get(String cityName);

    /**
     * Puts weather data into cache.
     *
     * @param cityName    city name
     * @param coordinates city coordinates
     * @param weatherData weather data (WeatherDataV2 or WeatherDataV3)
     * @param apiVersion  API version used
     * @param timestamp   timestamp in milliseconds
     */
    void put(String cityName, Coordinates coordinates, Object weatherData, ApiVersion apiVersion, long timestamp);

    /**
     * Updates existing cache entry with new weather data.
     *
     * @param cityName    city name
     * @param weatherData new weather data (WeatherDataV2 or WeatherDataV3)
     * @param apiVersion  API version used
     * @param timestamp   new timestamp
     */
    void update(String cityName, Object weatherData, ApiVersion apiVersion, long timestamp);

    /**
     * Checks if cache entry exists and is valid.
     *
     * @param cityName    city name
     * @param currentTime current timestamp in milliseconds
     * @param ttlMillis   time-to-live in milliseconds
     * @return true if entry exists and is valid, false otherwise
     */
    boolean isValid(String cityName, long currentTime, long ttlMillis);

    /**
     * Gets coordinates for a city from cache.
     *
     * @param cityName city name
     * @return coordinates, or null if not found
     */
    Coordinates getCoordinates(String cityName);

    /**
     * Gets all city names currently in cache.
     *
     * @return list of city names
     */
    List<String> getAllCities();

    /**
     * Removes a city from cache.
     *
     * @param cityName city name to remove
     */
    void remove(String cityName);

    /**
     * Clears all cache entries.
     */
    void clear();

    /**
     * Gets current cache size.
     *
     * @return number of entries in cache
     */
    int size();
}

