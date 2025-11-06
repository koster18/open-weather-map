package ru.sterkhovkv.openweathermap.model;

import org.junit.jupiter.api.Test;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;
import ru.sterkhovkv.openweathermap.util.Constants;

import static org.junit.jupiter.api.Assertions.*;

class CacheEntryTest {

    private static final String TEST_CITY_NAME = "Moscow";
    private static final double TEST_LAT = 55.7558;
    private static final double TEST_LON = 37.6173;
    private static final long ONE_SECOND_MILLIS = 1000;
    private static final long TEN_SECONDS_MILLIS = 10 * Constants.MILLIS_PER_MINUTE / 6;
    private static final long TWENTY_SECONDS_MILLIS = 20 * Constants.MILLIS_PER_MINUTE / 3;
    private static final long TEN_MINUTES_MILLIS = 10 * Constants.MILLIS_PER_MINUTE;

    @Test
    void testCacheEntryCreation() {
        Coordinates coordinates = new Coordinates(TEST_LAT, TEST_LON);
        Object weatherData = new WeatherDataV2();
        ApiVersion apiVersion = ApiVersion.V2_5;
        long timestamp = System.currentTimeMillis();

        CacheEntry entry = new CacheEntry(TEST_CITY_NAME, coordinates, weatherData, apiVersion, timestamp);

        assertEquals(TEST_CITY_NAME, entry.cityName());
        assertEquals(coordinates, entry.coordinates());
        assertEquals(weatherData, entry.weatherData());
        assertEquals(apiVersion, entry.apiVersion());
        assertEquals(timestamp, entry.timestamp());
    }

    @Test
    void testIsValid() {
        long timestamp = System.currentTimeMillis();
        CacheEntry entry = new CacheEntry(
            TEST_CITY_NAME,
            new Coordinates(TEST_LAT, TEST_LON),
            new WeatherDataV2(),
            ApiVersion.V2_5,
            timestamp
        );

        long currentTime = timestamp + ONE_SECOND_MILLIS;
        long ttlMillis = TEN_MINUTES_MILLIS;

        assertTrue(entry.isValid(currentTime, ttlMillis));
    }

    @Test
    void testIsValidExpired() {
        long timestamp = System.currentTimeMillis() - TWENTY_SECONDS_MILLIS;
        CacheEntry entry = new CacheEntry(
            TEST_CITY_NAME,
            new Coordinates(TEST_LAT, TEST_LON),
            new WeatherDataV2(),
            ApiVersion.V2_5,
            timestamp
        );

        long currentTime = System.currentTimeMillis();
        long ttlMillis = TEN_SECONDS_MILLIS;

        assertFalse(entry.isValid(currentTime, ttlMillis));
    }

    @Test
    void testIsValidExactlyAtTTL() {
        long timestamp = System.currentTimeMillis();
        CacheEntry entry = new CacheEntry(
            TEST_CITY_NAME,
            new Coordinates(TEST_LAT, TEST_LON),
            new WeatherDataV2(),
            ApiVersion.V2_5,
            timestamp
        );

        long currentTime = timestamp + TEN_SECONDS_MILLIS;
        long ttlMillis = TEN_SECONDS_MILLIS;

        assertFalse(entry.isValid(currentTime, ttlMillis));
    }
}

