package ru.sterkhovkv.openweathermap.scheduler;

import ru.sterkhovkv.openweathermap.cache.WeatherCache;
import ru.sterkhovkv.openweathermap.config.PollingStrategy;
import ru.sterkhovkv.openweathermap.model.CacheEntry;

/**
 * Strategy for deciding whether to update a city in polling mode.
 */
final class UpdateDecisionStrategy {

    private UpdateDecisionStrategy() {
    }

    /**
     * Determines if a city should be updated based on the polling strategy.
     *
     * @param strategy      polling strategy
     * @param cache         weather cache
     * @param cityName      city name
     * @param currentTime   current timestamp in milliseconds
     * @param ttlMillis     time-to-live in milliseconds
     * @param epsilonMillis epsilon in milliseconds for PREEMPTIVE_EPSILON strategy
     * @return true if city should be updated, false otherwise
     */
    static boolean shouldUpdate(
        PollingStrategy strategy,
        WeatherCache cache,
        String cityName,
        long currentTime,
        long ttlMillis,
        long epsilonMillis) {

        return switch (strategy) {
            case STRICT -> true;
            case EXPIRED_ONLY -> !cache.isValid(cityName, currentTime, ttlMillis);
            case PREEMPTIVE_EPSILON -> shouldUpdatePreemptive(cache, cityName, currentTime, ttlMillis, epsilonMillis);
        };
    }

    private static boolean shouldUpdatePreemptive(
        WeatherCache cache,
        String cityName,
        long currentTime,
        long ttlMillis,
        long epsilonMillis) {

        CacheEntry entry = cache.get(cityName);
        if (entry == null) {
            return true; // expired or not found
        }

        long age = currentTime - entry.timestamp();
        long remaining = ttlMillis - age;
        return remaining <= Math.max(0, epsilonMillis);
    }
}

