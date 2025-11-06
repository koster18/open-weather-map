package ru.sterkhovkv.openweathermap.scheduler;

import ru.sterkhovkv.openweathermap.cache.WeatherCache;
import ru.sterkhovkv.openweathermap.client.WeatherApiClient;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.PollingStrategy;

/**
 * Configuration for WeatherPollingScheduler.
 */
public record PollingSchedulerConfig(
    WeatherCache cache,
    WeatherApiClient weatherApiClient,
    ApiVersion apiVersion,
    long pollingIntervalMinutes,
    long cacheTtlMinutes,
    PollingStrategy pollingStrategy,
    long preemptiveEpsilonMinutes
) {
    public PollingSchedulerConfig {
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null");
        }
        if (weatherApiClient == null) {
            throw new IllegalArgumentException("Weather API client cannot be null");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("API version cannot be null");
        }
        if (pollingIntervalMinutes <= 0) {
            throw new IllegalArgumentException("Polling interval must be positive");
        }
        if (cacheTtlMinutes <= 0) {
            throw new IllegalArgumentException("Cache TTL must be positive");
        }
        if (pollingStrategy == null) {
            throw new IllegalArgumentException("Polling strategy cannot be null");
        }
        if (preemptiveEpsilonMinutes < 0) {
            throw new IllegalArgumentException("Preemptive epsilon cannot be negative");
        }
    }
}

