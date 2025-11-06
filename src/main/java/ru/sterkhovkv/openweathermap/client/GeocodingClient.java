package ru.sterkhovkv.openweathermap.client;

import ru.sterkhovkv.openweathermap.model.Coordinates;

/**
 * Client for OpenWeather Geocoding API.
 * Converts city names to geographic coordinates.
 */
public interface GeocodingClient {

    /**
     * Gets coordinates for a city name.
     * Returns coordinates of the first city found by the search.
     *
     * @param cityName city name
     * @return coordinates (lat, lon)
     * @throws ru.sterkhovkv.openweathermap.exception.CityNotFoundException if city not found
     * @throws ru.sterkhovkv.openweathermap.exception.NetworkException      if network error occurs
     * @throws ru.sterkhovkv.openweathermap.exception.ApiRateLimitException if rate limit exceeded
     * @throws ru.sterkhovkv.openweathermap.exception.BadRequestException   if request is invalid
     */
    Coordinates getCoordinates(String cityName);
}

