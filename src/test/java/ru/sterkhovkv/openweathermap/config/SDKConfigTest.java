package ru.sterkhovkv.openweathermap.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SDKConfigTest {

    private static final int DEFAULT_MAX_CALLS_PER_DAY = 2000;
    private static final int DEFAULT_MAX_CALLS_PER_MINUTE = 60;
    private static final long DEFAULT_REQUEST_TIMEOUT_SECONDS = 30;
    private static final long DEFAULT_CONNECT_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_CACHE_SIZE = 10;
    private static final long DEFAULT_CACHE_TTL_MINUTES = 10;
    private static final long DEFAULT_POLLING_INTERVAL_MINUTES = 10;
    private static final long DEFAULT_PREEMPTIVE_EPSILON_MINUTES = 1;

    private static final int TEST_MAX_CALLS_PER_DAY = 1000;
    private static final int TEST_MAX_CALLS_PER_MINUTE = 30;
    private static final int TEST_CACHE_SIZE = 20;
    private static final long TEST_CACHE_TTL_MINUTES = 15;
    private static final long TEST_POLLING_INTERVAL_MINUTES = 5;
    private static final long TEST_PREEMPTIVE_EPSILON_MINUTES = 2;
    private static final String TEST_LANG = "ru";

    private static final int VALID_TEST_MAX_CALLS_PER_DAY = 1000;
    private static final int VALID_TEST_MAX_CALLS_PER_MINUTE = 50;
    private static final long VALID_TEST_REQUEST_TIMEOUT_SECONDS = 20;
    private static final long VALID_TEST_CONNECT_TIMEOUT_SECONDS = 5;
    private static final int VALID_TEST_CACHE_SIZE = 15;
    private static final long VALID_TEST_CACHE_TTL_MINUTES = 12;
    private static final long VALID_TEST_POLLING_INTERVAL_MINUTES = 8;
    private static final long VALID_TEST_PREEMPTIVE_EPSILON_MINUTES = 0;

    private static final int INVALID_VALUE_ZERO = 0;
    private static final int INVALID_VALUE_NEGATIVE = -1;

    @Test
    void testDefaultConfig() {
        SDKConfig config = SDKConfig.defaultConfig();

        assertNotNull(config);
        assertEquals(DEFAULT_MAX_CALLS_PER_DAY, config.getMaxCallsPerDay());
        assertEquals(DEFAULT_MAX_CALLS_PER_MINUTE, config.getMaxCallsPerMinute());
        assertEquals(DEFAULT_REQUEST_TIMEOUT_SECONDS, config.getRequestTimeoutSeconds());
        assertEquals(DEFAULT_CONNECT_TIMEOUT_SECONDS, config.getConnectTimeoutSeconds());
        assertEquals(DEFAULT_CACHE_SIZE, config.getCacheSize());
        assertEquals(DEFAULT_CACHE_TTL_MINUTES, config.getCacheTtlMinutes());
        assertEquals(DEFAULT_POLLING_INTERVAL_MINUTES, config.getPollingIntervalMinutes());
        assertEquals(PollingStrategy.STRICT, config.getPollingStrategy());
        assertEquals(DEFAULT_PREEMPTIVE_EPSILON_MINUTES, config.getPreemptiveEpsilonMinutes());
        assertEquals(ApiVersion.V3_0, config.getApiVersion());
        assertEquals(TemperatureUnits.STANDARD, config.getUnits());
    }

    @Test
    void testBuilder() {
        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .maxCallsPerDay(TEST_MAX_CALLS_PER_DAY)
            .maxCallsPerMinute(TEST_MAX_CALLS_PER_MINUTE)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .pollingIntervalMinutes(TEST_POLLING_INTERVAL_MINUTES)
            .pollingStrategy(PollingStrategy.PREEMPTIVE_EPSILON)
            .preemptiveEpsilonMinutes(TEST_PREEMPTIVE_EPSILON_MINUTES)
            .units(TemperatureUnits.METRIC)
            .lang(TEST_LANG)
            .build();

        assertEquals(ApiVersion.V2_5, config.getApiVersion());
        assertEquals(TEST_MAX_CALLS_PER_DAY, config.getMaxCallsPerDay());
        assertEquals(TEST_MAX_CALLS_PER_MINUTE, config.getMaxCallsPerMinute());
        assertEquals(TEST_CACHE_SIZE, config.getCacheSize());
        assertEquals(TEST_CACHE_TTL_MINUTES, config.getCacheTtlMinutes());
        assertEquals(TEST_POLLING_INTERVAL_MINUTES, config.getPollingIntervalMinutes());
        assertEquals(PollingStrategy.PREEMPTIVE_EPSILON, config.getPollingStrategy());
        assertEquals(TEST_PREEMPTIVE_EPSILON_MINUTES, config.getPreemptiveEpsilonMinutes());
        assertEquals(TemperatureUnits.METRIC, config.getUnits());
        assertEquals(TEST_LANG, config.getLang());
    }

    @Test
    void testValidateMaxCallsPerDay() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .maxCallsPerDay(INVALID_VALUE_ZERO)
            .build()
            .validate());

        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .maxCallsPerDay(INVALID_VALUE_NEGATIVE)
            .build()
            .validate());
    }

    @Test
    void testValidateMaxCallsPerMinute() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .maxCallsPerMinute(INVALID_VALUE_ZERO)
            .build()
            .validate());
    }

    @Test
    void testValidateRequestTimeout() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .requestTimeoutSeconds(INVALID_VALUE_ZERO)
            .build()
            .validate());
    }

    @Test
    void testValidateConnectTimeout() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .connectTimeoutSeconds(INVALID_VALUE_ZERO)
            .build()
            .validate());
    }

    @Test
    void testValidateCacheSize() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .cacheSize(INVALID_VALUE_ZERO)
            .build()
            .validate());
    }

    @Test
    void testValidateCacheTtlMinutes() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .cacheTtlMinutes(INVALID_VALUE_ZERO)
            .build()
            .validate());
    }

    @Test
    void testValidatePollingIntervalMinutes() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .pollingIntervalMinutes(INVALID_VALUE_ZERO)
            .build()
            .validate());
    }

    @Test
    void testValidatePollingStrategy() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .pollingStrategy(null)
            .build()
            .validate());
    }

    @Test
    void testValidatePreemptiveEpsilonMinutes() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .preemptiveEpsilonMinutes(INVALID_VALUE_NEGATIVE)
            .build()
            .validate());
    }

    @Test
    void testValidateApiVersion() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .apiVersion(null)
            .build()
            .validate());
    }

    @Test
    void testValidateUnits() {
        assertThrows(IllegalArgumentException.class, () -> SDKConfig.builder()
            .units(null)
            .build()
            .validate());
    }

    @Test
    void testValidConfig() {
        SDKConfig config = SDKConfig.builder()
            .maxCallsPerDay(VALID_TEST_MAX_CALLS_PER_DAY)
            .maxCallsPerMinute(VALID_TEST_MAX_CALLS_PER_MINUTE)
            .requestTimeoutSeconds(VALID_TEST_REQUEST_TIMEOUT_SECONDS)
            .connectTimeoutSeconds(VALID_TEST_CONNECT_TIMEOUT_SECONDS)
            .cacheSize(VALID_TEST_CACHE_SIZE)
            .cacheTtlMinutes(VALID_TEST_CACHE_TTL_MINUTES)
            .pollingIntervalMinutes(VALID_TEST_POLLING_INTERVAL_MINUTES)
            .pollingStrategy(PollingStrategy.STRICT)
            .preemptiveEpsilonMinutes(VALID_TEST_PREEMPTIVE_EPSILON_MINUTES)
            .apiVersion(ApiVersion.V2_5)
            .units(TemperatureUnits.IMPERIAL)
            .build()
            .validate();

        assertNotNull(config);
    }
}
