package ru.sterkhovkv.openweathermap.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sterkhovkv.openweathermap.cache.WeatherCache;
import ru.sterkhovkv.openweathermap.client.WeatherApiClient;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.PollingStrategy;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WeatherPollingSchedulerTest {

    private static final long POLLING_INTERVAL_MINUTES = 1;
    private static final long CACHE_TTL_MINUTES = 10;
    private static final long PREEMPTIVE_EPSILON_MINUTES = 1;
    private static final long VERIFY_TIMEOUT_MILLIS = 2000;
    private static final long TEST_SLEEP_MILLIS = 1500;
    private static final String TEST_CITY_MOSCOW = "Moscow";
    private static final String TEST_CITY_LONDON = "London";
    private static final double MOSCOW_LAT = 55.7558;
    private static final double MOSCOW_LON = 37.6173;
    private static final double LONDON_LAT = 51.5074;
    private static final double LONDON_LON = -0.1278;
    private static final String NETWORK_ERROR_MESSAGE = "Network error";

    @Mock
    private WeatherCache cache;

    @Mock
    private WeatherApiClient weatherApiClient;

    private PollingSchedulerConfig config;
    private WeatherPollingScheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        config = new PollingSchedulerConfig(
            cache,
            weatherApiClient,
            ApiVersion.V2_5,
            POLLING_INTERVAL_MINUTES,
            CACHE_TTL_MINUTES,
            PollingStrategy.STRICT,
            PREEMPTIVE_EPSILON_MINUTES
        );

        scheduler = new WeatherPollingScheduler(config);
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.stop();
        }
    }

    @Test
    void testStart() {
        when(cache.getAllCities()).thenReturn(new ArrayList<>());

        scheduler.start();

        verify(cache, timeout(VERIFY_TIMEOUT_MILLIS).atLeastOnce()).getAllCities();
    }

    @Test
    void testStartTwice() {
        when(cache.getAllCities()).thenReturn(new ArrayList<>());

        scheduler.start();
        scheduler.start();

        verify(cache, timeout(VERIFY_TIMEOUT_MILLIS).atLeastOnce()).getAllCities();
    }

    @Test
    void testStop() {
        scheduler.start();

        assertDoesNotThrow(() -> {
            scheduler.stop();
        });
    }

    @Test
    void testStopTwice() {
        scheduler.start();
        scheduler.stop();

        assertDoesNotThrow(() -> {
            scheduler.stop();
        });
    }

    @Test
    void testUpdateExpiredCities() throws Exception {
        List<String> cities = List.of(TEST_CITY_MOSCOW, TEST_CITY_LONDON);
        when(cache.getAllCities()).thenReturn(cities);

        Coordinates moscowCoords = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        Coordinates londonCoords = new Coordinates(LONDON_LAT, LONDON_LON);

        when(cache.getCoordinates(TEST_CITY_MOSCOW)).thenReturn(moscowCoords);
        when(cache.getCoordinates(TEST_CITY_LONDON)).thenReturn(londonCoords);

        WeatherDataV2 weatherData = new WeatherDataV2();
        when(weatherApiClient.fetchWeather(any(Coordinates.class))).thenReturn(weatherData);

        scheduler.start();

        Thread.sleep(TEST_SLEEP_MILLIS);

        verify(cache, atLeastOnce()).getAllCities();
        verify(weatherApiClient, atLeastOnce()).fetchWeather(any(Coordinates.class));
    }

    @Test
    void testUpdateWithEmptyCache() {
        when(cache.getAllCities()).thenReturn(new ArrayList<>());

        scheduler.start();

        try {
            Thread.sleep(TEST_SLEEP_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(cache, atLeastOnce()).getAllCities();
        verify(weatherApiClient, never()).fetchWeather(any(Coordinates.class));
    }

    @Test
    void testUpdateWithMissingCoordinates() {
        List<String> cities = List.of(TEST_CITY_MOSCOW);
        when(cache.getAllCities()).thenReturn(cities);
        when(cache.getCoordinates(TEST_CITY_MOSCOW)).thenReturn(null);

        scheduler.start();

        try {
            Thread.sleep(TEST_SLEEP_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(cache, atLeastOnce()).getCoordinates(TEST_CITY_MOSCOW);
        verify(cache, atLeastOnce()).remove(TEST_CITY_MOSCOW);
        verify(weatherApiClient, never()).fetchWeather(any(Coordinates.class));
    }

    @Test
    void testUpdateHandlesNetworkException() throws Exception {
        List<String> cities = List.of(TEST_CITY_MOSCOW);
        when(cache.getAllCities()).thenReturn(cities);

        Coordinates coords = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        when(cache.getCoordinates(TEST_CITY_MOSCOW)).thenReturn(coords);

        when(weatherApiClient.fetchWeather(any(Coordinates.class)))
            .thenThrow(new NetworkException(NETWORK_ERROR_MESSAGE));

        scheduler.start();

        Thread.sleep(TEST_SLEEP_MILLIS);

        verify(weatherApiClient, atLeastOnce()).fetchWeather(any(Coordinates.class));
    }
}

