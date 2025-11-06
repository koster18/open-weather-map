package ru.sterkhovkv.openweathermap.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.sterkhovkv.openweathermap.exception.ApiRateLimitException;

import static org.junit.jupiter.api.Assertions.*;

class ApiRateLimiterTest {

    private static final int DEFAULT_MAX_CALLS_PER_DAY = 100;
    private static final int DEFAULT_MAX_CALLS_PER_MINUTE = 10;
    private static final int TEST_MAX_CALLS_PER_DAY = 50;
    private static final int TEST_MAX_CALLS_PER_MINUTE = 5;
    private static final int SMALL_MAX_CALLS_PER_DAY = 5;
    private static final int LARGE_MAX_CALLS_PER_MINUTE = 100;
    private static final int INVALID_LIMIT_ZERO = 0;
    private static final int INVALID_LIMIT_NEGATIVE = -1;
    private static final int INITIAL_CALLS_COUNT = 0;
    private static final int FIRST_CALL_COUNT = 1;
    private static final int SECOND_CALL_COUNT = 2;
    private static final int PER_MINUTE_LIMIT_CALLS = 10;
    private static final int DAILY_LIMIT_CALLS = 5;

    private ApiRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new ApiRateLimiter(DEFAULT_MAX_CALLS_PER_DAY, DEFAULT_MAX_CALLS_PER_MINUTE);
    }

    @Test
    void testConstructor() {
        ApiRateLimiter limiter = new ApiRateLimiter(TEST_MAX_CALLS_PER_DAY, TEST_MAX_CALLS_PER_MINUTE);
        assertEquals(TEST_MAX_CALLS_PER_DAY, limiter.getMaxCallsPerDay());
        assertEquals(TEST_MAX_CALLS_PER_MINUTE, limiter.getMaxCallsPerMinute());
    }

    @Test
    void testConstructorThrowsOnInvalidLimits() {
        assertThrows(IllegalArgumentException.class, () ->
            new ApiRateLimiter(INVALID_LIMIT_ZERO, DEFAULT_MAX_CALLS_PER_MINUTE));

        assertThrows(IllegalArgumentException.class, () ->
            new ApiRateLimiter(DEFAULT_MAX_CALLS_PER_DAY, INVALID_LIMIT_ZERO));

        assertThrows(IllegalArgumentException.class, () ->
            new ApiRateLimiter(INVALID_LIMIT_NEGATIVE, DEFAULT_MAX_CALLS_PER_MINUTE));

        assertThrows(IllegalArgumentException.class, () ->
            new ApiRateLimiter(DEFAULT_MAX_CALLS_PER_DAY, INVALID_LIMIT_NEGATIVE));
    }

    @Test
    void testCheckAndAcquireWithinLimits() {
        for (int i = 0; i < PER_MINUTE_LIMIT_CALLS; i++) {
            assertDoesNotThrow(() -> rateLimiter.checkAndAcquire());
        }
        assertEquals(PER_MINUTE_LIMIT_CALLS, rateLimiter.getCallsToday());
        assertEquals(PER_MINUTE_LIMIT_CALLS, rateLimiter.getCallsLastMinute());
    }

    @Test
    void testPerMinuteLimit() {
        for (int i = 0; i < PER_MINUTE_LIMIT_CALLS; i++) {
            rateLimiter.checkAndAcquire();
        }

        assertThrows(ApiRateLimitException.class, () -> rateLimiter.checkAndAcquire());
    }

    @Test
    void testDailyLimit() {
        ApiRateLimiter smallLimiter = new ApiRateLimiter(SMALL_MAX_CALLS_PER_DAY, LARGE_MAX_CALLS_PER_MINUTE);

        for (int i = 0; i < DAILY_LIMIT_CALLS; i++) {
            smallLimiter.checkAndAcquire();
        }

        assertThrows(ApiRateLimitException.class, smallLimiter::checkAndAcquire);
    }

    @Test
    void testGetCallsToday() {
        assertEquals(INITIAL_CALLS_COUNT, rateLimiter.getCallsToday());

        rateLimiter.checkAndAcquire();
        assertEquals(FIRST_CALL_COUNT, rateLimiter.getCallsToday());

        rateLimiter.checkAndAcquire();
        assertEquals(SECOND_CALL_COUNT, rateLimiter.getCallsToday());
    }

    @Test
    void testGetCallsLastMinute() {
        assertEquals(INITIAL_CALLS_COUNT, rateLimiter.getCallsLastMinute());

        rateLimiter.checkAndAcquire();
        assertEquals(FIRST_CALL_COUNT, rateLimiter.getCallsLastMinute());

        rateLimiter.checkAndAcquire();
        assertEquals(SECOND_CALL_COUNT, rateLimiter.getCallsLastMinute());
    }
}
