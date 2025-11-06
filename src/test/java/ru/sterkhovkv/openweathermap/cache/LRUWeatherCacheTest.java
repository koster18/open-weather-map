package ru.sterkhovkv.openweathermap.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.exception.CacheException;
import ru.sterkhovkv.openweathermap.model.CacheEntry;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LRUWeatherCacheTest {

    private static final int DEFAULT_CACHE_SIZE = 5;
    private static final long DEFAULT_CACHE_TTL_MINUTES = 10;
    private static final int TEST_CACHE_SIZE_LARGE = 10;
    private static final long TEST_CACHE_TTL_LARGE = 15;
    private static final int SMALL_CACHE_SIZE = 2;
    private static final long SHORT_TTL_MINUTES = 1;
    private static final int INVALID_CACHE_SIZE_ZERO = 0;
    private static final int INVALID_CACHE_SIZE_NEGATIVE = -1;
    private static final long INVALID_TTL_ZERO = 0;
    private static final long INVALID_TTL_NEGATIVE = -1;
    private static final int EMPTY_CACHE_SIZE = 0;
    private static final int SINGLE_ENTRY_CACHE_SIZE = 1;
    private static final int TWO_ENTRIES_CACHE_SIZE = 2;
    private static final int ITERATION_COUNT = 10;
    private static final long SLEEP_MILLIS = 100;
    private static final long ONE_SECOND_MILLIS = 1000;
    private static final long TEN_SECONDS_MILLIS = 10 * Constants.MILLIS_PER_MINUTE / 6;
    private static final long TWENTY_SECONDS_MILLIS = 20 * Constants.MILLIS_PER_MINUTE / 3;
    private static final long TWO_MINUTES_MILLIS = 2 * Constants.MILLIS_PER_MINUTE;
    private static final long TEN_MINUTES_MILLIS = 10 * Constants.MILLIS_PER_MINUTE;
    private static final long ONE_MINUTE_MILLIS = Constants.MILLIS_PER_MINUTE;

    private static final String TEST_CITY_MOSCOW = "Moscow";
    private static final String TEST_CITY_LONDON = "London";
    private static final String TEST_CITY_PARIS = "Paris";
    private static final String TEST_CITY_BERLIN = "Berlin";
    private static final String TEST_CITY_TOKYO = "Tokyo";
    private static final String TEST_CITY_NON_EXISTENT = "NonExistent";
    private static final String EMPTY_STRING = "";
    private static final String BLANK_STRING = "   ";

    private static final double MOSCOW_LAT = 55.7558;
    private static final double MOSCOW_LON = 37.6173;
    private static final double LONDON_LAT = 51.5074;
    private static final double LONDON_LON = -0.1278;
    private static final double PARIS_LAT = 48.8566;
    private static final double PARIS_LON = 2.3522;
    private static final double BERLIN_LAT = 52.5200;
    private static final double BERLIN_LON = 13.4050;
    private static final double TOKYO_LAT = 35.6762;
    private static final double TOKYO_LON = 139.6503;

    private LRUWeatherCache cache;
    private Coordinates testCoordinates;
    private Object testWeatherData;

    @BeforeEach
    void setUp() {
        cache = new LRUWeatherCache(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TTL_MINUTES);
        testCoordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        testWeatherData = new WeatherDataV2();
    }

    @Test
    void testConstructor() {
        LRUWeatherCache newCache = new LRUWeatherCache(TEST_CACHE_SIZE_LARGE, TEST_CACHE_TTL_LARGE);
        assertNotNull(newCache);
        assertEquals(EMPTY_CACHE_SIZE, newCache.size());
    }

    @Test
    void testConstructorThrowsOnInvalidMaxSize() {
        assertThrows(IllegalArgumentException.class, () ->
            new LRUWeatherCache(INVALID_CACHE_SIZE_ZERO, DEFAULT_CACHE_TTL_MINUTES));

        assertThrows(IllegalArgumentException.class, () ->
            new LRUWeatherCache(INVALID_CACHE_SIZE_NEGATIVE, DEFAULT_CACHE_TTL_MINUTES));
    }

    @Test
    void testConstructorThrowsOnInvalidTtl() {
        assertThrows(IllegalArgumentException.class, () ->
            new LRUWeatherCache(TEST_CACHE_SIZE_LARGE, INVALID_TTL_ZERO));

        assertThrows(IllegalArgumentException.class, () ->
            new LRUWeatherCache(TEST_CACHE_SIZE_LARGE, INVALID_TTL_NEGATIVE));
    }

    @Test
    void testPutAndGet() {
        long timestamp = System.currentTimeMillis();
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);

        CacheEntry entry = cache.get(TEST_CITY_MOSCOW);
        assertNotNull(entry);
        assertEquals(TEST_CITY_MOSCOW, entry.cityName());
        assertEquals(testCoordinates, entry.coordinates());
        assertEquals(testWeatherData, entry.weatherData());
        assertEquals(ApiVersion.V2_5, entry.apiVersion());
        assertEquals(timestamp, entry.timestamp());
    }

    @Test
    void testPutThrowsOnNullCityName() {
        assertThrows(IllegalArgumentException.class, () ->
            cache.put(null, testCoordinates, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis()));

        assertThrows(IllegalArgumentException.class, () ->
            cache.put(EMPTY_STRING, testCoordinates, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis()));

        assertThrows(IllegalArgumentException.class, () ->
            cache.put(BLANK_STRING, testCoordinates, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis()));
    }

    @Test
    void testPutThrowsOnNullCoordinates() {
        assertThrows(IllegalArgumentException.class, () ->
            cache.put(TEST_CITY_MOSCOW, null, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis()));
    }

    @Test
    void testPutThrowsOnNullWeatherData() {
        assertThrows(IllegalArgumentException.class, () ->
            cache.put(TEST_CITY_MOSCOW, testCoordinates, null, ApiVersion.V2_5, System.currentTimeMillis()));
    }

    @Test
    void testPutThrowsOnNullApiVersion() {
        assertThrows(IllegalArgumentException.class, () ->
            cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, null, System.currentTimeMillis()));
    }

    @Test
    void testGetReturnsNullForNonExistentCity() {
        CacheEntry entry = cache.get(TEST_CITY_NON_EXISTENT);
        assertNull(entry);
    }

    @Test
    void testGetReturnsNullForNullCityName() {
        CacheEntry entry = cache.get(null);
        assertNull(entry);
    }

    @Test
    void testGetReturnsNullForExpiredEntry() {
        LRUWeatherCache shortTtlCache = new LRUWeatherCache(DEFAULT_CACHE_SIZE, SHORT_TTL_MINUTES);

        long expiredTimestamp = System.currentTimeMillis() - TWO_MINUTES_MILLIS;
        shortTtlCache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, expiredTimestamp);

        CacheEntry entry = shortTtlCache.get(TEST_CITY_MOSCOW);
        assertNull(entry);
    }

    @Test
    void testUpdate() {
        long timestamp = System.currentTimeMillis();
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);

        Object newWeatherData = new WeatherDataV2();
        long newTimestamp = System.currentTimeMillis();
        cache.update(TEST_CITY_MOSCOW, newWeatherData, ApiVersion.V3_0, newTimestamp);

        CacheEntry entry = cache.get(TEST_CITY_MOSCOW);
        assertNotNull(entry);
        assertEquals(newWeatherData, entry.weatherData());
        assertEquals(ApiVersion.V3_0, entry.apiVersion());
        assertEquals(newTimestamp, entry.timestamp());
        assertEquals(testCoordinates, entry.coordinates());
    }

    @Test
    void testUpdateThrowsOnNonExistentCity() {
        assertThrows(CacheException.class, () ->
            cache.update(TEST_CITY_NON_EXISTENT, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis()));
    }

    @Test
    void testIsValid() {
        long timestamp = System.currentTimeMillis();
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);

        long currentTime = timestamp + ONE_SECOND_MILLIS;
        long ttlMillis = TEN_MINUTES_MILLIS;

        assertTrue(cache.isValid(TEST_CITY_MOSCOW, currentTime, ttlMillis));
    }

    @Test
    void testIsValidReturnsFalseForExpired() {
        long timestamp = System.currentTimeMillis() - TWENTY_SECONDS_MILLIS;
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);

        long currentTime = System.currentTimeMillis();
        long ttlMillis = TEN_SECONDS_MILLIS;

        assertFalse(cache.isValid(TEST_CITY_MOSCOW, currentTime, ttlMillis));
    }

    @Test
    void testIsValidReturnsFalseForNonExistent() {
        assertFalse(cache.isValid(TEST_CITY_NON_EXISTENT, System.currentTimeMillis(), ONE_MINUTE_MILLIS));
    }

    @Test
    void testGetCoordinates() {
        long timestamp = System.currentTimeMillis();
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);

        Coordinates coords = cache.getCoordinates(TEST_CITY_MOSCOW);
        assertNotNull(coords);
        assertEquals(testCoordinates, coords);
    }

    @Test
    void testGetCoordinatesReturnsNullForNonExistent() {
        Coordinates coords = cache.getCoordinates(TEST_CITY_NON_EXISTENT);
        assertNull(coords);
    }

    @Test
    void testGetAllCities() {
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis());
        cache.put(TEST_CITY_LONDON, new Coordinates(LONDON_LAT, LONDON_LON),
            testWeatherData, ApiVersion.V2_5, System.currentTimeMillis());

        List<String> cities = cache.getAllCities();
        assertEquals(TWO_ENTRIES_CACHE_SIZE, cities.size());
        assertTrue(cities.contains(TEST_CITY_MOSCOW));
        assertTrue(cities.contains(TEST_CITY_LONDON));
    }

    @Test
    void testRemove() {
        long timestamp = System.currentTimeMillis();
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);

        cache.remove(TEST_CITY_MOSCOW);

        CacheEntry entry = cache.get(TEST_CITY_MOSCOW);
        assertNull(entry);
        assertEquals(EMPTY_CACHE_SIZE, cache.size());
    }

    @Test
    void testRemoveDoesNothingForNonExistent() {
        assertDoesNotThrow(() -> cache.remove(TEST_CITY_NON_EXISTENT));
    }

    @Test
    void testClear() {
        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis());
        cache.put(TEST_CITY_LONDON, new Coordinates(LONDON_LAT, LONDON_LON),
            testWeatherData, ApiVersion.V2_5, System.currentTimeMillis());

        cache.clear();

        assertEquals(EMPTY_CACHE_SIZE, cache.size());
        assertNull(cache.get(TEST_CITY_MOSCOW));
        assertNull(cache.get(TEST_CITY_LONDON));
    }

    @Test
    void testSize() {
        assertEquals(EMPTY_CACHE_SIZE, cache.size());

        cache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, System.currentTimeMillis());
        assertEquals(SINGLE_ENTRY_CACHE_SIZE, cache.size());

        cache.put(TEST_CITY_LONDON, new Coordinates(LONDON_LAT, LONDON_LON),
            testWeatherData, ApiVersion.V2_5, System.currentTimeMillis());
        assertEquals(TWO_ENTRIES_CACHE_SIZE, cache.size());
    }

    @Test
    void testLRUEviction() throws InterruptedException {
        LRUWeatherCache smallCache = new LRUWeatherCache(SMALL_CACHE_SIZE, DEFAULT_CACHE_TTL_MINUTES);

        long timestamp = System.currentTimeMillis();
        smallCache.put(TEST_CITY_MOSCOW, testCoordinates, testWeatherData, ApiVersion.V2_5, timestamp);
        smallCache.put(TEST_CITY_LONDON, new Coordinates(LONDON_LAT, LONDON_LON),
            testWeatherData, ApiVersion.V2_5, timestamp);

        assertEquals(TWO_ENTRIES_CACHE_SIZE, smallCache.size());

        smallCache.get(TEST_CITY_MOSCOW);
        smallCache.get(TEST_CITY_MOSCOW);

        smallCache.put(TEST_CITY_PARIS, new Coordinates(PARIS_LAT, PARIS_LON),
            testWeatherData, ApiVersion.V2_5, timestamp);

        smallCache.put(TEST_CITY_BERLIN, new Coordinates(BERLIN_LAT, BERLIN_LON),
            testWeatherData, ApiVersion.V2_5, timestamp);
        smallCache.put(TEST_CITY_TOKYO, new Coordinates(TOKYO_LAT, TOKYO_LON),
            testWeatherData, ApiVersion.V2_5, timestamp);

        for (int i = 0; i < ITERATION_COUNT; i++) {
            smallCache.get(TEST_CITY_TOKYO);
            smallCache.get(TEST_CITY_BERLIN);
        }

        Thread.sleep(SLEEP_MILLIS);

        List<String> allCities = smallCache.getAllCities();
        assertTrue(allCities.size() <= SMALL_CACHE_SIZE,
            "Cache should contain at most " + SMALL_CACHE_SIZE +
                " cities after eviction, but found: " + allCities.size());

        int foundCount = 0;
        if (smallCache.get(TEST_CITY_MOSCOW) != null) foundCount++;
        if (smallCache.get(TEST_CITY_PARIS) != null) foundCount++;
        if (smallCache.get(TEST_CITY_BERLIN) != null) foundCount++;
        if (smallCache.get(TEST_CITY_TOKYO) != null) foundCount++;
        if (smallCache.get(TEST_CITY_LONDON) != null) foundCount++;

        assertTrue(foundCount <= SMALL_CACHE_SIZE,
            "At most " + SMALL_CACHE_SIZE +
                " cities should be accessible in cache after eviction, but found: " + foundCount);
    }
}

