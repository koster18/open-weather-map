package ru.sterkhovkv.openweathermap.api;

import ru.sterkhovkv.openweathermap.client.WeatherApiClient;
import ru.sterkhovkv.openweathermap.client.WeatherApiV2Client;
import ru.sterkhovkv.openweathermap.client.WeatherApiV3Client;
import ru.sterkhovkv.openweathermap.config.ApiRateLimiter;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;

/**
 * Factory for creating WeatherApiClient instances based on API version.
 */
final class WeatherApiClientFactory {
    
    private WeatherApiClientFactory() {}
    
    /**
     * Creates WeatherApiClient for the specified API version.
     *
     * @param apiVersion API version
     * @param apiKey API key
     * @param config SDK configuration
     * @param rateLimiter rate limiter
     * @return WeatherApiClient instance
     */
    static WeatherApiClient create(
            ApiVersion apiVersion,
            String apiKey,
            SDKConfig config,
            ApiRateLimiter rateLimiter) {
        
        return switch (apiVersion) {
            case V2_5 -> new WeatherApiV2Client(apiKey, config, rateLimiter);
            case V3_0 -> new WeatherApiV3Client(apiKey, config, rateLimiter);
        };
    }
}

