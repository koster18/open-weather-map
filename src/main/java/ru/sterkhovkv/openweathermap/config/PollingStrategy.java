package ru.sterkhovkv.openweathermap.config;

/**
 * Strategy for POLLING mode behavior.
 */
public enum PollingStrategy {

    /**
     * Update all cached cities on each tick.
     */
    STRICT,

    /**
     * Update only expired entries.
     */
    EXPIRED_ONLY,

    /**
     * Update when remaining TTL is below epsilon; expired entries always update.
     */
    PREEMPTIVE_EPSILON
}
