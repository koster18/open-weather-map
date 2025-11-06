package ru.sterkhovkv.openweathermap.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sterkhovkv.openweathermap.client.GeocodingClient;
import ru.sterkhovkv.openweathermap.client.WeatherApiClient;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.CityNotFoundException;
import ru.sterkhovkv.openweathermap.exception.IllegalSDKStateException;
import ru.sterkhovkv.openweathermap.exception.InvalidApiKeyException;
import ru.sterkhovkv.openweathermap.exception.NetworkException;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;
import ru.sterkhovkv.openweathermap.model.CacheEntry;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.model.WeatherResponse;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;
import ru.sterkhovkv.openweathermap.util.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenWeatherMapSDKTest {

    private static final String TEST_API_KEY = "test-api-key-for-sdk";
    private static final String TEST_CITY_NAME = "Moscow";
    private static final String TEST_CITY_LONDON = "London";
    private static final String EMPTY_STRING = "";
    private static final String BLANK_STRING = "   ";
    private static final String API_KEY_MASK = "****";
    private static final int EMPTY_CACHE_SIZE = 0;
    private static final int TEST_CACHE_SIZE = 20;
    private static final long TEST_CACHE_TTL_MINUTES = 15;
    private static final double MOSCOW_LAT = 55.7558;
    private static final double MOSCOW_LON = 37.6173;
    private static final double LONDON_LAT = 51.5074;
    private static final double LONDON_LON = -0.1278;
    private static final String FIELD_GEOCODING_CLIENT = "geocodingClient";
    private static final String FIELD_WEATHER_API_CLIENT = "weatherApiClient";
    private static final long TEST_TEMP = 273;
    private static final long TEST_FEELS_LIKE = 270;
    private static final int TEST_VISIBILITY = 10000;
    private static final double TEST_WIND_SPEED = 2.5;
    private static final long TEST_DATETIME = 1675744800L;
    private static final int TEST_TIMEZONE = 3600;
    private static final String WEATHER_MAIN = "Clear";
    private static final String WEATHER_DESCRIPTION = "clear sky";

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private WeatherApiClient weatherApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    @AfterEach
    void cleanup() {
        SDKFactory.removeAllInstances();
    }

    @Test
    void testSDKCreation() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertNotNull(sdk);
        assertEquals(SDKMode.ON_DEMAND, sdk.getMode());
        assertNotNull(sdk.getApiKey());
        assertEquals(EMPTY_CACHE_SIZE, sdk.getCacheSize());
    }

    @Test
    void testSDKCreationWithConfig() {
        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .build();

        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertNotNull(sdk);
        assertEquals(SDKMode.ON_DEMAND, sdk.getMode());
    }

    @Test
    void testGetWeatherThrowsOnNullCityName() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertThrows(IllegalArgumentException.class, () -> sdk.getWeather(null));
    }

    @Test
    void testGetWeatherThrowsOnBlankCityName() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertThrows(IllegalArgumentException.class, () -> sdk.getWeather(EMPTY_STRING));

        assertThrows(IllegalArgumentException.class, () -> sdk.getWeather(BLANK_STRING));
    }

    @Test
    void testGetWeatherThrowsAfterDestroy() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);
        sdk.destroy();

        assertThrows(IllegalSDKStateException.class, () -> sdk.getWeather(TEST_CITY_NAME));
    }

    @Test
    void testDestroy() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertDoesNotThrow(sdk::destroy);
    }

    @Test
    void testDestroyTwice() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);
        sdk.destroy();

        assertDoesNotThrow(sdk::destroy);
    }

    @Test
    void testGetApiKey() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        String maskedKey = sdk.getApiKey();
        assertNotNull(maskedKey);
        assertTrue(maskedKey.contains(API_KEY_MASK));
    }

    @Test
    void testGetCacheSize() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.ON_DEMAND);

        assertEquals(EMPTY_CACHE_SIZE, sdk.getCacheSize());
    }

    @Test
    void testPollingModeCreation() {
        OpenWeatherMapSDK sdk = SDKFactory.getInstance(TEST_API_KEY, SDKMode.POLLING);

        assertNotNull(sdk);
        assertEquals(SDKMode.POLLING, sdk.getMode());
    }

    @Test
    void testGetWeatherSuccessOnDemand() {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        WeatherDataV2 weatherData = createTestWeatherDataV2();

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates)).thenReturn(weatherData);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        WeatherResponse response = sdk.getWeather(TEST_CITY_NAME);

        assertNotNull(response);
        assertEquals(TEST_CITY_NAME, response.getName());
        assertNotNull(response.getTemperature());
        assertEquals((double) TEST_TEMP, response.getTemperature().getTemp());
        assertEquals(1, sdk.getCacheSize());

        verify(geocodingClient, times(1)).getCoordinates(TEST_CITY_NAME);
        verify(weatherApiClient, times(1)).fetchWeather(coordinates);
    }

    @Test
    void testGetWeatherCacheHit() {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        WeatherDataV2 weatherData = createTestWeatherDataV2();

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates)).thenReturn(weatherData);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        WeatherResponse response1 = sdk.getWeather(TEST_CITY_NAME);
        WeatherResponse response2 = sdk.getWeather(TEST_CITY_NAME);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getName(), response2.getName());

        verify(geocodingClient, times(1)).getCoordinates(TEST_CITY_NAME);
        verify(weatherApiClient, times(1)).fetchWeather(coordinates);
    }

    @Test
    void testGetWeatherCacheMissAfterExpiration() throws Exception {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        WeatherDataV2 weatherData = createTestWeatherDataV2();

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates)).thenReturn(weatherData);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(1)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        WeatherResponse response1 = sdk.getWeather(TEST_CITY_NAME);

        makeCacheEntryExpired(sdk, TEST_CITY_NAME);

        WeatherResponse response2 = sdk.getWeather(TEST_CITY_NAME);

        assertNotNull(response1);
        assertNotNull(response2);

        verify(geocodingClient, atLeast(2)).getCoordinates(TEST_CITY_NAME);
        verify(weatherApiClient, atLeast(2)).fetchWeather(coordinates);
    }

    @Test
    void testGetWeatherThrowsCityNotFoundException() {
        when(geocodingClient.getCoordinates(TEST_CITY_NAME))
            .thenThrow(new CityNotFoundException("City not found: " + TEST_CITY_NAME));

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertThrows(CityNotFoundException.class, () -> sdk.getWeather(TEST_CITY_NAME));

        verify(geocodingClient, times(1)).getCoordinates(TEST_CITY_NAME);
        verify(weatherApiClient, never()).fetchWeather(any());
    }

    @Test
    void testGetWeatherThrowsInvalidApiKeyException() {
        when(geocodingClient.getCoordinates(TEST_CITY_NAME))
            .thenThrow(new InvalidApiKeyException("Invalid API key"));

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertThrows(InvalidApiKeyException.class, () -> sdk.getWeather(TEST_CITY_NAME));
    }

    @Test
    void testGetWeatherThrowsNetworkException() {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates))
            .thenThrow(new NetworkException("Network error"));

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertThrows(NetworkException.class, () -> sdk.getWeather(TEST_CITY_NAME));
    }

    @Test
    void testGetWeatherPollingMode() {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        WeatherDataV2 weatherData = createTestWeatherDataV2();

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates)).thenReturn(weatherData);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .pollingIntervalMinutes(1)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.POLLING, config);

        WeatherResponse response = sdk.getWeather(TEST_CITY_NAME);

        assertNotNull(response);
        assertEquals(TEST_CITY_NAME, response.getName());
        assertEquals(SDKMode.POLLING, sdk.getMode());

        verify(geocodingClient, times(1)).getCoordinates(TEST_CITY_NAME);
        verify(weatherApiClient, times(1)).fetchWeather(coordinates);
    }

    @Test
    void testGetWeatherMultipleCities() {
        Coordinates moscowCoords = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        Coordinates londonCoords = new Coordinates(LONDON_LAT, LONDON_LON);
        WeatherDataV2 moscowWeather = createTestWeatherDataV2();
        WeatherDataV2 londonWeather = createTestWeatherDataV2ForCity(TEST_CITY_LONDON);

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(moscowCoords);
        when(geocodingClient.getCoordinates(TEST_CITY_LONDON)).thenReturn(londonCoords);
        when(weatherApiClient.fetchWeather(moscowCoords)).thenReturn(moscowWeather);
        when(weatherApiClient.fetchWeather(londonCoords)).thenReturn(londonWeather);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        WeatherResponse moscowResponse = sdk.getWeather(TEST_CITY_NAME);
        WeatherResponse londonResponse = sdk.getWeather(TEST_CITY_LONDON);

        assertNotNull(moscowResponse);
        assertNotNull(londonResponse);
        assertEquals(TEST_CITY_NAME, moscowResponse.getName());
        assertEquals(TEST_CITY_LONDON, londonResponse.getName());
        assertEquals(2, sdk.getCacheSize());
    }

    @Test
    void testDestroyClearsCache() {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        WeatherDataV2 weatherData = createTestWeatherDataV2();

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates)).thenReturn(weatherData);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(TEST_CACHE_SIZE)
            .cacheTtlMinutes(TEST_CACHE_TTL_MINUTES)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        sdk.getWeather(TEST_CITY_NAME);
        assertEquals(1, sdk.getCacheSize());

        sdk.destroy();
        assertEquals(EMPTY_CACHE_SIZE, sdk.getCacheSize());
    }

    private OpenWeatherMapSDK createSDKWithMocks(String apiKey, SDKMode mode, SDKConfig config) {
        OpenWeatherMapSDK sdk = new OpenWeatherMapSDK(apiKey, mode, config);
        try {
            Field geocodingField = OpenWeatherMapSDK.class.getDeclaredField(FIELD_GEOCODING_CLIENT);
            geocodingField.setAccessible(true);
            geocodingField.set(sdk, geocodingClient);

            Field weatherField = OpenWeatherMapSDK.class.getDeclaredField(FIELD_WEATHER_API_CLIENT);
            weatherField.setAccessible(true);
            weatherField.set(sdk, weatherApiClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocks into SDK", e);
        }
        return sdk;
    }

    private WeatherDataV2 createTestWeatherDataV2() {
        return createTestWeatherDataV2ForCity(TEST_CITY_NAME);
    }

    private WeatherDataV2 createTestWeatherDataV2ForCity(String cityName) {
        WeatherDataV2 weatherData = new WeatherDataV2();
        weatherData.setName(cityName);
        weatherData.setDatetime(TEST_DATETIME);
        weatherData.setVisibility(TEST_VISIBILITY);
        weatherData.setTimezone(TEST_TIMEZONE);

        WeatherDataV2.MainData main = new WeatherDataV2.MainData();
        main.setTemp((double) TEST_TEMP);
        main.setFeelsLike((double) TEST_FEELS_LIKE);
        weatherData.setMain(main);

        WeatherDataV2.Wind wind = new WeatherDataV2.Wind();
        wind.setSpeed(TEST_WIND_SPEED);
        weatherData.setWind(wind);

        WeatherDataV2.WeatherCondition condition = new WeatherDataV2.WeatherCondition();
        condition.setMain(WEATHER_MAIN);
        condition.setDescription(WEATHER_DESCRIPTION);
        weatherData.setWeather(List.of(condition));

        WeatherDataV2.SystemData sys = new WeatherDataV2.SystemData();
        sys.setSunrise(TEST_DATETIME);
        sys.setSunset(TEST_DATETIME);
        weatherData.setSys(sys);

        return weatherData;
    }

    private void makeCacheEntryExpired(OpenWeatherMapSDK sdk, String cityName) throws Exception {
        Field cacheField = OpenWeatherMapSDK.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Object cache = cacheField.get(sdk);

        Method getMethod = cache.getClass().getDeclaredMethod("get", String.class);
        getMethod.setAccessible(true);

        Method putMethod = cache.getClass().getDeclaredMethod("put",
            String.class, Coordinates.class, Object.class, ApiVersion.class, long.class);
        putMethod.setAccessible(true);

        CacheEntry entry = (CacheEntry) getMethod.invoke(cache, cityName);
        if (entry != null) {
            long expiredTimestamp = System.currentTimeMillis() - (2 * Constants.MILLIS_PER_MINUTE);
            putMethod.invoke(cache, cityName, entry.coordinates(), entry.weatherData(),
                entry.apiVersion(), expiredTimestamp);
        }
    }
}
