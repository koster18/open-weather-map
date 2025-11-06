package ru.sterkhovkv.openweathermap.config;

import lombok.Getter;
import ru.sterkhovkv.openweathermap.exception.ApiRateLimitException;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter for API calls to prevent exceeding API quotas.
 * Tracks calls per day and per minute.
 */
public class ApiRateLimiter {

    @Getter
    private final int maxCallsPerDay;

    @Getter
    private final int maxCallsPerMinute;

    private final AtomicInteger callsToday = new AtomicInteger(0);
    private final Queue<Long> callTimestamps = new ConcurrentLinkedQueue<>();
    private volatile long dayResetTime;

    public ApiRateLimiter(int maxCallsPerDay, int maxCallsPerMinute) {
        if (maxCallsPerDay <= 0 || maxCallsPerMinute <= 0) {
            throw new IllegalArgumentException("Rate limits must be positive");
        }
        this.maxCallsPerDay = maxCallsPerDay;
        this.maxCallsPerMinute = maxCallsPerMinute;
        this.dayResetTime = calculateNextDayReset();
    }

    /**
     * Checks if a new API call can be made and registers it.
     * Throws ApiRateLimitException if limits are exceeded.
     *
     * @throws ApiRateLimitException if daily or per-minute limit is exceeded
     */
    public synchronized void checkAndAcquire() {
        long currentTime = System.currentTimeMillis();

        // Reset daily counter if needed
        if (currentTime >= dayResetTime) {
            resetDailyCounter();
        }

        // Check daily limit
        if (callsToday.get() >= maxCallsPerDay) {
            throw new ApiRateLimitException(
                String.format("Daily API limit exceeded: %d calls per day", maxCallsPerDay)
            );
        }

        // Clean up old timestamps (older than 1 minute)
        cleanupOldTimestamps(currentTime);

        // Check per-minute limit
        int callsLastMinute = callTimestamps.size();
        if (callsLastMinute >= maxCallsPerMinute) {
            throw new ApiRateLimitException(
                String.format("Per-minute API limit exceeded: %d calls per minute", maxCallsPerMinute)
            );
        }

        // Register the call
        callTimestamps.offer(currentTime);
        callsToday.incrementAndGet();
    }

    /**
     * Removes timestamps older than 1 minute.
     *
     * @param currentTime current timestamp in milliseconds
     */
    private void cleanupOldTimestamps(long currentTime) {
        long oneMinuteAgo = currentTime - Constants.MILLIS_PER_MINUTE;
        while (!callTimestamps.isEmpty()) {
            Long oldest = callTimestamps.peek();
            if (oldest != null && oldest < oneMinuteAgo) {
                callTimestamps.poll();
            } else {
                break;
            }
        }
    }

    /**
     * Resets daily counter and updates reset time.
     */
    private void resetDailyCounter() {
        callsToday.set(0);
        dayResetTime = calculateNextDayReset();
    }

    /**
     * Calculates next day reset time (midnight UTC).
     *
     * @return timestamp of next midnight UTC
     */
    private long calculateNextDayReset() {
        long now = System.currentTimeMillis();
        // Calculate milliseconds until next midnight UTC
        long millisInDay = Constants.MILLIS_PER_DAY;
        return ((now / millisInDay) + 1) * millisInDay;
    }

    /**
     * Gets current number of calls made today.
     *
     * @return number of calls today
     */
    public int getCallsToday() {
        return callsToday.get();
    }

    /**
     * Gets current number of calls in the last minute.
     *
     * @return number of calls in the last minute
     */
    public int getCallsLastMinute() {
        cleanupOldTimestamps(System.currentTimeMillis());
        return callTimestamps.size();
    }
}
