package ru.sterkhovkv.openweathermap.api;

/**
 * SDK operation mode.
 */
public enum SDKMode {
    
    /**
     * On-demand mode: weather data is fetched from API only when requested.
     * Data is cached and returned if still valid (less than TTL time passed).
     */
    ON_DEMAND,
    
    /**
     * Polling mode: weather data is automatically updated in background
     * for all cached cities at regular intervals.
     * Requests always return data from cache (zero latency).
     */
    POLLING
}

