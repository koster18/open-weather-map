package ru.sterkhovkv.openweathermap.client;

import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.model.Coordinates;

/**
 * Common interface for weather API clients (both v2.5 and v3.0).
 * Returns weather data as a generic object that will be converted to WeatherResponse.
 */
public interface WeatherApiClient {

    /**
     * Fetches weather data for given coordinates.
     *
     * @param coordinates geographic coordinates
     * @return weather data as Object (WeatherDataV2 or WeatherDataV3)
     * @throws ru.sterkhovkv.openweathermap.exception.NetworkException       if network error occurs
     * @throws ru.sterkhovkv.openweathermap.exception.ApiRateLimitException  if rate limit exceeded
     * @throws ru.sterkhovkv.openweathermap.exception.BadRequestException    if request is invalid
     * @throws ru.sterkhovkv.openweathermap.exception.InvalidApiKeyException if API key is invalid
     */
    Object fetchWeather(Coordinates coordinates);

    /**
     * Gets the API version this client supports.
     *
     * @return API version
     */
    ApiVersion getApiVersion();
}
