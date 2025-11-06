package ru.sterkhovkv.openweathermap.scheduler;

import lombok.extern.slf4j.Slf4j;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler for background weather data updates in POLLING mode.
 * Periodically updates expired weather data for all cached cities.
 */
@Slf4j
public class WeatherPollingScheduler {

    private final PollingSchedulerConfig config;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollingTask;
    private volatile boolean stopped = false;

    public WeatherPollingScheduler(PollingSchedulerConfig config) {
        this.config = Objects.requireNonNull(config, "Config cannot be null");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WeatherPollingScheduler");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the polling scheduler.
     * Note: instance is not designed to be restarted after {@link #stop()}.
     */
    public synchronized void start() {
        if (pollingTask != null && !pollingTask.isCancelled()) {
            log.warn("Polling scheduler is already running");
            return;
        }

        stopped = false;

        pollingTask = scheduler.scheduleWithFixedDelay(
            this::updateExpiredCities,
            0,
            config.pollingIntervalMinutes(),
            TimeUnit.MINUTES
        );

        log.info("Polling scheduler started with interval: {} minutes, strategy: {}",
            config.pollingIntervalMinutes(), config.pollingStrategy());
    }

    /**
     * Stops the polling scheduler and releases resources.
     * After stop, this scheduler instance should not be started again.
     */
    public synchronized void stop() {
        if (stopped) {
            return;
        }

        stopped = true;

        if (pollingTask != null) {
            pollingTask.cancel(false);
            pollingTask = null;
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(Constants.SCHEDULER_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Polling scheduler did not terminate gracefully, forcing shutdown");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for scheduler to terminate");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Polling scheduler stopped");
    }

    /**
     * Updates expired cities in cache.
     */
    private void updateExpiredCities() {
        if (stopped) {
            return;
        }

        try {
            long tickStart = System.currentTimeMillis();
            long ttlMillis = config.cacheTtlMinutes() * Constants.MILLIS_PER_MINUTE;
            long epsilonMillis = config.preemptiveEpsilonMinutes() * Constants.MILLIS_PER_MINUTE;

            List<String> cities = config.cache().getAllCities();

            if (cities.isEmpty()) {
                log.debug("No cities in cache to update");
                return;
            }

            log.debug("Polling update for {} cached cities", cities.size());

            UpdateStats stats = processCities(cities, tickStart, ttlMillis, epsilonMillis);

            long durationMs = System.currentTimeMillis() - tickStart;
            log.info("Polling tick: strategy={}, cities={}, updated={}, failed={}, skipped={}, durationMs={}",
                config.pollingStrategy(), cities.size(), stats.updated(), stats.failed(),
                stats.skipped(), durationMs);

        } catch (Exception e) {
            log.error("Error in polling update cycle", e);
        }
    }

    /**
     * Processes all cities and returns update statistics.
     */
    private UpdateStats processCities(List<String> cities, long currentTime, long ttlMillis, long epsilonMillis) {
        int updated = 0;
        int failed = 0;
        int skipped = 0;

        for (String cityName : cities) {
            if (stopped) {
                break;
            }

            UpdateResult result = updateSingleCity(cityName, currentTime, ttlMillis, epsilonMillis);
            switch (result) {
                case UPDATED -> updated++;
                case FAILED -> failed++;
                case SKIPPED -> skipped++;
            }
        }

        return new UpdateStats(updated, failed, skipped);
    }

    /**
     * Updates a single city if needed.
     *
     * @return update result
     */
    private UpdateResult updateSingleCity(String cityName, long currentTime, long ttlMillis, long epsilonMillis) {
        if (!shouldUpdateCity(cityName, currentTime, ttlMillis, epsilonMillis)) {
            return UpdateResult.SKIPPED;
        }

        Coordinates coordinates = config.cache().getCoordinates(cityName);
        if (coordinates == null) {
            log.warn("Coordinates not found in cache for city: {}, removing", cityName);
            config.cache().remove(cityName);
            return UpdateResult.SKIPPED;
        }

        try {
            log.debug("Updating weather data for city: {}", cityName);
            Object weatherData = config.weatherApiClient().fetchWeather(coordinates);
            config.cache().update(cityName, weatherData, config.apiVersion(), currentTime);
            return UpdateResult.UPDATED;

        } catch (NetworkException e) {
            log.warn("Failed to update weather for city {}: {}", cityName, e.getMessage());
            return UpdateResult.FAILED;
        } catch (Exception e) {
            log.error("Unexpected error updating weather for city: {}", cityName, e);
            return UpdateResult.FAILED;
        }
    }

    /**
     * Determines if a city should be updated based on polling strategy.
     */
    private boolean shouldUpdateCity(String cityName, long currentTime, long ttlMillis, long epsilonMillis) {
        return UpdateDecisionStrategy.shouldUpdate(
            config.pollingStrategy(),
            config.cache(),
            cityName,
            currentTime,
            ttlMillis,
            epsilonMillis
        );
    }

    /**
     * Result of updating a single city.
     */
    private enum UpdateResult {
        UPDATED,
        FAILED,
        SKIPPED
    }

    /**
     * Statistics for a polling update cycle.
     */
    private record UpdateStats(int updated, int failed, int skipped) {
    }
}

