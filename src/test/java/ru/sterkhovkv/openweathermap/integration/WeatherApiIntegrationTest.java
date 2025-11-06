package ru.sterkhovkv.openweathermap.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sterkhovkv.openweathermap.api.OpenWeatherMapSDK;
import ru.sterkhovkv.openweathermap.api.SDKMode;
import ru.sterkhovkv.openweathermap.client.GeocodingClient;
import ru.sterkhovkv.openweathermap.client.WeatherApiClient;
import ru.sterkhovkv.openweathermap.config.ApiVersion;
import ru.sterkhovkv.openweathermap.config.SDKConfig;
import ru.sterkhovkv.openweathermap.exception.CityNotFoundException;
import ru.sterkhovkv.openweathermap.exception.InvalidApiKeyException;
import ru.sterkhovkv.openweathermap.factory.SDKFactory;
import ru.sterkhovkv.openweathermap.model.Coordinates;
import ru.sterkhovkv.openweathermap.model.v2.WeatherDataV2;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherApiIntegrationTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_CITY_NAME = "Moscow";
    private static final String TEST_CITY_NON_EXISTENT = "NonExistentCity";
    private static final int DEFAULT_CACHE_SIZE = 10;
    private static final long DEFAULT_CACHE_TTL_MINUTES = 10;
    private static final double MOSCOW_LAT = 55.7558;
    private static final double MOSCOW_LON = 37.6173;
    private static final String FIELD_GEOCODING_CLIENT = "geocodingClient";
    private static final String FIELD_WEATHER_API_CLIENT = "weatherApiClient";

    @Mock
    private GeocodingClient geocodingClient;

    @Mock
    private WeatherApiClient weatherApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        SDKFactory.removeAllInstances();
    }

    @Test
    void testGetWeatherWithValidResponse() {
        Coordinates coordinates = new Coordinates(MOSCOW_LAT, MOSCOW_LON);
        WeatherDataV2 weatherData = new WeatherDataV2();

        when(geocodingClient.getCoordinates(TEST_CITY_NAME)).thenReturn(coordinates);
        when(weatherApiClient.fetchWeather(coordinates)).thenReturn(weatherData);
        when(weatherApiClient.getApiVersion()).thenReturn(ApiVersion.V2_5);

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .cacheSize(DEFAULT_CACHE_SIZE)
            .cacheTtlMinutes(DEFAULT_CACHE_TTL_MINUTES)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertThrows(Exception.class, () -> {
            sdk.getWeather(TEST_CITY_NAME);
        });
    }

    @Test
    void testGetWeatherWithCityNotFound() {
        when(geocodingClient.getCoordinates(TEST_CITY_NON_EXISTENT))
            .thenThrow(new CityNotFoundException("City not found: " + TEST_CITY_NON_EXISTENT));

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertThrows(CityNotFoundException.class, () -> {
            sdk.getWeather(TEST_CITY_NON_EXISTENT);
        });
    }

    @Test
    void testGetWeatherWithInvalidApiKey() {
        when(geocodingClient.getCoordinates(TEST_CITY_NAME))
            .thenThrow(new InvalidApiKeyException("Invalid API key"));

        SDKConfig config = SDKConfig.builder()
            .apiVersion(ApiVersion.V2_5)
            .build();

        OpenWeatherMapSDK sdk = createSDKWithMocks(TEST_API_KEY, SDKMode.ON_DEMAND, config);

        assertThrows(InvalidApiKeyException.class, () -> {
            sdk.getWeather(TEST_CITY_NAME);
        });
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
}

